-- =========================================================
-- 校园二手交易平台 —— 完整初始化脚本
-- MySQL 8.0
-- 用法（先建库，再执行本文件）
-- 内容：
--   1~7  建表：users / category / product / product_image / favorite / orders / review
--   8    建表：notification（站内通知）
--   9    基础数据：4 个商品分类（不导入的话发布商品时没有分类可选）
-- =========================================================

-- Docker 等精简环境里 mysql 客户端默认按 latin1 读文件，这里改成utf8mb4
SET NAMES utf8mb4;

-- =========================================================
-- 1. 用户表
-- =========================================================
CREATE TABLE `users` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(16) NOT NULL UNIQUE COMMENT '用户名',
    `password` VARCHAR(64) NOT NULL COMMENT '登录密码',
    `nickname` VARCHAR(32) COMMENT '用户昵称',
    `phone` VARCHAR(11) COMMENT '手机号',
    `gender` TINYINT COMMENT '性别, 1: 男, 2: 女',
    `avatar` VARCHAR(255) COMMENT '用户头像地址',
    `role` TINYINT NOT NULL DEFAULT 0 COMMENT '用户角色：0普通用户，1管理员',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB
COMMENT='用户表';

-- =========================================================
-- 2. 商品分类表
-- =========================================================
CREATE TABLE `category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(32) NOT NULL UNIQUE COMMENT '分类名称',
    `image` VARCHAR(255) COMMENT '分类图片地址',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '分类排序值，越小越靠前',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB
COMMENT='商品分类表';

-- =========================================================
-- 3. 商品表
-- =========================================================
CREATE TABLE `product` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    `user_id` BIGINT NOT NULL COMMENT '发布用户ID',
    `category_id` BIGINT NOT NULL COMMENT '商品分类ID',
    `title` VARCHAR(64) NOT NULL COMMENT '商品标题',
    `description` VARCHAR(1000) COMMENT '商品描述',
    `price` DECIMAL(10,2) NOT NULL COMMENT '商品价格',
    `cover` VARCHAR(255) COMMENT '商品封面图片地址',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '商品状态：0下架，1在售，2已售出，3审核中',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    CONSTRAINT `fk_product_user`
        FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE RESTRICT,

    CONSTRAINT `fk_product_category`
        FOREIGN KEY (`category_id`) REFERENCES `category`(`id`) ON DELETE RESTRICT,

    INDEX `idx_product_user_id` (`user_id`),
    INDEX `idx_product_category_id` (`category_id`),
    INDEX `idx_product_status` (`status`),
    INDEX `idx_product_create_time` (`create_time`)
) ENGINE=InnoDB
COMMENT='商品表';

-- =========================================================
-- 4. 商品图片表
-- =========================================================
CREATE TABLE `product_image` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '图片ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `image_url` VARCHAR(255) NOT NULL COMMENT '图片地址',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '图片显示顺序',

    CONSTRAINT `fk_product_image_product`
        FOREIGN KEY (`product_id`) REFERENCES `product`(`id`) ON DELETE CASCADE,

    INDEX `idx_product_image_product_id` (`product_id`)
) ENGINE=InnoDB
COMMENT='商品图片表';

-- =========================================================
-- 5. 收藏表
-- =========================================================
CREATE TABLE `favorite` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '收藏ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',

    CONSTRAINT `uk_favorite_user_product`
        UNIQUE (`user_id`, `product_id`),

    CONSTRAINT `fk_favorite_user`
        FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,

    CONSTRAINT `fk_favorite_product`
        FOREIGN KEY (`product_id`) REFERENCES `product`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB
COMMENT='收藏表';

-- =========================================================
-- 6. 订单表
-- =========================================================
CREATE TABLE `orders` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    `buyer_id` BIGINT NOT NULL COMMENT '买家ID',
    `seller_id` BIGINT NOT NULL COMMENT '卖家ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `price` DECIMAL(10,2) NOT NULL COMMENT '实际成交价格',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '订单状态：0待确认，1交易中，2已完成，3已取消',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',

    CONSTRAINT `fk_orders_buyer`
        FOREIGN KEY (`buyer_id`) REFERENCES `users`(`id`) ON DELETE RESTRICT,

    CONSTRAINT `fk_orders_seller`
        FOREIGN KEY (`seller_id`) REFERENCES `users`(`id`) ON DELETE RESTRICT,

    CONSTRAINT `fk_orders_product`
        FOREIGN KEY (`product_id`) REFERENCES `product`(`id`) ON DELETE RESTRICT,

    INDEX `idx_orders_buyer_id` (`buyer_id`),
    INDEX `idx_orders_seller_id` (`seller_id`),
    INDEX `idx_orders_product_id` (`product_id`),
    INDEX `idx_orders_status` (`status`),
    INDEX `idx_orders_create_time` (`create_time`)
) ENGINE=InnoDB
COMMENT='订单表';

-- =========================================================
-- 7. 评价表
-- =========================================================
CREATE TABLE `review` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `reviewer_id` BIGINT NOT NULL COMMENT '评价人ID',
    `reviewee_id` BIGINT NOT NULL COMMENT '被评价人ID',
    `score` TINYINT NOT NULL COMMENT '评分，1-5分',
    `content` VARCHAR(500) COMMENT '评价内容',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',

    CONSTRAINT `uk_review_order_reviewer`
        UNIQUE (`order_id`, `reviewer_id`),

    CONSTRAINT `fk_review_order`
        FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`) ON DELETE RESTRICT,

    CONSTRAINT `fk_review_product`
        FOREIGN KEY (`product_id`) REFERENCES `product`(`id`) ON DELETE RESTRICT,

    CONSTRAINT `fk_review_reviewer`
        FOREIGN KEY (`reviewer_id`) REFERENCES `users`(`id`) ON DELETE RESTRICT,

    CONSTRAINT `fk_review_reviewee`
        FOREIGN KEY (`reviewee_id`) REFERENCES `users`(`id`) ON DELETE RESTRICT,

    INDEX `idx_review_reviewer_id` (`reviewer_id`),
    INDEX `idx_review_reviewee_id` (`reviewee_id`),
    INDEX `idx_review_order_id` (`order_id`),
    INDEX `idx_review_product_id` (`product_id`)
) ENGINE=InnoDB
COMMENT='评价表';

-- =========================================================
-- 8. 站内通知表
-- =========================================================
CREATE TABLE `notification` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
    `user_id` BIGINT NOT NULL COMMENT '接收人ID',
    `type` TINYINT NOT NULL COMMENT '通知类型：1新订单，2收到评价',
    `content` VARCHAR(255) NOT NULL COMMENT '通知内容',
    `order_id` BIGINT COMMENT '关联订单ID',
    `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0未读，1已读',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',

    CONSTRAINT `fk_notification_user`
        FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,

    INDEX `idx_notification_user_read` (`user_id`, `is_read`),
    INDEX `idx_notification_order_id` (`order_id`)
) ENGINE=InnoDB
COMMENT='站内通知表';

-- =========================================================
-- 9. 基础数据：商品分类
--
-- 分类图放在 uploads/ 根目录，页面首页分类栏直接引用
-- =========================================================

INSERT INTO `category` (`id`, `name`, `image`, `sort`) VALUES
    (1, '教材',     '/uploads/book.png',        0),
    (2, '电子产品', '/uploads/electronics.png', 1),
    (3, '生活用品', '/uploads/life.png',        2),
    (4, '其他',     '/uploads/other.png',       3);
