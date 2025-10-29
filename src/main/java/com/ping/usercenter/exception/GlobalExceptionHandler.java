package com.ping.usercenter.exception;

import com.ping.usercenter.common.BaseResponse;
import com.ping.usercenter.common.ErrorCode;
import com.ping.usercenter.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 *
 * @author ping
 */
@RestControllerAdvice //切面功能，在任意代码前后做封装 / 额外处理
@Slf4j
public class GlobalExceptionHandler {

    //该方法只去捕获BusinessException.class的异常
    @ExceptionHandler(BusinessException.class)
    public BaseResponse businessExceptionHandler(BusinessException e) {
        log.error("businessException" + e.getMessage(), e);
        return ResultUtils.error(e.getCode(), e.getMessage(), e.getDescription());
    }

    //捕获别的异常
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse runtimeExceptionHandler(RuntimeException e) {
        log.error("runtimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage(), "");
    }

}
