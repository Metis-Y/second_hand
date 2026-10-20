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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private ProductMapper productMapper;

    /**
     * 分页查询订单列表
     */
    @Override
    public PageResult<Order> getOrders(String role, Integer status, int page, int size) {
        Long userId = CurrentHolder.getCurrentId();
        PageHelper.startPage(page, size);
        List<Order> list;
        //按身份买家/卖家查询
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
     * 下单购买商品：非本人发布且在售才可下单，下单后商品置为"已售出"
     */
    @Override
    @Transactional
    public void createOrder(Long productId) {
        Long userId = CurrentHolder.getCurrentId();
        Product product = productMapper.selectById(productId);
        if (userId.equals(product.getUserId())) {
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
        order.setStatus(1); // 买家下单 → 1 交易中
        orderMapper.insert(order);
        product.setStatus(2); // 商品置为已售出，别人不能再下单
        productMapper.updateById(product);
        log.info("下单成功，订单ID：{}", order.getId());
    }

    /**
     * 修改订单状态：卖家确认完成，任一方取消；取消时把商品放回在售
     */
    @Override
    @Transactional
    public void changeStatus(Long orderId, Integer status) {
        Long userId = CurrentHolder.getCurrentId();
        Order order = orderMapper.selectById(orderId);
        boolean b1 = (userId.equals(order.getBuyerId()) || userId.equals(order.getSellerId()));
        // 仅买卖双方能操作，且仅交易中(1)卖家完成(2)任一方取消(3)
        if (!b1||order.getStatus()!=1){
            throw new BusinessException("当前状态不能进行该操作");
        }
        //双方都可以取消
        if (status == 3){
            order.setStatus(status);
            orderMapper.updateById(order);
            // 取消后设置商品状态
            Product product = productMapper.selectById(order.getProductId());
            product.setStatus(1);
            productMapper.updateById(product);
            return ;
        }
        //卖家可以完成
        if (userId.equals(order.getSellerId()) && status == 2){
            order.setStatus(status);
            orderMapper.updateById(order);
        }

        /*if(userId.equals(order.getSellerId())&&status.equals(2)){
            order.setSellerId(userId);
            orderMapper.updateById(order);
            return ;
        }
        if ((userId.equals(order.getBuyerId()) || userId.equals(order.getSellerId())) && status==3) {

            orderMapper.updateById(order);
            // 取消后设置商品状态
            Product product = productMapper.selectById(order.getProductId());
            product.setStatus(1);
            productMapper.updateById(product);
            return ;
        }
        throw new BusinessException("当前状态不能进行该操作");*/

    }
}