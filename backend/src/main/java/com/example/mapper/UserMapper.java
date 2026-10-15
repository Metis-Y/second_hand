package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.User;

/**
 * 用户数据访问层：继承 BaseMapper 即可获得单表增删改查能力
 * （由启动类上的 @MapperScan("com.example.mapper") 扫描）
 */
public interface UserMapper extends BaseMapper<User> {
}