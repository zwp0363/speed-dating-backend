package com.zwp.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回类
 * @param <T>
 * @author zwp
 */
@Data
public class BaseResponse<T> implements Serializable { // 加泛型提高可复用性

    private int code;

    private T data;

    private String message;

    private String description;

    public BaseResponse(int code, T data, String message, String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }

    public BaseResponse(int code, T data, String message) {
        this(code, data, message, "");
    }

    public BaseResponse(int code, T data) {
        this(code, data, "","");
    }
    // this(...): 这表示 调用同一个类中的另一个构造方法 (构造方法重载)。
    // (code, data, ""): 它调用了 第一个构造方法 (public BaseResponse(int code, T data, String message))，
    // 并将当前构造方法的 code 和 data 参数传递给第一个构造方法，同时 硬编码了一个空字符串 "" 作为 message 参数。
    // 当只需要返回状态码和数据，不需要额外消息时，可以使用这个简化的构造方法。

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage(), errorCode.getDescription());
    }
}
