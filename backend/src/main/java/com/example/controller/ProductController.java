package com.example.controller;

import com.example.dto.PageResult;
import com.example.dto.Result;
import com.example.entity.Product;
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

    //查询所有商品，按照时间排序
    @GetMapping("/products")
    public Result products(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        PageResult<Product> products = productService.getProducts(page, size, categoryId, status);
        return Result.success(products);

    }

    //按标题关键字搜索商品
    @GetMapping("/products/search")
    public Result searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return Result.success(productService.searchProducts(keyword, page, size, status));
    }
}