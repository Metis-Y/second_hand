package com.example.controller;

import com.example.dto.Result;
import com.example.dto.LoginDTO;
import com.example.dto.RegisterDTO;
import com.example.entity.User;
import com.example.service.AuthService;
import com.example.vo.LoginVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口：注册、登录
 * 对应接口文档：POST /api/auth/register、POST /api/auth/login
 */
@RestController
@RequestMapping("/api/auth")
public class LoginController {
    @Autowired
    private AuthService authService;

    /** 用户注册 */
    @PostMapping("/register")
    public Result<User> register(@RequestBody @Valid RegisterDTO dto) {
        return Result.success(authService.register(dto));
    }

    /** 用户登录 */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        return Result.success(authService.login(dto));
    }
}