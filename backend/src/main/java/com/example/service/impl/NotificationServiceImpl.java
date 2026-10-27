package com.example.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.dto.PageResult;
import com.example.entity.Notification;
import com.example.mapper.NotificationMapper;
import com.example.service.NotificationService;
import com.example.utils.CurrentHolder;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Resource
    private NotificationMapper notificationMapper;

    @Override
    public PageResult<Notification> getMyNotifications(int page, int size) {
        Long userId = CurrentHolder.getCurrentId();
        PageHelper.startPage(page, size);
        List<Notification> list = notificationMapper.selectList(Wrappers.<Notification>lambdaQuery()
                .eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreateTime));
        PageInfo<Notification> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal());
    }

    @Override
    public Long getUnreadCount() {
        Long userId = CurrentHolder.getCurrentId();
        return notificationMapper.selectCount(Wrappers.<Notification>lambdaQuery()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
    }

    @Override
    public void markRead(Long id) {
        // 条件里带上 user_id，避免点到别人的通知也能改
        notificationMapper.update(null, Wrappers.<Notification>lambdaUpdate()
                .eq(Notification::getId, id)
                .eq(Notification::getUserId, CurrentHolder.getCurrentId())
                .set(Notification::getIsRead, 1));
    }

    @Override
    public void markAllRead() {
        notificationMapper.update(null, Wrappers.<Notification>lambdaUpdate()
                .eq(Notification::getUserId, CurrentHolder.getCurrentId())
                .eq(Notification::getIsRead, 0)
                .set(Notification::getIsRead, 1));
    }

    @Override
    public void send(Long userId, Integer type, String content, Long orderId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setContent(content);
        notification.setOrderId(orderId);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }
}