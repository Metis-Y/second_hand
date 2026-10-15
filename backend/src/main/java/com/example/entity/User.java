package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体，对应表 users
 */
@Data
@TableName("users")
public class User {

    /** 用户ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名 */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 16, message = "用户名 2~16 位")
    private String username;

    /** 登录密码（目前明文存储，后续可升级 BCrypt） */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码 6~32 位")
    private String password;

    /** 昵称 */
    private String nickname;

    /** 手机号 */
    private String phone;

    /** 性别：1 男、2 女 */
    private Integer gender;

    /** 头像地址 */
    private String avatar;

    /** 角色：0 普通用户、1 管理员 */
    private Integer role;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}