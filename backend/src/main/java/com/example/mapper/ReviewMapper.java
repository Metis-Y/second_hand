package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.Review;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 评价 Mapper
 */
public interface ReviewMapper extends BaseMapper<Review> {

    /**
     * 联表查询某用户收到的评价：review JOIN users（评价人），
     * 按 reviewee_id 筛选，评价时间倒序。
     *
     * @param revieweeId 被评价人ID
     */
    @Select("SELECT r.id, r.order_id, r.reviewer_id, r.reviewee_id, r.score, r.content, r.create_time, " +
            "u.nickname AS reviewer_nickname " +
            "FROM review r " +
            "JOIN users u ON r.reviewer_id = u.id " +
            "WHERE r.reviewee_id = #{revieweeId} " +
            "ORDER BY r.create_time DESC")
    List<Review> selectUserReviews(@Param("revieweeId") Long revieweeId);
}