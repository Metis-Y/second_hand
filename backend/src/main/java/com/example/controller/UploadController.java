package com.example.controller;

import com.example.dto.Result;
import com.example.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 图片上传接口
 */
@RestController
@RequestMapping("/api")
public class UploadController {

    /** 本地图片目录（来自 application.yml 的 file.upload-dir，尾部可能带 / 也可能不带） */
    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) {
        // 1. 判空
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请上传图片文件");
        }
        // 2. content-type 必须为 image/*
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("仅支持上传图片文件");
        }
        // 3. 大小不能超过 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException("图片大小不能超过 5MB");
        }
        // 4. 根据原文件名取扩展名，随机文件名 = UUID（去横线）+ 扩展名
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null) {
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex >= 0) {
                ext = originalFilename.substring(dotIndex);
            }
        }
        String filename = UUID.randomUUID().toString().replace("-", "") + ext;

        // 5. 保存目录：{uploadDir}/products，不存在则创建
        File dirFile = new File(new File(uploadDir), "products");
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        // 6. 保存文件
        try {
            file.transferTo(new File(dirFile, filename));
        } catch (IOException e) {
            throw new BusinessException("图片上传失败");
        }

        // 7. 返回访问路径（存库用，前导斜杠）
        Map<String, String> map = new HashMap<>();
        map.put("url", "/uploads/products/" + filename);
        return Result.success(map);
    }

}