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
| 消息 | 下单后给卖家发站内通知、未读红点、标记已读 |

## 技术栈

| 层   | 技术                                                         |
| --- | ---------------------------------------------------------- |
| 后端  | Spring Boot 3.5.7、Java 21、MyBatis-Plus、PageHelper、AOP 动态代理 |
| 存储  | MySQL 8、Redis                                              |
| 消息  | RabbitMQ（Spring AMQP）                                      |
| 鉴权  | JWT + 拦截器 + ThreadLocal                                    |
| 前端  | Vue 3、Vite 5、Element Plus、Pinia、Axios                      |

## 目录结构

```
second_hand/
├── backend/            Spring Boot 后端
│   └── src/main/
│       ├── java/com/example/
│       │   ├── controller/   控制器
│       │   ├── service/      业务层
│       │   ├── mapper/       数据访问层（复杂 SQL 在 resources/mapper/*.xml）
│       │   ├── entity/       实体
│       │   ├── config/       配置（WebConfig：拦截器 + 静态资源映射；RabbitMQConfig：队列与交换机）
│       │   ├── mq/           消息消费者
│       │   ├── interceptor/  登录拦截器
│       │   └── utils/        工具（JWT、Redis 缓存、互斥锁等）
│       └── resources/
│           ├── application.yml   配置文件
│           ├── mapper/           MyBatis XML
│           └── unlock.lua        Redis 解锁脚本
├── frontend/           Vue 3 前端
│   └── src/
│       ├── views/      页面
│       ├── components/ 公共组件
│       └── api/        接口封装
├── sql/                数据库脚本
│   └── second_hand.sql       建表 + 4 个商品分类
├── uploads/            上传的图片（本地磁盘）
├── 接口文档.md
└── README.md
```

## 环境要求

| 软件      | 版本   | 说明                         |
| ------- | ---- | -------------------------- |
| JDK     | 21   | 后端编译运行，需配置 `JAVA_HOME`     |
| Maven   | 3.9+ | 命令行构建；用 IDEA 可忽略（自带 Maven） |
| MySQL   | 8.x  | 字符集需为 utf8mb4              |
| Redis   | 6.x+ | 缓存、收藏、下单锁都依赖它              |
| RabbitMQ | 3.x  | 下单通知走消息队列，需带 management 插件的镜像 |
| Node.js | 18+  | 自带 npm                     |
|         |      |                            |
redis下载自己找教程 

[免费Redis图形化界面（RESP）下载地址和连接步骤_resp下载-CSDN博客](https://blog.csdn.net/drhnb/article/details/131717836)
---方便查看控制redis
## 快速开始

### 1. 准备数据库

```bash
# 建表 + 4 个商品分类，一个文件搞定
mysql -u root -p --default-character-set=utf8mb4 second_hand < sql/second_hand.sql
```

> 也可以通过navicat等引入sql语句创建，数据要自己插入

### 2. 启动 RabbitMQ

```bash
docker run -d --name rabbitmq   -p 5672:5672 -p 15672:15672   -e RABBITMQ_DEFAULT_USER=admin   -e RABBITMQ_DEFAULT_PASS=123   rabbitmq:3-management

# 给本项目单独建一个 vhost，和同机器上的其他项目隔离
docker exec rabbitmq rabbitmqctl add_vhost second_hand
docker exec rabbitmq rabbitmqctl set_permissions -p second_hand admin ".*" ".*" ".*"
```

管理界面：`http://localhost:15672`（账号 `admin`，密码 `123`）。

> 本机已装 RabbitMQ 的可以跳过，只建 vhost 即可。

### 3. 修改后端配置

打开 `backend/src/main/resources/application.yml`，改这四处：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/second_hand?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: 123456                                    # 改成你的 MySQL 密码

  data:
    redis:
      host: 127.0.0.1     # 改成你的 Redis 地址
      port: 6379
      password: 123       # 没设密码就把这一行删掉

  rabbitmq:
    host: 127.0.0.1       # 改成你的 RabbitMQ 地址
    port: 5672
    username: admin
    password: 123
    virtual-host: second_hand   # 与第 2 步建的一致

file:
  upload-dir: D:/quan_zhan/second_hand/uploads/          # 改成你的图片目录，末尾带 /
```

> `upload-dir` 指向项目里的 `uploads/` 目录即可，写绝对路径，**结尾必须带 `/`**。
> Linux/macOS 示例：`/home/yourname/second_hand/uploads/`

### 4. 启动后端

```bash
cd backend
mvn spring-boot:run
```

或者直接在 IDEA 里运行 `BackendApplication`。

看到这段就是成功了：

```
Tomcat started on port 8080 (http)
Started BackendApplication in 3.037 seconds
```

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

浏览器打开 `http://localhost:5173`。

打包生产版本用 `npm run build`，产物在 `frontend/dist`；`npm run preview` 可本地预览构建结果。

### 6. 验证是否跑通

```

然后在浏览器打开 `http://localhost:5173`，注册一个账号，登录后应该能看到 4 个分类。发布一个商品、上传一张图片，整条链路就通了。

### 7. 同步收藏缓存（有历史数据时）
在 IDEA 里运行测试类 `backend/src/test/java/com/example/FavoriteCacheTest.java` 的 `preheat()` 方法。
---
```

**npm install 很慢时加镜像**

```bash
npm config set registry https://registry.npmmirror.com
```
---

## 接口文档

完整接口说明见 [接口文档.md](./接口文档.md)。

