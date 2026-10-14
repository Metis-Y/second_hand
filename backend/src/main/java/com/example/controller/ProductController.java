package com.example.controller;

import com.example.dto.Result;
import com.example.entity.ProductCategory;
import com.example.service.ProductService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    @Resource
    private ProductService productService;

    @GetMapping("/categories")
    public Result productCategories(){
        List<ProductCategory> products=productService.getProductCategories();
        return Result.success(products);
    }


}
