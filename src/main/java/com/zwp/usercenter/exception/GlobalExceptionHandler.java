package com.zwp.usercenter.exception;

import com.zwp.usercenter.common.BaseResponse;
import com.zwp.usercenter.common.ErrorCode;
import com.zwp.usercenter.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * @author zwp
 */
@Slf4j
@RestControllerAdvice
// @RestControllerAdvice注解的类主要用于为所有的@RestController
// 或指定范围的@RestController提供全局的AOP(面向切面编程)功能,并直接将处理结果作为响应体返回。
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse businessExceptionHandler(BusinessException e) {
        log.error("businessException: " + e.getMessage(), e);
        return ResultUtils.error(e.getCode(), e.getMessage(), e.getDescription());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse runtimeExceptionHandler(RuntimeException e) {
        log.error("runtimeException" , e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage(), "");
    }
}
