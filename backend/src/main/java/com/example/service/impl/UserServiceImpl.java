package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.dto.UserDTO;
import com.example.entity.User;
import com.example.exception.BusinessException;
import com.example.mapper.UserMapper;
import com.example.service.UserService;
import com.example.utils.JwtUtil;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private JwtUtil jwtUtil;

    @Override
    public UserDTO register(User user) {
        // 1. 用户名唯一性校验
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));
        if (count != null && count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 补全注册默认字段
        if (user.getNickname() == null || user.getNickname().isBlank()) {
            user.setNickname(user.getUsername());
        }
        user.setRole(0);

        // 3. 入库
        userMapper.insert(user);

        // 4. 生成令牌，组装 UserDTO 返回
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        UserDTO userDTO = toUserDTO(user, token);
        return userDTO;
    }

    @Override
    public UserDTO login(User user) {
        // 1. 按用户名查用户（取数据库里的完整用户信息）
        User dbUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, user.getUsername()));

        // 2. 校验（用户不存在 或 密码不匹配，统一返回“用户名或密码错误”）
        if (dbUser == null || !user.getPassword().equals(dbUser.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 3. 生成令牌，组装 UserDTO 返回
        String token = jwtUtil.generateToken(dbUser.getId(), dbUser.getUsername());
        UserDTO userDTO = toUserDTO(dbUser, token);
        return userDTO;
    }

    /** 组装返回 DTO：token + 用户基础信息（不含密码、手机号、性别） */
    private UserDTO toUserDTO(User user, String token) {
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        userDTO.setToken(token);
        return userDTO;
    }
}