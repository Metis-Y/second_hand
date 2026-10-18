package com.example.service;

import com.example.dto.PageResult;
import com.example.entity.Favorite;

public interface FavoriteService {

    /**
     * 添加收藏（幂等：重复收藏不报错）
     */
    void addFavorite(Long productId);

    /**
     * 取消收藏（商品不存在的收藏删除 0 行也算成功）
     */
    void removeFavorite(Long productId);

    /**
     * 我的收藏列表（收藏记录列表 + 总数，含联表商品字段）
     */
    PageResult<Favorite> getFavorites();

}