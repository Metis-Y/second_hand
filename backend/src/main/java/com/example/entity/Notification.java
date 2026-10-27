package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 站内通知实体，对应表 notification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("notification")
public class Notification {

    /**
     * 通知ID，主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 接收人ID（被通知的人，比如下单通知的接收人是卖家）
     */
    private Long userId;

    /**
     * 通知类型：1新订单，2收到评价
     */
    private Integer type;

    /**
     * 通知内容，直接展示在消息中心
     */
    private String content;

    /**
     * 关联订单ID，方便从通知点进订单
     */
    private Long orderId;

    /**
     * 是否已读：0未读，1已读
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}