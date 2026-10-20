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

    /** 查询用户资料（个人主页资料卡），返回 User 实体，密码置空 */
    User getUserById(Long id);

    /** 编辑当前登录用户的资料（昵称、头像、手机号、性别） */
    void updateProfile(User user);
}