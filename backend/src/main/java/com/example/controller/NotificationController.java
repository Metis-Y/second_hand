package com.example.controller;

import com.example.dto.Result;
import com.example.service.NotificationService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 站内通知接口：列表、未读数、标记已读
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Resource
    private NotificationService notificationService;

    /**
     * 我的通知列表（分页，按下发时间倒序）
     */
    @GetMapping
    public Result getMyNotifications(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        return Result.success(notificationService.getMyNotifications(page, size));
    }

    /**
     * 未读数量，用于个人中心红点
     */
    @GetMapping("/unread-count")
    public Result getUnreadCount() {
        return Result.success(notificationService.getUnreadCount());
    }

    /**
     * 单条标记已读
     */
    @PutMapping("/{id}/read")
    public Result markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return Result.success();
    }

    /**
     * 全部标记已读
     */
    @PutMapping("/read-all")
    public Result markAllRead() {
        notificationService.markAllRead();
        return Result.success();
    }
}