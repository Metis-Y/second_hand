# 校园二手交易平台

面向校园场景的二手闲置交易平台：卖家发布商品，买家浏览、搜索、收藏、下单，交易完成后买卖双方互评。

后端 Spring Boot 3 + MyBatis-Plus + MySQL + Redis，前端 Vue 3 + Element Plus。

---

## 功能

| 模块 | 功能 |
| --- | --- |
| 用户 | 注册、登录、查看他人资料、编辑自己的资料 |
| 商品 | 分类浏览、分页列表、关键字搜索、详情、发布、编辑、删除 |
| 图片 | 上传商品图片，返回站内访问地址 |
| 收藏 | 添加/取消收藏、我的收藏列表 |
| 订单 | 下单、我买到的 / 我卖出的、卖家确认完成、双方取消 |
| 评价 | 对已完成订单评价、商品评价列表、个人主页收到的评价 |

## 技术栈

| 层 | 技术 |
| --- | --- |
| 后端 | Spring Boot 3.5.7、Java 21、MyBatis-Plus、PageHelper |
| 存储 | MySQL 8、Redis |
| 鉴权 | JWT + 拦截器 + ThreadLocal |
| 前端 | Vue 3、Vite 5、Element Plus、Pinia、Axios |

## 目录结构

```
second_hand/
├── backend/            Spring Boot 后端
│   └── src/main/java/com/example/
│       ├── controller/ 控制器
│       ├── service/    业务层
│       ├── mapper/     数据访问层
│       ├── entity/     实体
│       ├── config/     配置（WebConfig：拦截器 + 静态资源映射）
│       ├── interceptor/ 登录拦截器
│       └── utils/      工具（JWT、Redis 缓存、互斥锁等）
├── frontend/           Vue 3 前端
│   └── src/
│       ├── views/      页面
│       ├── components/ 公共组件
│       ├── api/        接口封装
│       └── mock/       前端 mock 数据（VITE_USE_MOCK=true 时启用）
├── sql/                数据库脚本
├── uploads/            上传的图片（本地磁盘）
├── 接口文档.md
└── README.md
```

## 环境要求

| 软件 | 版本 |
| --- | --- |
| JDK | 21+ |
| Maven | 3.9+ |
| MySQL | 8.x |
| Redis | 6.x 及以上 |
| Node.js | 18+ |

---

## 快速启动

### 1. 建库并导入表结构

```bash
mysql -u root -p -e "CREATE DATABASE second_hand DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;"
mysql -u root -p second_hand < sql/second_hand.sql
```

> `sql/second_hand.sql` 只有建表语句，没有初始数据。启动后在前端注册账号、发布商品即可。

### 2. 修改后端配置

打开 `backend/src/main/resources/application.yml`，改这三处：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/second_hand?...   # 改成你的 MySQL 地址
    username: root
    password: 123456                                    # 改成你的密码

  data:
    redis:
      host: 127.0.0.1     # 改成你的 Redis 地址
      port: 6379
      password: 123       # 没有密码就删掉这一行

file:
  upload-dir: D:/quan_zhan/second_hand/uploads/          # 改成你本机的图片目录，末尾带 /
```

> `file.upload-dir` 下的 `products` 子目录会在上传时自动创建，父目录需要提前存在。

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

启动后接口地址为 `http://localhost:8080`。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

浏览器打开 `http://localhost:5173`。

开发环境下 `/api` 和 `/uploads` 已由 Vite 代理到 `http://localhost:8080`，前端不需要额外处理跨域。

### 5. 首次启动：跑一次缓存预热

收藏关系存在 Redis 的 Set 里，数据库里的历史收藏需要手工同步一次：

在 IDEA 里运行测试类 `backend/src/test/java/com/example/FavoriteCacheTest.java` 的 `preheat()` 方法。

> 不跑的话，商品详情页会一律显示「未收藏」。
> 只有 Redis 数据被清空（`FLUSHDB`、换机器）时才需要重跑，平时重启应用**不需要**。

---

## 配置说明

| 配置项 | 说明 |
| --- | --- |
| `spring.datasource.*` | MySQL 连接，库名固定 `second_hand` |
| `spring.data.redis.*` | Redis 连接。Boot 3 的前缀是 `spring.data.redis`，不是 `spring.redis` |
| `spring.data.redis.database` | 默认注释掉（用 0 号库）。该 Redis 只开了 0 号库，指定其它库会报 `DB index is out of range` |
| `file.upload-dir` | 图片存放的本地目录，`/uploads/**` 会映射到这里 |
| `jwt.secret` | 令牌签名密钥，正式部署前务必换掉 |
| `jwt.expire-hours` | 令牌有效期（小时） |
| `logging.level.com.example.mapper` | 设为 `debug` 会打印 SQL，排查问题时用 |

前端可选环境变量 `VITE_USE_MOCK=true`，开启后所有接口走本地 mock 数据，方便在没启动后端时调样式。

---

## 已实现的技术点

- **Redis 缓存**：分类列表、商品详情、用户资料，采用 Cache-Aside 模式（写时删、读时重建）
  - 防穿透：查不到的数据缓存空字符串，TTL 2 分钟
  - 防击穿：缓存重建加互斥锁，未抢到锁的线程等待 50ms 后重试
- **收藏判断**：收藏关系存 Redis Set（`second_hand:favorite:user:{userId}`），详情页用 `SISMEMBER` 判断，不再查库
- **下单防超卖**：Redis 互斥锁（`SETNX` 加锁 + Lua 脚本解锁），事务通过 AOP 代理在锁内提交
- **鉴权**：JWT + 拦截器，拦截器在 `afterCompletion` 里清理 ThreadLocal，避免线程复用串号
- **统一返回体**：`{ code, message, data }`，`code=1` 成功、`code=0` 失败；全局异常处理器统一转成该结构

## 接口文档

完整接口说明见 [接口文档.md](./接口文档.md)。

## 已知待办

- 密码目前明文存储，计划改成 BCrypt
- 图片存本机磁盘，多实例部署时需要换成对象存储
- 没有「修改密码」接口
- 站内通知（MQ）尚未实现，下单后卖家只能自己刷订单列表