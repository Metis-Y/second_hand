package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息返回 DTO：注册、登录统一返回（不含密码、手机号、性别）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    /** 登录令牌（JWT） */
    private String token;

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 头像地址 */
    private String avatar;

    /** 角色，0 普通用户、1 管理员 */
    private Integer role;
}