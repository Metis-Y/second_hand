package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评价实体，对应表 review
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("review")
public class Review {

    /**
     * 评价ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属订单ID
     */
    private Long orderId;

    /**
     * 所属商品ID
     */
    private Long productId;

    /**
     * 评价人ID
     */
    private Long reviewerId;

    /**
     * 被评价人ID
     */
    private Long revieweeId;

    /**
     * 评分，1-5 分
     */
    private Integer score;

    /**
     * 评价内容，可为空
     */
    private String content;

    /**
     * 评价时间
     */
    private LocalDateTime createTime;

    /**
     * 评价人昵称（联表 users 带出，非本表字段）
     */
    @TableField(exist = false)
    private String reviewerNickname;
}