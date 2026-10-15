package com.example.service;

import com.example.dto.UserDTO;
import com.example.entity.User;

/**
 * 认证业务：注册、登录
 */
public interface UserService {

    /** 注册：创建新用户，返回用户信息 DTO（含令牌） */
    UserDTO register(User user);

    /** 登录：校验用户名密码，返回用户信息 DTO（含令牌） */
    UserDTO login(User user);
}