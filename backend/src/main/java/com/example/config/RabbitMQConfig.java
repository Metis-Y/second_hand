package com.example.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置：声明交换机、队列、绑定关系。
 *
 * 消息流转：生产者 → 交换机(exchange) → 按路由键(routingKey) → 队列(queue) → 消费者
 * 这里用 direct 类型交换机：路由键必须完全相等，才会投递到队列。
 *
 * 这三个东西在 RabbitMQ 里会自动创建（不存在时），所以应用第一次启动就会建好，
 * 也可以去 http://192.168.100.130:15672 的 Queues 页面看到。
 */
@Configuration
public class RabbitMQConfig {

    /** 订单交换机：订单相关消息都走它 */
    public static final String ORDER_EXCHANGE = "second_hand.order.exchange";

    /** 下单成功队列 */
    public static final String ORDER_CREATED_QUEUE = "second_hand.order.created.queue";

    /** 下单成功的路由键 */
    public static final String ORDER_CREATED_KEY = "order.created";

    /**
     * 交换机：durable=true 表示 RabbitMQ 重启后交换机还在
     */
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE, true, false);
    }

    /**
     * 队列：durable=true 表示 RabbitMQ 重启后队列和里面的消息还在
     */
    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(ORDER_CREATED_QUEUE, true);
    }

    /**
     * 绑定：把队列接到交换机上，并指定路由键
     */
    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder.bind(orderCreatedQueue()).to(orderExchange()).with(ORDER_CREATED_KEY);
    }

    /**
     * 消息转换器：默认用 JDK 序列化，存进去是一堆二进制看不懂。
     * 换成 Jackson 后消息体是 JSON，管理界面里能直接看到内容，方便调试。
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}