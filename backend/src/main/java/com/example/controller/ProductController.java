package com.example.controller;

import com.example.dto.PageResult;
import com.example.dto.Result;
import com.example.entity.Product;
import com.example.entity.ProductCategory;
import com.example.service.ProductService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
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
    @GetMapping()
    public Result getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        PageResult<Product> products = productService.getProducts(page, size, categoryId, status);
        return Result.success(products);
    }

    //按标题关键字搜索商品
    @GetMapping("/search")
    public Result searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return Result.success(productService.searchProducts(keyword, page, size, status));
    }

    //根据商品id查询
    @GetMapping("/{id}")
    public Result getProductById(@PathVariable Long id) {
        return Result.success(productService.getProductById(id));
    }

    //发布商品
    @PostMapping
    public Result createProduct(@RequestBody Product req) {
        return Result.success(productService.createProduct(req));
    }

}