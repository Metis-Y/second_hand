package com.example.service;

import com.example.dto.PageResult;
import com.example.entity.Notification;

public interface NotificationService {

    /**
     * 我的通知列表（当前登录人收到的，按下发时间倒序分页）
     */
    PageResult<Notification> getMyNotifications(int page, int size);

    /**
     * 未读通知数量（个人中心红点用）
     */
    Long getUnreadCount();

    /**
     * 把一条通知标记为已读（只能操作自己的通知）
     */
    void markRead(Long id);

    /**
     * 全部标记为已读
     */
    void markAllRead();

    /**
     * 发一条通知给指定用户（供 MQ 消费者调用）
     */
    void send(Long userId, Integer type, String content, Long orderId);
}