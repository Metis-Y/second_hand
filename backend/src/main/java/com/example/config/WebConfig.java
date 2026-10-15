package com.example.config;

import com.example.interceptor.TokenInterceptor;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置：注册登录校验拦截器 + 静态图片资源映射。
 * 除注册、登录两个公开接口和静态图片外，其余接口都需要带 token 才能访问。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Resource
    private TokenInterceptor tokenInterceptor;

    /** 本地图片目录（来自 application.yml 的 file.upload-dir） */
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tokenInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/auth/register",
                        "/api/auth/login",
                        // 图片通过 <img> 加载时不带 token，必须放行，否则图片会被 401 拦下
                        "/uploads/**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 把 /uploads/** 请求映射到本地 uploads 目录：/uploads/book.png -> {uploadDir}/book.png
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///" + uploadDir);
    }
}