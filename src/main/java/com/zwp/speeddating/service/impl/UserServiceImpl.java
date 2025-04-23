package com.zwp.speeddating.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zwp.speeddating.common.ErrorCode;
import com.zwp.speeddating.exception.BusinessException;
import com.zwp.speeddating.model.domain.User;
import com.zwp.speeddating.service.UserService;
import com.zwp.speeddating.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.zwp.speeddating.constant.UserConstant.ADMIN_ROLE;
import static com.zwp.speeddating.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 *
 * @author zwp
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-02-16 19:48:54
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "zwp";


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        // 1.校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号过长");
        }
        // 账户不能包含特殊字符
//        String validPattern = "\\pP|\\pS|\\s+";
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
         /* \\pP : 这是一个 Unicode 字符属性，表示 "Punctuation" (标点符号)。它会匹配任何标点符号字符，例如逗号、句号、问号、感叹号等等。 在 Java 正则表达式中，\p 用于匹配 Unicode 字符属性，P 是标点符号的缩写
         \\pS : 这也是一个 Unicode 字符属性，表示 "Symbol" (符号)。它会匹配各种符号字符，例如货币符号（$, €, ¥）、数学符号（+, =, ×）、
         \\s+ : 这表示匹配空白字符，\s 代表任何空白字符（包括空格、制表符、换行符等）， + 表示匹配一个或多个空白字符。
         正则表达式 \\pP|\\pS|\\s+ 的含义是：匹配 任何标点符号字符 或者 任何符号字符 或者 一个或多个空白字符 。
         注意，这里需要使用双反斜杠 \\，因为在 Java 字符串中，反斜杠 \ 是转义字符。*/
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        /* Pattern.compile(validPattern): 将 validPattern 字符串编译成一个 Pattern 对象。 Pattern 对象是正则表达式的编译表示形式，用于高效地进行匹配操作。
        .matcher(userAccount): 使用编译后的 Pattern 对象创建一个 Matcher 对象。 Matcher 对象负责在输入的 userAccount 字符串中查找与 Pattern 匹配的子序列*/
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能包含特殊字符");
        }  /* 用于验证 userAccount 字符串是否 不 包含任何特殊字符、符号或空白字符 。 如果 userAccount 不包含 这些字符，则返回 -1。*/
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码和校验密码相同");
        }
        // 账户不能重复
        /* 这里涉及数据库查询，为了避免资源浪费，放到最后校验，因为上述对账户不能包含特殊字符的校验已经确保账户不可能重复 */
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 星球编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编号重复");
        }
        // 2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        /*定义一个固定的盐值 SALT 为 "zwp"。 (请注意，这只是一个示例，实际应用中应该使用更安全的盐值生成和管理方式)。
        将盐值 SALT 和用户输入的原始密码 userPassword 拼接在一起。
        使用 MD5 哈希算法对拼接后的字符串进行哈希处理。
        将哈希结果转换为十六进制字符串。
        将十六进制的哈希密码存储在 encryptPassword 变量中*/
        // 3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        /* UserServiceImpl 类继承了 ServiceImpl<UserMapper, User>。 这是 MyBatis-Plus 框架提供的一个 通用 Service 实现类 。
        ServiceImpl 已经默认实现了一些基本的增删改查 (CRUD) 操作，包括 save(), updateById(), removeById(), getById() 等方法
        调用 this.save(user) 时，实际上是在调用 ServiceImpl 父类中提供的 save() 方法
        同时利用了 ServiceImpl 提供的默认功能 (例如主键生成) */
        if (!saveResult) {
            return -1;
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1.校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "账户或密码为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户过短");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码过短");
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能包含特殊字符");
        }
        // 2.加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户不存在");
        }
        // 3.用户信息脱敏(去除密码,即只允许返回前端的值)
        User safetyUser = getSafetyUser(user);
        // 4.记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        /* 将用户的登录状态（user 对象）存储在当前会话中，以便在后续的请求中可以访问该用户的登录信息
        setAttribute(String name, Object value): 这个方法用于在会话中存储一个属性。第一个参数是属性的名称，第二个参数是要存储的对象 */
        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据标签搜索用户（内存过滤版）
     *
     * @param tagNameList 用户拥有的标签
     * @return 用户集合
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 1.先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        // 2.在内存中判断是否包含要求的标签
        return userList.stream()
                .filter(user -> {
                    String tagStr = user.getTags(); // 获取用户的标签字符串
                    if (StringUtils.isBlank(tagStr)) {
                        return false;
                    }
                    // 将标签字符串解析为Set集合
                    // new TypeToken<Set<String>>() {}.getType(): 这是一个匿名内部类和 TypeToken 的用法，用于在运行时获取泛型类型 Set<String> 的 Type 对象。
                    // 由于 Java 的类型擦除，直接使用 Set<String>.class 是无法获取到泛型信息的，TypeToken 可以解决这个问题，让 Gson 正确地反序列化为 Set<String> 类型
                    Set<String> tempTagNameSet = gson.fromJson(tagStr, new TypeToken<Set<String>>() {}.getType());
                    // 如果 tempTagNameSet 是 null，就把它初始化为一个新的空的 HashSet，否则保持原样,等价if-null
                    tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
                    for (String tagName : tagNameList) { // 遍历给定的标签名称列表
                        if (!tempTagNameSet.contains(tagName)) { // 检查用户标签集合中是否包含当前搜索标签
                            return false; // 如果不包含，过滤掉该用户
                        }
                    }
                    return true; // 如果所有标签都包含，保留该用户
                })
                .map(this::getSafetyUser) // 将通过过滤的用户映射为安全用户
                .collect(Collectors.toList());
    }

    @Override
    public int updateUser(User user, User loginUser) {
        Long userId = user.getId();
        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 如果是管理员，允许更新任意用户
        // 如果不是管理员，只允许更新当前（自己的）信息
        // 如果不是管理员并且不是自己的信息，不允许更新信息
        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(user);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw  new BusinessException(ErrorCode.NO_AUTH);
        }
        return (User) userObj;
    }

    /**
     * 是否为管理员
     * @param request
     * @return
     */
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可删除
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return  user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 是否为管理员
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return  loginUser != null && loginUser.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 根据标签搜索用户（SQL 查询版）
     * @param tagNameList 用户拥有的标签
     * @return 用户集合
     */
    @Deprecated
    private List<User> searchUsersByTagsBySQL(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 拼接 and 查询
        // like '%Java%' and like '%Python%'
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName); //每次like都会自动拼接
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }
}




