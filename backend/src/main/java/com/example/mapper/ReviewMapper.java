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
     * 联表查询某用户收到的评价：review JOIN users（评价人）JOIN product（商品标题/封面），
     * 按 reviewee_id 筛选，评价时间倒序。
     *
     * @param revieweeId 被评价人ID
     */
    @Select("SELECT r.id, r.order_id, r.product_id, r.reviewer_id, r.reviewee_id, r.score, r.content, r.create_time, " +
            "u.nickname AS reviewer_nickname, u.avatar AS reviewer_avatar, " +
            "p.title AS product_title, p.cover AS product_cover " +
            "FROM review r " +
            "JOIN users u ON r.reviewer_id = u.id " +
            "JOIN product p ON r.product_id = p.id " +
            "WHERE r.reviewee_id = #{revieweeId} " +
            "ORDER BY r.create_time DESC")
    List<Review> selectUserReviews(@Param("revieweeId") Long revieweeId);

    /**
     * 联表查询某商品的全部评价：review JOIN users，
     * 按 product_id 筛选，评价时间倒序。
     *
     * @param productId 商品ID
     *  reviewer 评价人
     */
    @Select("SELECT r.id, r.order_id, r.product_id, r.reviewer_id, r.reviewee_id, r.score, r.content, r.create_time, " +
            "u.nickname AS reviewer_nickname, u.avatar AS reviewer_avatar " +
            "FROM review r " +
            "JOIN users u ON r.reviewer_id = u.id " +
            "WHERE r.product_id = #{productId} " +
            "ORDER BY r.create_time ")
    List<Review> selectProductReviews(@Param("productId") Long productId);
}