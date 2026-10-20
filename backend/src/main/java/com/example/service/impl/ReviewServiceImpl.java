package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.dto.PageResult;
import com.example.entity.Order;
import com.example.entity.Review;
import com.example.exception.BusinessException;
import com.example.mapper.OrderMapper;
import com.example.mapper.ReviewMapper;
import com.example.service.ReviewService;
import com.example.utils.CurrentHolder;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Resource
    private ReviewMapper reviewMapper;

    @Resource
    private OrderMapper orderMapper;

    /**
     * 分页查询某用户收到的评价，联表带出评价人昵称
     */
    @Override
    public PageResult<Review> getUserReviews(Long userId, int page, int size) {
        PageHelper.startPage(page, size);
        List<Review> list = reviewMapper.selectUserReviews(userId);
        PageInfo<Review> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal());
    }

    /**
     * 发表评价：校验订单/权限/状态/重复评价/买家先评，落库
     */
    @Override
    public void createReview(Review review) {
        Long userId = CurrentHolder.getCurrentId();

        // 1. 订单存在
        Order order = orderMapper.selectById(review.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        // 3. 只有已完成的订单能评价
        if (order.getStatus() != 2) {
            throw new BusinessException("订单未完成，不能评价");
        }

        // 4. 同一订单同一人只能评一次
        Long reviewed = reviewMapper.selectCount(
                new LambdaQueryWrapper<Review>()
                        .eq(Review::getOrderId, order.getId())
                        .eq(Review::getReviewerId, userId));
        if (reviewed > 0) {
            throw new BusinessException("该订单已评价过");
        }

        // 5. 买家先评：卖家评价前，买家必须先完成评价
        if (userId.equals(order.getSellerId())) {
            Long buyerReviewed = reviewMapper.selectCount(
                    new LambdaQueryWrapper<Review>()
                            .eq(Review::getOrderId, order.getId())
                            .eq(Review::getReviewerId, order.getBuyerId()));
            if (buyerReviewed == 0) {
                throw new BusinessException("买家评价后才能评价");
            }
        }

        // 落库：评价人=当前用户，被评价人=交易对方
        Review newReview = new Review();
        newReview.setOrderId(order.getId());
        newReview.setProductId(order.getProductId());
        newReview.setReviewerId(userId);
        if (userId.equals(order.getBuyerId())) {
            newReview.setRevieweeId(order.getSellerId());
        } else {
            newReview.setRevieweeId(order.getBuyerId());
        }
        newReview.setScore(review.getScore());
        newReview.setContent(review.getContent());
        reviewMapper.insert(newReview);
    }



}