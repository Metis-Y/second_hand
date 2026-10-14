package com.example.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.entity.ProductCategory;
import com.example.mapper.ProductCategoryMapper;
import com.example.service.ProductService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Resource
    private ProductCategoryMapper productCategoryMapper;

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
}