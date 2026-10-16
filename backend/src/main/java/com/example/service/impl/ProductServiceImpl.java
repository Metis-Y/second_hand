package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.dto.PageResult;
import com.example.dto.ProductDetailVO;
import com.example.entity.Product;
import com.example.entity.ProductCategory;
import com.example.entity.ProductImage;
import com.example.entity.User;
import com.example.exception.BusinessException;
import com.example.mapper.ProductCategoryMapper;
import com.example.mapper.ProductImageMapper;
import com.example.mapper.ProductMapper;
import com.example.mapper.UserMapper;
import com.example.service.ProductService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.query;

@Service
public class ProductServiceImpl implements ProductService {

    @Resource
    private ProductCategoryMapper productCategoryMapper;

    @Resource
    private ProductMapper productMapper;

    @Resource
    private ProductImageMapper productImageMapper;

    @Resource
    private UserMapper userMapper;

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
     * 分页查询商品列表：PageHelper 分页 + 条件过滤
     */
    @Override
    public PageResult<Product> getProducts(int page, int size, Long categoryId, Integer status) {
        PageHelper.startPage(page, size);
        //分页查询
        List<Product> list = productMapper.selectList( Wrappers.<Product>query()
                .eq(categoryId != null, "category_id", categoryId) //分类
                .eq(status != null, "status", status)
                .orderByDesc("update_time")); //降序
        PageInfo<Product> info = new PageInfo<>(list);
        return new PageResult<Product>(info.getList(), info.getTotal());

    }

    /**
     * 按标题关键字搜索商品（分页）
     */
    @Override
    public PageResult<Product> searchProducts(String keyword, int page, int size, Integer status) {
        PageHelper.startPage(page, size);
        //sql条件构造器
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Product::getTitle, keyword); //模糊匹配
        wrapper.orderByDesc(Product::getUpdateTime); //降序
        List<Product> list = productMapper.selectList(wrapper);
        PageInfo<Product> info = new PageInfo<>(list);

        return new PageResult<>(info.getList(), info.getTotal());
    }

    @Override
    public ProductDetailVO getProductById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) throw new BusinessException("商品不存在");

        ProductDetailVO vo = new ProductDetailVO();
        BeanUtils.copyProperties(product, vo);

        ProductCategory c = productCategoryMapper.selectById(product.getCategoryId());
        if (c != null) vo.setCategoryName(c.getName());

        List<ProductImage> imgs = productImageMapper.selectList(
                Wrappers.<ProductImage>lambdaQuery()
                        .eq(ProductImage::getProductId, id)
                        .orderByAsc(ProductImage::getSort));
        List<String> images = new ArrayList<>();
        if (product.getCover() != null && !product.getCover().isBlank()) {
            images.add(product.getCover());
        }
        for (ProductImage pi : imgs) {
            if (pi.getImageUrl() != null && !images.contains(pi.getImageUrl())) {
                images.add(pi.getImageUrl());
            }
        }
        vo.setImages(images);

        User seller = userMapper.selectById(product.getUserId());
        if (seller != null) {
            vo.setSeller(new ProductDetailVO.Seller(seller.getId(), seller.getNickname(), seller.getAvatar()));
        }

        return vo;
    }

}