package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品图片实体，对应表 product_image
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("product_image")
public class ProductImage {

    /**
     * 图片ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属商品ID
     */
    private Long productId;

    /**
     * 图片地址
     */
    private String imageUrl;

    /**
     * 图片显示顺序，越小越靠前
     */
    private Integer sort;

}