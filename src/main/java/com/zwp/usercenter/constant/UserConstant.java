package com.zwp.usercenter.constant;

// 接口中定义的任何字段（变量）都会 隐式地 被声明为 public static final。 这意味着：
// public: 常量是公开的，可以从任何地方访问。
// static: 常量是属于接口本身的，而不是接口的任何实现类的实例。
// final: 常量的值在定义后不能被修改。
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "userLoginState";

    // --------权限--------
    /**
     * 默认权限
     */
    int DEFAULT_ROLE = 0;

    /**
     * 管理员权限
     */
    int ADMIN_ROLE = 1;
}
