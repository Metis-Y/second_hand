package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.dto.PageResult;
import com.example.entity.Product;
import com.example.entity.ProductCategory;
import com.example.mapper.ProductCategoryMapper;
import com.example.mapper.ProductMapper;
import com.example.service.ProductService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Resource
    private ProductCategoryMapper productCategoryMapper;

    @Resource
    private ProductMapper productMapper;

    /**
     * 查询全部分类，按 sort 升序（sort 相同时按 id 升序，保证顺序稳定）
     */
    @Override
    public List<ProductCategory> getProductCategories() {
        return productCategoryMapper.selectList(
                Wrappers.<ProductCategory>lambdaQuery()
                        .orderByAsc(ProductCategory::getSort)
                        .orderByAsc(ProductCategory::getId)
        );
    }

    /**
     * 分页查询商品列表：PageHelper 分页 + LambdaQueryWrapper 条件过滤
     */
    @Override
    public PageResult getProducts(int page, int size, String keyword, Long categoryId, Integer status) {
        PageHelper.startPage(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Product::getTitle, keyword);
        }
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (status != null) {
            wrapper.eq(Product::getStatus, status);
        }
        wrapper.orderByDesc(Product::getId);
        List<Product> list = productMapper.selectList(wrapper);
        PageInfo<Product> info = new PageInfo<>(list);
        return new PageResult(info.getList(), info.getTotal());
    }
}