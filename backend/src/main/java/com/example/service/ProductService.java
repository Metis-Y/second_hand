package com.example.service;

import com.example.dto.PageResult;
import com.example.entity.ProductCategory;

import java.util.List;

public interface ProductService {


    List<ProductCategory> getProductCategories();

    /**
     * 分页查询商品列表
     *
     * @param page       页码（从 1 开始）
     * @param size       每页条数
     * @param keyword    标题关键字，可空
     * @param categoryId 分类ID，可空
     * @param status     商品状态，可空
     */
    PageResult getProducts(int page, int size, String keyword, Long categoryId, Integer status);
}