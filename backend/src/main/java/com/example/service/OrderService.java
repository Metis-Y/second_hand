package com.example.service;

import com.example.dto.PageResult;
import com.example.entity.Order;

public interface OrderService {

    /**
     * 分页查询当前用户的订单列表（按身份 + 状态筛选）
     *
     * @param role   buyer 查我买到的，seller 查我卖出的
     * @param status 订单状态：0待确认/1交易中/2已完成/3已取消，null 表示全部
     * @param page   页码，从 1 开始
     * @param size   每页条数
     */
    PageResult<Order> getOrders(String role, Integer status, int page, int size);

    /**
     * 下单购买商品：校验后生成"待确认"订单，并把商品置为"已售出"
     *
     * @param productId 要购买的商品ID
     * @return 新建的订单
     */
    Order createOrder(Long productId);
}