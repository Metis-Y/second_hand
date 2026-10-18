package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体，对应表 orders
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("orders")
public class Order {

    /**
     * 订单ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 买家ID
     */
    private Long buyerId;

    /**
     * 卖家ID
     */
    private Long sellerId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 实际成交价格（DECIMAL 对应 BigDecimal）
     */
    private BigDecimal price;

    /**
     * 订单状态：0待确认，1交易中，2已完成，3已取消
     */
    private Integer status;

    /**
     * 下单时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 买家昵称（联表 users 带出，非本表字段）
     */
    @TableField(exist = false)
    private String buyerNickname;

    /**
     * 卖家昵称（联表 users 带出，非本表字段）
     */
    @TableField(exist = false)
    private String sellerNickname;

    /**
     * 商品标题（联表 product 带出，非本表字段）
     */
    @TableField(exist = false)
    private String productTitle;
}