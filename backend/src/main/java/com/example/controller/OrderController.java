package com.example.controller;

import com.example.dto.Result;
import com.example.service.OrderService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

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
     * @param status 订单状态：1交易中/2已完成/3已取消，空表示全部
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
     * @param productId 要购买的商品ID（URL 路径参数）
     */
    @PostMapping("/{productId}")
    public Result createOrder(@PathVariable Long productId) {
        orderService.createOrder(productId);
        return Result.success();
    }

    /**
     * 修改订单状态（卖家确认完成 / 任一方取消）
     *
     * @param id     订单ID（URL 路径参数）
     * @param status 目标状态：2已完成 / 3已取消（URL 路径参数）
     */
    @PutMapping("/{id}/{status}")
    public Result changeStatus(@PathVariable Long id, @PathVariable Integer status) {
        orderService.changeStatus(id, status);
        return Result.success();
    }
}