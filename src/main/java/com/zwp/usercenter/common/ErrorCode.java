package com.zwp.usercenter.common;

/**
 * 错误码
 * @author zwp
 */
public enum ErrorCode {

    SUCCESS(0, "ok", ""),
    PARAMS_ERROR(4000, "请求参数错误", ""),
    NULL_ERROR(4001, "请求数据为空", ""),
    NOT_LOGIN(40100, "未登录", ""),
    NO_AUTH(40101, "无权限", ""),
    SYSTEM_ERROR(50000, "系统内部异常", "");
    // 分号的作用是分隔枚举常量列表和枚举类型的其他成员（例如，字段、方法或构造函数）
    // 如果枚举类型只包含枚举常量列表，并且没有其他成员（字段、方法、构造函数），那么分号 ; 是可选的。 例如
    // public enum Color { RED, GREEN, BLUE } // 分号可以省略
    private final int code;
    /**
     * 状态码信息
     */
    private final String message;
    /**
     * 状态码描述（详情）
     */
    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
}
