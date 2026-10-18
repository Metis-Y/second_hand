package com.example.service.impl;

import com.example.dto.PageResult;
import com.example.entity.Order;
import com.example.entity.Product;
import com.example.exception.BusinessException;
import com.example.mapper.OrderMapper;
import com.example.mapper.ProductMapper;
import com.example.service.OrderService;
import com.example.utils.CurrentHolder;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private ProductMapper productMapper;

    /**
     * 分页查询订单列表：按身份（买家/卖家）+ 状态筛选，联表带出昵称与商品标题
     */
    @Override
    public PageResult<Order> getOrders(String role, Integer status, int page, int size) {
        Long userId = CurrentHolder.getCurrentId();
        PageHelper.startPage(page, size);
        List<Order> list;
        if ("seller".equals(role)) {
            list = orderMapper.selectOrders(userId, role, status);
        } else {
            role = "buyer";
            list = orderMapper.selectOrders(userId, role, status);
        }
        PageInfo<Order> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal());
    }

    /**
     * 下单购买商品：校验商品在售且非本人发布，落单后把商品置为已售出
     */
    @Override
    @Transactional
    public Order createOrder(Long productId) {
        Long userId = CurrentHolder.getCurrentId();
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (product.getUserId().equals(userId)) {
            throw new BusinessException("不能购买自己发布的商品");
        }
        if (product.getStatus() != 1) {
            throw new BusinessException("该商品当前不可购买");
        }
        Order order = new Order();
        order.setBuyerId(userId);
        order.setSellerId(product.getUserId());
        order.setProductId(productId);
        order.setPrice(product.getPrice());
        order.setStatus(0);
        orderMapper.insert(order);

        // 下单成功，商品置为"已售出"
        product.setStatus(2);
        productMapper.updateById(product);

        return orderMapper.selectById(order.getId());
    }
}