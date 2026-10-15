package com.example.controller;

import com.example.dto.Result;
import com.example.entity.ProductCategory;
import com.example.service.ProductService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    @Resource
    private ProductService productService;

    //商品分类
    @GetMapping("/categories")
    public Result productCategories(){
        List<ProductCategory> products=productService.getProductCategories();
        return Result.success(products);
    }

    //查询商品
    @GetMapping("/products")
    public Result products(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {

        return Result.success(productService.getProducts(page, size, keyword, categoryId, status));

    }

}