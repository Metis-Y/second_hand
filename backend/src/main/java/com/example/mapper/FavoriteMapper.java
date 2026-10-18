package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.Favorite;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 收藏 Mapper
 */
public interface FavoriteMapper extends BaseMapper<Favorite> {

    /**
     * 联表查询收藏列表：favorite JOIN product，把商品的标题/价格/封面/状态带出来
     *
     * @param userId 当前用户ID
     * @return 收藏记录（含商品字段，按收藏时间倒序）
     */
    @Select("SELECT f.id, f.user_id, f.product_id, f.create_time, " +
            "p.title, p.price, p.cover, p.status " +
            "FROM favorite f JOIN product p ON f.product_id = p.id " +
            "WHERE f.user_id = #{userId} " +
            "ORDER BY f.create_time DESC")
    List<Favorite> selectFavoritesWithProduct(@Param("userId") Long userId);
}