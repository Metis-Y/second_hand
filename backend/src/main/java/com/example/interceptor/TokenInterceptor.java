package com.example.interceptor;

import cn.hutool.json.JSONUtil;
import com.example.dto.Result;
import com.example.utils.CurrentHolder;
import com.example.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录校验拦截器：校验请求头 Authorization: Bearer <token> 里的 JWT。
 * 注册、登录两个公开接口在 WebConfig 里已排除，不拦截；
 * 其余接口 token 缺失 / 格式错误 / 无效 / 过期，统一返回 HTTP 401。
 */
@Slf4j
@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 取请求头里的令牌
        String authHeader = request.getHeader("Authorization");

        // 2. 令牌为空或格式不对，返回 401
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.info("令牌为空或格式错误, 返回 401");
            return unauthorized(response);
        }

        // 3. 解析 token，失败说明无效/过期，返回 401
        try {
            String token = authHeader.substring(7); // 去掉 "Bearer " 前缀
            Claims claims = jwtUtil.parseClaims(token);
            Long userId = Long.valueOf(claims.get("uid").toString());
            CurrentHolder.setCurrentId(userId);
        } catch (Exception e) {
            log.info("解析令牌失败, 返回 401");
            return unauthorized(response);
        }

        // 4. 令牌合法，放行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求结束，移除 ThreadLocal，防止内存泄漏
        CurrentHolder.remove();
    }

    /** 返回未登录错误（HTTP 401 + JSON 响应体），并阻止放行 */
    private boolean unauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSONUtil.toJsonStr(Result.error("未登录或登录已过期")));
        return false;
    }
    
}