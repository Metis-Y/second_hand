package com.example;

import com.example.entity.Favorite;
import com.example.mapper.FavoriteMapper;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.utils.RedisConstants.FAVORITE_USER_KEY;

@SpringBootTest
public class FavoriteCacheTest {
    @Resource
    private FavoriteMapper favoriteMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void preheat() {
        // 1. 查全表

        List<Favorite> all = favoriteMapper.selectList(null);

        // 2. 按 userId 分组
        Map<Long, List<Long>> byUser = all.stream().collect(
                Collectors.groupingBy(Favorite::getUserId,
                        Collectors.mapping(Favorite::getProductId, Collectors.toList())));

        // 3. 每个用户批量 SADD
        byUser.forEach((userId, productIds) -> {
            String[] members = productIds.stream().map(String::valueOf).toArray(String[]::new);
            stringRedisTemplate.opsForSet().add(FAVORITE_USER_KEY + userId, members);
        });

    }
}
