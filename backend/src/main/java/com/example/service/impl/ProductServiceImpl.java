package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.dto.PageResult;
import com.example.entity.Favorite;
import com.example.entity.Product;
import com.example.entity.ProductCategory;
import com.example.entity.ProductImage;
import com.example.entity.User;
import com.example.exception.BusinessException;
import com.example.mapper.FavoriteMapper;
import com.example.mapper.ProductCategoryMapper;
import com.example.mapper.ProductImageMapper;
import com.example.mapper.ProductMapper;
import com.example.mapper.UserMapper;
import com.example.service.ProductService;
import com.example.utils.CurrentHolder;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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

    @Resource
    private FavoriteMapper favoriteMapper;

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
     * 分页查询商品列表：PageHelper 分页 + 条件过滤，返回 Product
     */
    @Override
    @Transactional
    public PageResult<Product> getProducts(int page, int size, Long categoryId, Integer status) {
        PageHelper.startPage(page, size);
        List<Product> list = productMapper.selectList(Wrappers.<Product>query()
                .eq(categoryId != null, "category_id", categoryId) //分类
                .eq(status != null, "status", status)
                .orderByDesc("update_time")); //降序

        //补充卖家昵称 + 当前用户是否已收藏
        return getProductPageResult(list);
    }

    /**
     * 按标题关键字搜索商品（分页），返回 Product
     */
    @Override
    @Transactional
    public PageResult<Product> searchProducts(String keyword, int page, int size, Integer status) {
        PageHelper.startPage(page, size);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(Product::getTitle, keyword); //模糊匹配
        wrapper.orderByDesc(Product::getUpdateTime); //降序
        List<Product> list = productMapper.selectList(wrapper);
        //补充卖家昵称 + 当前用户是否已收藏
        return getProductPageResult(list);
    }

    /**
     * 根据 id 获取商品详情：单独查询分类名、卖家昵称/头像、图片（不联表）
     */
    @Override
    public Product getProductById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 分类名
        ProductCategory category = productCategoryMapper.selectById(product.getCategoryId());
        product.setCategoryName(category.getName());

        // 卖家信息
        User user = userMapper.selectById(product.getUserId());
        if (user != null) {
            product.setSellerNickname(user.getNickname());
            product.setSellerAvatar(user.getAvatar());
        }

        // 是否已收藏（详情页直接用，免去前端再单独调一次查收藏接口）
        Long currentId = CurrentHolder.getCurrentId();
        boolean favorited = false;
        Long cnt = favoriteMapper.selectCount(Wrappers.<Favorite>lambdaQuery()
                    .eq(Favorite::getUserId, currentId).eq(Favorite::getProductId, id));
        favorited = cnt != null && cnt > 0;

        product.setFavorited(favorited);

        // 图片：封面优先 + product_image 按 sort 升序去重
        List<ProductImage> imgs = productImageMapper.selectList(
                Wrappers.<ProductImage>lambdaQuery()
                        .eq(ProductImage::getProductId, id)
                        .orderByAsc(ProductImage::getSort));
        //数组图片
        List<String> images = new ArrayList<>();
        for (ProductImage pi : imgs) {
            if (pi.getImageUrl() != null ) {
                images.add(pi.getImageUrl());
            }
        }
        product.setImages(images);
        return product;
    }

    /**
     * 发布商品：入参直接用 Product（前端只传标题/分类/价格/描述/图片），
     * 服务端重设 userId、封面、状态并批量写入多张图片。
     */
    @Override
    @Transactional
    public Long createProduct(Product req) {
        Long userId = CurrentHolder.getCurrentId();

        Product product = new Product();
        product.setUserId(userId);
        product.setCategoryId(req.getCategoryId());
        product.setTitle(req.getTitle());
        product.setDescription(req.getDescription());
        product.setPrice(req.getPrice());
        // 第一张作为封面
        product.setCover(req.getImages().get(0));
        // 1=在售
        product.setStatus(1);
        productMapper.insert(product);

        List<String> images = req.getImages();
        for (int i = 0; i < images.size(); i++) {
            ProductImage image = new ProductImage();
            image.setProductId(product.getId());
            image.setImageUrl(images.get(i));
            image.setSort(i);
            productImageMapper.insert(image);
        }
        //方便跳转到个人主页
        return product.getId();
    }

    // 抽出公共方法：补充卖家昵称 + 当前用户是否已收藏
    private PageResult<Product> getProductPageResult(List<Product> list) {
        for (Product product : list){
            User user = userMapper.selectById(product.getUserId());
            product.setSellerNickname(user.getNickname());
            product.setSellerAvatar(user.getAvatar());
            long count = favoriteMapper.selectCount(Wrappers.<Favorite>lambdaQuery()
                    .eq(Favorite::getUserId, CurrentHolder.getCurrentId())
                    .eq(Favorite::getProductId, product.getId()));
            product.setFavorited(count > 0);
        }

        PageInfo<Product> info = new PageInfo<>(list);
        return new PageResult<>(info.getList(), info.getTotal());
    }
}