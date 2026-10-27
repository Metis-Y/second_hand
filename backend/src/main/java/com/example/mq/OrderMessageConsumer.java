package com.example.mq;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.config.RabbitMQConfig;
import com.example.entity.Notification;
import com.example.entity.Order;
import com.example.entity.Product;
import com.example.entity.User;
import com.example.mapper.NotificationMapper;
import com.example.mapper.ProductMapper;
import com.example.mapper.UserMapper;
import com.example.service.NotificationService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 订单消息消费者：收到下单消息后，给卖家发一条站内通知。
 *
 * @RabbitListener 标在方法上：Spring 启动时会为这个队列创建一个监听容器，
 * 队列里有消息就自动回调这个方法，取到的是反序列化后的对象。
 * 方法正常执行完，消息才会被 ack 并从队列删除。
 */
@Slf4j
@Component
public class OrderMessageConsumer {

    /** 通知类型：新订单 */
    private static final Integer TYPE_NEW_ORDER = 1;

    @Resource
    private NotificationService notificationService;
    @Resource
    private NotificationMapper notificationMapper;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private UserMapper userMapper;

    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
    public void onOrderCreated(Order order) {
        // MQ 只保证"至少投递一次"，同一条消息可能来两次。
        // 先按订单ID+类型查一下有没有处理过，避免卖家收到两条一样的通知。
        Long handled = notificationMapper.selectCount(Wrappers.<Notification>lambdaQuery()
                .eq(Notification::getOrderId, order.getId())
                .eq(Notification::getType, TYPE_NEW_ORDER));
        if (handled > 0) {
            log.info("订单{}的下单通知已存在，跳过重复消息", order.getId());
            return;
        }

        Product product = productMapper.selectById(order.getProductId());
        User buyer = userMapper.selectById(order.getBuyerId());
        String buyerName = buyer.getNickname() != null ? buyer.getNickname() : buyer.getUsername();
        String content = buyerName + " 拍下了你的《" + product.getTitle()
                + "》，成交价 " + order.getPrice() + " 元，请尽快确认";

        notificationService.send(order.getSellerId(), TYPE_NEW_ORDER, content, order.getId());
        log.info("已给卖家{}发送下单通知，订单ID={}", order.getSellerId(), order.getId());
    }
}