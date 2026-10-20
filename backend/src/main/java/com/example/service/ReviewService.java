package com.example.service;

import com.example.dto.PageResult;
import com.example.entity.Review;

public interface ReviewService {

    /**
     * 分页查询某用户收到的评价（个人主页「收到的评价」）
     *
     * @param userId 被评价人ID
     * @param page   页码，从 1 开始
     * @param size   每页条数
     */
    PageResult<Review> getUserReviews(Long userId, int page, int size);

    /**
     * 发表评价：仅限订单买卖双方，仅限已完成订单，买家先评后卖家才能评
     *
     * @param review 请求体（orderId、score、content），评价人取当前登录用户
     */
    void createReview(Review review);

    /**
     * 分页查询某商品的全部评价（商品详情页评价列表）
     *
     * @param productId 商品ID
     * @param page      页码，从 1 开始
     * @param size      每页条数
     */
    PageResult<Review> getProductReviews(Long productId, int page, int size);
}