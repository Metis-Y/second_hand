package com.example.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具（jjwt 0.12.x 写法）
 * secret 由 application.yml 的 jwt.secret 注入，expireHours 由 jwt.expire-hours 注入
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expire-hours}")
    private long expireHours;

    /** 根据 secret 生成 HMAC-SHA256 的密钥（要求 secret 至少 32 字节） */
    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 token：subject 存用户名，claim("uid") 存用户 id
     */
    public String generateToken(Long userId, String username) {
        return Jwts.builder()
                .subject(username)
                .claim("uid", String.valueOf(userId))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+expireHours*60*60*1000)) //24小时
                .signWith(key())
                .compact();
    }

    /**
     * 解析 token（拦截器校验登录用）
     */
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}