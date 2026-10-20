package com.example.controller;

import com.example.dto.Result;
import com.example.dto.UserDTO;
import com.example.entity.User;
import com.example.service.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户接口：注册、登录、查询资料、编辑资料
 * 对应接口文档：POST /api/auth/register、POST /api/auth/login、GET /api/users/{id}、PUT /api/users/me
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

    /** 查询用户资料（个人主页顶部资料卡） */
    @GetMapping("/users/{id}")
    public Result getUser(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    /** 编辑当前登录用户的资料 */
    @PutMapping("/users/me")
    public Result updateProfile(@RequestBody User user) {
        userService.updateProfile(user);
        return Result.success();
    }
}