package com.example.controller;

import com.example.dto.PageResult;
import com.example.dto.Result;
import com.example.entity.Review;
import com.example.service.ReviewService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评价接口：GET /api/reviews/user/{userId} 查询某用户收到的评价
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Resource
    private ReviewService reviewService;

    //查询某用户收到的评价（个人主页「收到的评价」页签）
    @GetMapping("/user/{userId}")
    public Result getUserReviews(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int size) {
        PageResult<Review> reviews = reviewService.getUserReviews(userId, page, size);
        return Result.success(reviews);
    }
}