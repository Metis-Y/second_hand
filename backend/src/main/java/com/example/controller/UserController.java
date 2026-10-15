package com.example.controller;

import com.example.dto.Result;
import com.example.dto.UserDTO;
import com.example.entity.User;
import com.example.service.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：注册、登录
 * 对应接口文档：POST /api/auth/register、POST /api/auth/login
 */
@RestController
@RequestMapping("/api")
public class UserController {

    @Resource
    private UserService userService;

    /** 用户注册 */
    @PostMapping("/auth/register")
    public Result register(@RequestBody @Valid User user) {
        UserDTO userDTO = userService.register(user);
        return Result.success(userDTO);
    }

    /** 用户登录 */
    @PostMapping("/auth/login")
    public Result login(@RequestBody @Valid User user) {
        UserDTO userDTO = userService.login(user);
        return Result.success(userDTO);
    }


}