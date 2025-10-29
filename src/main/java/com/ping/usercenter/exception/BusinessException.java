package com.ping.usercenter.exception;

import com.ping.usercenter.common.ErrorCode;

/**
 * 自定义业务异常类
 *
 * 给原本的异常类扩充了两个字段，并提供构造函数，可以传errorCode
 * @author ping
 */
public class BusinessException extends RuntimeException {

    private final int code;//不需要set异常中的属性，直接定义为final

    private final String description;

    //因为继承了 RuntimeException 所以直接调用其本身的构造函数
    public BusinessException(String message, int code, String description) {
        super(message); //传递message
        this.code = code;
        this.description = description;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage()); //传递message
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public BusinessException(ErrorCode errorCode, String description) {
        super(errorCode.getMessage()); //传递message
        this.code = errorCode.getCode();
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
