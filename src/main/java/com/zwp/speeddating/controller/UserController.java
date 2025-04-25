package com.zwp.speeddating.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zwp.speeddating.common.BaseResponse;
import com.zwp.speeddating.common.ErrorCode;
import com.zwp.speeddating.common.ResultUtils;
import com.zwp.speeddating.exception.BusinessException;
import com.zwp.speeddating.model.domain.User;
import com.zwp.speeddating.model.request.UserLoginRequest;
import com.zwp.speeddating.model.request.UserRegisterRequest;
import com.zwp.speeddating.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.zwp.speeddating.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 * @author zwp
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/register")
    // @RequestBody 是 Spring MVC 中用于处理 HTTP 请求体的关键注解。它负责将请求体数据转换为 Java 对象，方便 Controller 方法使用
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE); // 获取用户的登录态
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long userId = currentUser.getId();
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
        // Session 中的 currentUser 可能过时: currentUser 对象是从 HttpSession 中获取的。Session 是一种缓存机制，用户登录信息会被存储在 Session 中，以便在后续请求中快速识别用户身份，而无需每次都重新验证。
        // 数据库数据可能已更新: 在用户登录后，数据库中的用户信息可能发生变化（例如，用户修改了个人资料、权限被更改、账户状态被禁用等等）。Session 中存储的 currentUser 对象可能没有反映这些最新的数据库更改。
        // userService.getById(userId) 重新从数据库获取最新数据: 通过 userService.getById(userId)，代码会根据从 Session 中获取的 userId 再次查询数据库。这确保了返回的 user 对象总是最新的数据库记录，反映了用户信息的最新状态。
    }

    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            userQueryWrapper.like("username", username);
        }
        List<User> userList = userService.list(userQueryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
        /* stream() 方法将 userList 转换为一个 Stream (流)。Stream 是 Java 8 引入的用于处理集合数据的抽象概念，它允许进行函数式风格的操作。
         map() 是 Stream 的一个 中间操作 (intermediate operation)。它会将流中的每个元素 转换 成另一个元素。
         user -> { ... } 是一个 Lambda 表达式，它定义了转换的逻辑。对于流中的每个 user 对象，这段 Lambda 表达式会被执行
         collect() 是 Stream 的一个 终端操作 (terminal operation)。它会 收集 流中的元素，并将它们 汇总成一个结果。
         Collectors.toList() 是 Collectors 类提供的一个静态方法，它会创建一个 Collector，用于将流中的元素收集到一个 新的 List 中。*/
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUsers(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);// mybatis-plus的逻辑删除，更新为已删除状态
        return ResultUtils.success(b);
    }

    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        String redisKey = String.format("zwp:user:recommend:%s", loginUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        // 如果有缓存，直接读缓存
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);
        if (userPage != null) {
            return ResultUtils.success(userPage);
        }
        // 无缓存，查数据库
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        // 偏移量 = (页码 - 1) * 每页条数,定义了从总数据集中的哪一条记录开始查询,配置了MybatisPlus分页插件，会自动处理
//       Page<User> userList = userService.page(new Page<>((pageNum - 1) * pageSize, pageSize), userQueryWrapper);
        userPage = userService.page(new Page<>(pageNum, pageSize), userQueryWrapper);
        // 写缓存
        try {
            // 缓存数据30秒自动失效，Redis会将其删除
            valueOperations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("redis set key error", e);
        }
        return ResultUtils.success(userPage);
    }

    /**
     * 根据标签搜索用户
     * @param tagNameList
     * @return
     */
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(User user, HttpServletRequest request) {
        // 1.检验参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2.校验权限
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        // 3.更新
        int result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }
}
