package com.example.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.example.utils.RedisConstants.CACHE_NULL_TTL;
import static com.example.utils.RedisConstants.LOCK_CACHE_KEY;
import static com.example.utils.RedisConstants.LOCK_TTL;

/**
 * Redis 缓存工具类
 * 序列化统一用 Hutool 的 JSONUtil：写和读是同一套规则，格式天然自洽，
 * 不会出现「一种序列化器写、另一种读不出来」的问题
 */
@Slf4j
@Component
public class CacheClient {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 构造器注入：由 Spring 容器传入 StringRedisTemplate
     */
    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 写缓存：对象序列化成 JSON 字符串后写入，并设置过期时间
     */
    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    /**
     * 删除缓存：数据库写入后调用，下次请求自动回查重建（Cache-Aside 模式）
     */
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    /**
     * 缓存固定 key 的列表数据（集合缓存）
     * 命中直接返回；未命中回查数据库并写入缓存
     *
     * @param key        完整 key（含前缀）
     * @param type       列表元素类型
     * @param time       过期时间
     * @param unit       过期时间单位
     * @param dbFallback 回查数据库的方法
     */
    public <R> List<R> queryListWithPassThrough(String key, Class<R> type, Long time, TimeUnit unit, Supplier<List<R>> dbFallback) {
        String json = stringRedisTemplate.opsForValue().get(key);
        // 命中缓存，反序列化成列表
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toList(json, type);
        }
        // 未命中，回查数据库
        List<R> list = dbFallback.get();
        set(key, list, time, unit);
        return list;
    }

    /**
     * 按 id 查询单条数据：互斥锁重建（防击穿）+ 空值缓存（防穿透）
     *
     * @param keyPrefix  缓存 key 前缀（不含 id）
     * @param id         数据 id
     * @param type       返回类型
     * @param time       正常数据的缓存时间
     * @param unit       时间单位
     * @param dbFallback 回查数据库的方法
     * @return 查询结果，数据库也没有时返回 null
     */
    public <R, ID> R queryWithMutex(String keyPrefix, ID id, Class<R> type, Long time, TimeUnit unit, Function<ID, R> dbFallback) {
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        // 命中真实数据
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toBean(json, type);
        }
        // 命中空值缓存，说明数据库也没有
        if (json != null) {
            return null;
        }

        String lockKey = LOCK_CACHE_KEY + id;
        try {
            // 没抢到锁：说明别人正在重建，等一会儿再重试
            if (!tryLock(lockKey)) {
                Thread.sleep(50);
                return queryWithMutex(keyPrefix, id, type, time, unit, dbFallback);
            }
            // 抢到锁后二次检查：可能别的线程已经重建完成
            json = stringRedisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(json)) {
                return JSONUtil.toBean(json, type);
            }
            if (json != null) {
                return null;
            }
            // 回查数据库
            R r = dbFallback.apply(id);
            if (r == null) {
                // 数据库也没有，缓存空串（短 TTL）防止穿透
                stringRedisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL, TimeUnit.MINUTES);
                return null;
            }
            set(key, r, time, unit);
            return r;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 只有真正持锁的线程才会走到这，不会误删别人的锁
            unlock(lockKey);
        }

    }

    /**
     * 抢锁：SETNX + 过期时间，一条命令原子完成
     */
    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", LOCK_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    /**
     * 释放锁
     */
    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
}