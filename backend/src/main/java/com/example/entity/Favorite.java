package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收藏实体，对应表 favorite（用户收藏商品）
 */
@Data
@TableName("favorite")
public class Favorite {

    /**
     * 收藏ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 收藏用户ID
     */
    private Long userId;

    /**
     * 被收藏商品ID
     */
    private Long productId;

    /**
     * 收藏时间（由数据库默认值生成）
     */
    private LocalDateTime createTime;

    /**
     * 商品标题（联表 product 带出，非本表字段）
     */
    @TableField(exist = false)
    private String title;

    /**
     * 商品价格（DECIMAL 对应 BigDecimal，联表带出）
     */
    @TableField(exist = false)
    private BigDecimal price;

    /**
     * 商品封面图片地址（联表带出）
     */
    @TableField(exist = false)
    private String cover;

    /**
     * 商品状态：0下架，1在售，2已售出，3审核中（联表带出）
     */
    @TableField(exist = false)
    private Integer status;

    /**
     * 当前用户是否已收藏（非本表字段；收藏列表里恒为 true）
     */
    @TableField(exist = false)
    private Boolean favorited;

}