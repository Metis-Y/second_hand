package com.example.exception;

import com.example.dto.Result;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器：保证所有错误都以 Result{code:0, message, data:null} 的形式返回
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常处理 */
    @ExceptionHandler(BusinessException.class)
    public Result handleBusiness(BusinessException e) {
        return Result.error(e.getMessage());
    }

    /** 参数校验失败（@Valid 触发） */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result handleValid(MethodArgumentNotValidException e) {
        FieldError fe = e.getBindingResult().getFieldError();
        String msg = fe != null ? fe.getDefaultMessage() : "参数校验失败";
        return Result.error(msg);
    }

    /** 其它未预期异常 */
    @ExceptionHandler(Exception.class)
    public Result handleOther(Exception e) {
        return Result.error("服务器异常：" + e.getMessage());
    }
}