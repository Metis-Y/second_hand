package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.Notification;

/**
 * 站内通知 Mapper
 * 查询都是单表操作，用 MyBatis-Plus 的条件构造器就够了，不需要手写 SQL
 */
public interface NotificationMapper extends BaseMapper<Notification> {
}