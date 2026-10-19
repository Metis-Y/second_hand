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
}