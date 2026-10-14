package com.example.dto;

import lombok.Data;

/**
 * 统一返回结果
 * { code, message, data }
 * code = 200 表示成功，其它值表示失败
 */
/*
@Data
public class Result<T> {

    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 200;
        r.message = "ok";
        r.data = data;
        return r;
    }

    public static <T> Result<T> error(int code, String message) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = message;
        r.data = null;
        return r;
    }
}*/
@Data
public class Result {

    private Integer code; //编码：1成功，0为失败
    private String message; //错误信息
    private Object data; //数据

    public static Result success() {
        Result result = new Result();
        result.code = 1;
        result.message = "success";
        return result;
    }

    public static Result success(Object object) {
        Result result = new Result();
        result.data = object;
        result.code = 1;
        result.message = "success";
        return result;
    }

    public static Result error(String msg) {
        Result result = new Result();
        result.message = msg;
        result.code = 0;
        return result;
    }

}