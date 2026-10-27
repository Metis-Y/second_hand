package com.example.mq;

import com.example.config.RabbitMQConfig;
import com.example.entity.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 订单消息消费者。
 *
 * @RabbitListener 标在方法上：Spring 启动时会为这个队列创建一个监听容器，
 * 队列里有消息就自动回调这个方法，取到的是反序列化后的对象。
 *
 * 第一步只打印日志，用来验证「下单 → 发消息 → 收到」这条链路是通的。
 */
@Slf4j
@Component
public class OrderMessageConsumer {

    @RabbitListener(queues = RabbitMQConfig.ORDER_CREATED_QUEUE)
    public void onOrderCreated(Order order) {
        log.info("收到下单消息：订单ID={}, 商品ID={}, 买家ID={}, 卖家ID={}, 金额={}",
                order.getId(), order.getProductId(), order.getBuyerId(),
                order.getSellerId(), order.getPrice());
        // 下一步在这里写通知表：给卖家插一条「有人下单了」的消息
    }
}