package com.example.service.impl;

import com.example.dto.PageResult;
import com.example.entity.Review;
import com.example.mapper.ReviewMapper;
import com.example.service.ReviewService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Resource
    private ReviewMapper reviewMapper;

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



}