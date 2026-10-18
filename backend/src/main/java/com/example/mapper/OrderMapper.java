package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.Order;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 订单 Mapper
 */
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 联表查询订单列表：orders JOIN users（买家/卖家）JOIN product，
     * 按身份（role）和状态（status 可空）筛选，下单时间倒序。
     *
     * @param userId 当前用户ID
     * @param role   身份过滤："seller" 查我卖出的，非 "seller" 查我买到的
     * @param status 订单状态过滤，null 表示全部
     */
    @Select("<script>" +
            "SELECT o.id, o.buyer_id, o.seller_id, o.product_id, o.price, o.status, o.create_time, o.update_time, " +
            "bu.nickname AS buyer_nickname, su.nickname AS seller_nickname, p.title AS product_title " +
            "FROM orders o " +
            "JOIN users bu ON o.buyer_id = bu.id " +
            "JOIN users su ON o.seller_id = su.id " +
            "JOIN product p ON o.product_id = p.id " +
            "<where>" +
            "<choose>" +
            "<when test=\"role == 'seller'\"> o.seller_id = #{userId} </when>" +
            "<otherwise> o.buyer_id = #{userId} </otherwise>" +
            "</choose>" +
            "<if test=\"status != null\"> AND o.status = #{status} </if>" +
            "</where>" +
            "ORDER BY o.create_time DESC" +
            "</script>")
    List<Order> selectOrders(@Param("userId") Long userId,
                             @Param("role") String role,
                             @Param("status") Integer status);
}