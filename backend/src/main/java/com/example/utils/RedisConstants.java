package com.example.utils;

/**
 * Redis key 常量
 * 与黑马点评共用同一个 Redis 实例（只有一个 db0），所有 key 统一加项目前缀区分
 */
public class RedisConstants {

    /**
     * 项目 key 前缀，避免和黑马点评的key冲突
     */
    public static final String KEY_PREFIX = "second_hand:";

    /**
     * 商品分类列表缓存（固定 key，无参数）
     */
    public static final String CACHE_CATEGORY_KEY = KEY_PREFIX + "category:all";

    /**
     * 分类列表缓存有效期（分钟）
     */
    public static final Long CACHE_CATEGORY_TTL = 30L;

    //空值缓存
    public static final Long CACHE_NULL_TTL = 2L;

    /**
     * 商品详情缓存（key = 前缀 + 商品id）
     */
    public static final String CACHE_PRODUCT_KEY = KEY_PREFIX + "product:";

    /**
     * 商品详情缓存有效期（分钟）
     */
    public static final Long CACHE_PRODUCT_TTL = 30L;

    /**
     * 缓存重建互斥锁（key = 前缀 + 数据id）：商品、用户等所有缓存共用
     */
    public static final String LOCK_CACHE_KEY = KEY_PREFIX + "lock:cache:";

    /**
     * 互斥锁有效期（秒）：防止持锁线程异常退出导致死锁
     */
    public static final Long LOCK_TTL = 10L;

    //用户的收藏列表缓存（key = 前缀 + 用户id）
    public static final String FAVORITE_USER_KEY = KEY_PREFIX + "favorite:user:";

    //用户信息缓存
    public static final String CACHE_USER_KEY = KEY_PREFIX + "user:";
    public static final Long CACHE_USER_TTL = 30L;
}