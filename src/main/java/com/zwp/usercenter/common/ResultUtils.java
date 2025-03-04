package com.zwp.usercenter.common;

/**
 * 返回工具类
 * @author zwp
 */
public class ResultUtils {

    /**
     * 成功
     * @param data
     * @return
     * @param <T>
     */
    // 第一个 <T> (方法声明中): 声明 success 方法为泛型方法，引入类型参数 T.
    // 第二个 <T> (返回类型中): 使用类型参数 T 来指定 BaseResponse 对象的泛型类型，确保返回值能够携带正确类型的数据
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * 失败
     * @param errorCode
     * @return
     */
    public static BaseResponse error (ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败
     * @param code
     * @return
     */
    public static BaseResponse error (int code, String message, String description) {
        return new BaseResponse<>(code, null, message, description);
    }

    /**
     * 失败
     * @param errorCode
     * @return
     */
    public static BaseResponse error (ErrorCode errorCode, String message, String description) {
        return new BaseResponse<>(errorCode.getCode(), null, message, description);
    }

    /**
     * 失败
     * @param errorCode
     * @return
     */
    public static BaseResponse error (ErrorCode errorCode, String description) {
        return new BaseResponse<>(errorCode.getCode(), description);
    }
}
