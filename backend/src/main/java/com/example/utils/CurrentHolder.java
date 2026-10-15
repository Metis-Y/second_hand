package com.example.utils;

/**
 * 保存当前登录用户 id（ThreadLocal，线程私有，每个请求各自独立）
 */
public class CurrentHolder {

    private static final ThreadLocal<Long> CURRENT_ID = new ThreadLocal<>();

    /** 保存当前登录用户 id（拦截器解析 token 后调用） */
    public static void setCurrentId(Long userId) {
        CURRENT_ID.set(userId);
    }

    /** 获取当前登录用户 id（业务代码需要使用"当前用户"时调用） */
    public static Long getCurrentId() {
        return CURRENT_ID.get();
    }

    /** 请求结束后移除，防止内存泄漏 */
    public static void remove() {
        CURRENT_ID.remove();
    }
}