package com.example.controller;

import com.example.dto.Result;
import com.example.entity.Order;
import com.example.service.OrderService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单接口：GET /api/orders 查询订单列表、POST /api/orders 下单
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Resource
    private OrderService orderService;

    /**
     * 查询订单列表（身份 + 状态筛选）
     *
     * @param role   buyer 我买到的 / seller 我卖出的，默认 buyer
     * @param status 订单状态：0待确认/1交易中/2已完成/3已取消，空表示全部
     */
    @GetMapping
    public Result getOrders(@RequestParam(defaultValue = "buyer") String role,
                            @RequestParam(required = false) Integer status,
                            @RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "10") int size) {
        return Result.success(orderService.getOrders(role, status, page, size));
    }

    /**
     * 下单购买商品
     *
     * @param req 只取 productId（要购买的商品ID）
     */
    @PostMapping
    public Result createOrder(@RequestBody Order req) {
        return Result.success(orderService.createOrder(req.getProductId()));
    }
}