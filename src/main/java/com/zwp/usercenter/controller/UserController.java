package com.zwp.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zwp.usercenter.common.BaseResponse;
import com.zwp.usercenter.common.ErrorCode;
import com.zwp.usercenter.common.ResultUtils;
import com.zwp.usercenter.exception.BusinessException;
import com.zwp.usercenter.model.domain.User;
import com.zwp.usercenter.model.domain.request.UserLoginRequest;
import com.zwp.usercenter.model.domain.request.UserRegisterRequest;
import com.zwp.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.zwp.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.zwp.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 * @author zwp
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

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
        if (!isAdmin(request)) {
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
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);// mybatis-plus的逻辑删除，更新为已删除状态
        return ResultUtils.success(b);
    }

    /**
     * 是否为管理员
     * @param request
     * @return
     */
    private boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可删除
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return  user != null && user.getUserRole() == ADMIN_ROLE;
    }
}
