package com.example.utils;

import cn.hutool.core.util.BooleanUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的互斥锁
 * <p>
 * 加锁：SETNX + 过期时间，一条命令原子完成
 * 解锁：用 Lua 脚本「先比对再删除」，保证判断和删除的原子性，避免误删别人的锁
 */
public class SimpleRedisLock {

    /**
     * 锁的 key 前缀
     */
    private static final String KEY_PREFIX = "second_hand:lock:";

    /**
     * 锁的值前缀：标识「哪个线程持有的锁」
     */
    private static final String ID_PREFIX = "thread:";
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;
    //lua脚本
    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }

    /**
     * 业务名，最终 key = second_hand:lock:{name}
     */
    // 构造器注入
    private final String name;
    private final StringRedisTemplate stringRedisTemplate;
    public SimpleRedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 尝试加锁，不等待，抢不到立即返回
     */
    public boolean tryLock() {
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        Boolean success = stringRedisTemplate.opsForValue() //存入线程id
                .setIfAbsent(KEY_PREFIX + name, threadId, 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(success);
    }

    /**
     * 释放锁：只有锁的值是自己的才删，别人的锁不动
     */
    public void unlock() {
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        stringRedisTemplate.execute(UNLOCK_SCRIPT,  //对比线程id
                Collections.singletonList(KEY_PREFIX + name), threadId);
    }
}