package com.example.controller;

import com.example.dto.Result;
import com.example.service.FavoriteService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 收藏接口：POST /api/favorites/{productId}、DELETE /api/favorites/{productId}、
 * GET /api/favorites
 */
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Resource
    private FavoriteService favoriteService;

    /**
     * 添加收藏
     */
    @PostMapping("/{productId}")
    public Result addFavorite(@PathVariable Long productId) {
        favoriteService.addFavorite(productId);
        return Result.success();
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/{productId}")
    public Result removeFavorite(@PathVariable Long productId) {
        favoriteService.removeFavorite(productId);
        return Result.success();
    }

    /**
     * 我的收藏列表
     */
    @GetMapping
    public Result getFavorites() {
        return Result.success(favoriteService.getFavorites());
    }

}