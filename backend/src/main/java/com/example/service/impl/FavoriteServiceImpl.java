package com.example.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.dto.PageResult;
import com.example.entity.Favorite;
import com.example.exception.BusinessException;
import com.example.mapper.FavoriteMapper;
import com.example.mapper.ProductMapper;
import com.example.service.FavoriteService;
import com.example.utils.CurrentHolder;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import static com.example.utils.RedisConstants.FAVORITE_USER_KEY;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Resource
    private FavoriteMapper favoriteMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ProductMapper productMapper;

    @Override
    @Transactional
    public void addFavorite(Long productId) {
        Long userId = CurrentHolder.getCurrentId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        if (productMapper.selectById(productId) == null) {
            throw new BusinessException("商品不存在");
        }
        /*//判断有没有收藏
        Long count = favoriteMapper.selectCount(Wrappers.<Favorite>lambdaQuery()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));
        if (count == null || count == 0) {
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setProductId(productId);
            favoriteMapper.insert(favorite);
        }*/

        //判断有没有收藏
        Long count = favoriteMapper.selectCount(Wrappers.<Favorite>lambdaQuery()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));
        if (count == null || count == 0) {
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setProductId(productId);
            favoriteMapper.insert(favorite);
        }
        // SADD 放在 if 外面：已收藏时也补写一次，Redis 数据丢了能自愈
        stringRedisTemplate.opsForSet().add(FAVORITE_USER_KEY + userId, String.valueOf(productId));
    }

    //移除收藏
    @Override
    @Transactional
    public void removeFavorite(Long productId) {
        Long userId = CurrentHolder.getCurrentId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        int isDeleted = favoriteMapper.delete(Wrappers.<Favorite>lambdaQuery()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));
        if (isDeleted != 1) {
            throw new BusinessException("取消收藏失败");
        }
        stringRedisTemplate.opsForSet().remove(FAVORITE_USER_KEY + userId, String.valueOf(productId));
    }

    //获取收藏列表（联表 favorite JOIN product，带出商品标题/价格/封面/状态）
    @Override
    public PageResult<Favorite> getFavorites() {
        Long userId = CurrentHolder.getCurrentId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        List<Favorite> list = favoriteMapper.selectFavoritesWithProduct(userId);
        // 收藏列表里的每一项自然都是已收藏
        for (Favorite f : list) {
            f.setFavorited(true);
        }
        return new PageResult<>(list, list.size());
    }

    /**
     * 获取当前登录用户 id，未登录时抛业务异常
     */

}