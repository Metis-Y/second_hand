package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.Order;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 订单 Mapper
 */
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 联表查询订单列表：orders JOIN users（买家/卖家）JOIN product，
     * 按身份（role）和状态（status 可空）筛选，下单时间倒序。
     * SQL 见 resources/mapper/OrderMapper.xml
     *
     * @param userId 当前用户ID
     * @param role   身份过滤："seller" 查我卖出的，非 "seller" 查我买到的
     * @param status 订单状态过滤，null 表示全部
     */
    List<Order> selectOrders(@Param("userId") Long userId,
                             @Param("role") String role,
                             @Param("status") Integer status);
}