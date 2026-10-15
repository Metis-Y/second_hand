package com.example.exception;

/**
 * 业务异常：用于在 service 里抛出可预期的错误，由全局异常处理器统一转成 Result
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}