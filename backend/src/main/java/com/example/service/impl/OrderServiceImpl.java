package com.example.service.impl;

import com.example.config.RabbitMQConfig;
import com.example.dto.PageResult;
import com.example.entity.Order;
import com.example.entity.Product;
import com.example.exception.BusinessException;
import com.example.mapper.OrderMapper;
import com.example.mapper.ProductMapper;
import com.example.service.OrderService;
import com.example.utils.CacheClient;
import com.example.utils.CurrentHolder;
import com.example.utils.SimpleRedisLock;
import org.springframework.aop.framework.AopContext;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.utils.RedisConstants.CACHE_PRODUCT_KEY;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private OrderMapper orderMapper;
    @Resource
    private CacheClient cacheClient;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 分页查询订单列表
     */
    @Override
    public PageResult<Order> getOrders(String role, Integer status, int page, int size) {
        Long userId = CurrentHolder.getCurrentId();
        PageHelper.startPage(page, size);
        List<Order> list;
        //按身份买家/卖家查询
        if ("seller".equals(role)) {
            list = orderMapper.selectOrders(userId, role, status);
        } else {
            role = "buyer";
            list = orderMapper.selectOrders(userId, role, status);
        }
        PageInfo<Order> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getList(), pageInfo.getTotal());
    }

    /**
     * 下单购买商品：非本人发布且在售才可下单，下单后商品置为"已售出"
     */
    @Override
    // 事务不能加在锁的外面
    //@Transactional
    public void createOrder(Long productId) {
        Long userId = CurrentHolder.getCurrentId();
        Product product = productMapper.selectById(productId);
        if (product == null){
            throw new BusinessException("商品已被删除");
        }
        if (userId.equals(product.getUserId())) {
            throw new BusinessException("不能购买自己发布的商品");
        }
        if (product.getStatus() != 1) {
            throw new BusinessException("该商品当前不可购买");
        }

        /**
         * 使用redis实现防止超卖
         * */
        //获取互斥锁
        SimpleRedisLock lock = new SimpleRedisLock("order:" + productId, stringRedisTemplate);
        boolean locked = lock.tryLock();
        if (!locked) {
            throw new BusinessException("下单的人太多了，请稍后再试");
        }
        Order order;
        try {
            // 通过代理调用，事务在锁内开启并提交，unlock 排在提交之后
            OrderServiceImpl proxy = (OrderServiceImpl) AopContext.currentProxy();
            order = proxy.doCreateOrder(productId);
        } finally {
            lock.unlock();
        }
        // 事务已提交、锁已释放，再把消息发出去：
        // 放锁外是为了不让 MQ 的网络耗时占着锁；放事务外是为了避免事务回滚了消息却已经发出去
        // 发送失败只记日志：订单已经建好了，不能因为通知发不出去就让下单接口报错
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.ORDER_EXCHANGE, RabbitMQConfig.ORDER_CREATED_KEY, order);
            log.info("下单消息已发送到 MQ：订单ID={}", order.getId());
        } catch (Exception e) {
            log.error("下单消息发送失败，订单ID={}，卖家将收不到通知", order.getId(), e);
        }
    }

    /**
     *  createOrder 持锁后通过代理调用
     */
    //在这里加事务防止不一致，避免自调用
    @Transactional
    public Order doCreateOrder(Long productId) {
        Long userId = CurrentHolder.getCurrentId();
        //再判断一次
        Product product1 = productMapper.selectById(productId);
        if (product1.getStatus() != 1) {
            throw new BusinessException("该商品当前不可购买");
        }
        Order order = new Order();
        order.setBuyerId(userId);
        order.setSellerId(product1.getUserId());
        order.setProductId(productId);
        order.setPrice(product1.getPrice());
        order.setStatus(1); // 买家下单 → 1 交易中
        orderMapper.insert(order);
        product1.setStatus(2); // 商品置为已售出，别人不能再下单
        productMapper.updateById(product1);
        /**
             使用mysql实现防止超卖
        **/
        /*int iss = productMapper.update(null, Wrappers.<Product>lambdaUpdate()
                .eq(Product::getId, productId)
                .eq(Product::getStatus, 1)
                .set(Product::getStatus, 2));
        if (iss == 0) {
            throw new BusinessException("商品已经被别人下单了哦");
        }*/
        cacheClient.delete(CACHE_PRODUCT_KEY + productId);
        log.info("下单成功，订单ID：{}", order.getId());
        return order;
    }

    /**
     * 修改订单状态：卖家确认完成，任一方取消；取消时把商品放回在售
     */
    @Override
    @Transactional
    public void changeStatus(Long orderId, Integer status) {
        Long userId = CurrentHolder.getCurrentId();
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        boolean b1 = (userId.equals(order.getBuyerId()) || userId.equals(order.getSellerId()));
        // 仅买卖双方能操作，且仅交易中(1)卖家完成(2)任一方取消(3)
        if (!b1||order.getStatus()!=1){
            throw new BusinessException("当前状态不能进行该操作");
        }
        //双方都可以取消
        if (status == 3){
            order.setStatus(status);
            orderMapper.updateById(order);
            // 取消后设置商品状态
            Product product = productMapper.selectById(order.getProductId());
            product.setStatus(1);
            productMapper.updateById(product);
            //状态有变化，删除缓存
            cacheClient.delete(CACHE_PRODUCT_KEY + product.getId());
            return ;
        }
        //卖家可以完成
        if (userId.equals(order.getSellerId()) && status == 2){
            order.setStatus(status);
            orderMapper.updateById(order);
            return ;
        }
        throw new BusinessException("当前状态不能进行该操作");
        /*if(userId.equals(order.getSellerId())&&status.equals(2)){
            order.setSellerId(userId);
            orderMapper.updateById(order);
            return ;
        }
        if ((userId.equals(order.getBuyerId()) || userId.equals(order.getSellerId())) && status==3) {

            orderMapper.updateById(order);
            // 取消后设置商品状态
            Product product = productMapper.selectById(order.getProductId());
            product.setStatus(1);
            productMapper.updateById(product);
            return ;
        }
        throw new BusinessException("当前状态不能进行该操作");*/

    }
}