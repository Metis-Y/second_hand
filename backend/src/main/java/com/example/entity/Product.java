package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("product")
public class Product {

    /**
     * 商品ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 发布用户ID
     */
    private Long userId;

    /**
     * 商品分类ID
     */
    private Long categoryId;

    /**
     * 商品标题
     */
    private String title;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 商品价格（DECIMAL 对应 BigDecimal）
     */
    private BigDecimal price;

    /**
     * 商品封面图片地址
     */
    private String cover;

    /**
     * 商品状态：0下架，1在售，2已售出，3审核中
     */
    private Integer status;

    /**
     * 发布时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}