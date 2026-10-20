package com.example.controller;

import com.example.dto.PageResult;
import com.example.dto.Result;
import com.example.entity.Review;
import com.example.service.ReviewService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评价接口：POST /api/reviews 发表评价；GET /api/reviews/user/{userId} 查询某用户收到的评价
 */
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Resource
    private ReviewService reviewService;

    //发表评价（对已完成订单，买家先评后卖家才能评）
    @PostMapping
    public Result createReview(@RequestBody Review review) {
        reviewService.createReview(review);
        return Result.success();
    }

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