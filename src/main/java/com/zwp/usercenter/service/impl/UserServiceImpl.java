package com.zwp.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zwp.usercenter.common.ErrorCode;
import com.zwp.usercenter.exception.BusinessException;
import com.zwp.usercenter.model.domain.User;
import com.zwp.usercenter.service.UserService;
import com.zwp.usercenter.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.zwp.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
* @author zwp
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-02-16 19:48:54
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

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
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"不能包含特殊字符");
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
        return safetyUser;
    }

    /**
     * 用户注销
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }
}




