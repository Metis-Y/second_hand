# 校园二手交易平台 · 前端

基于 **Vue 3 + Vite + Vue Router + Pinia + Element Plus + Axios** 的单页应用。

## 一、如何运行

> 需要本机已有 Node.js（建议 18+，当前环境为 v24）。

```bash
cd frontend
npm install      # 首次安装依赖
npm run dev      # 启动开发服务器
```

启动后浏览器打开 <http://localhost:5173>。

生产构建：

```bash
npm run build    # 产物输出到 frontend/dist
npm run preview  # 本地预览构建结果
```

## 二、演示数据（mock）开关

后端接口还没写好时，打开演示数据，页面也能完整跑起来：

- `frontend/.env.development` 里 `VITE_USE_MOCK=true` → 使用内置演示数据（默认）
- 改为 `VITE_USE_MOCK=false` → 切换为真实 http 请求，走 `/api` 前缀

**演示账号**（password 都是 `123456`）：

| 用户名 | 角色 |
| --- | --- |
| `admin` | 管理员 |
| `xiaoming` | 普通用户，已发布若干商品 |
| `xiaohong` | 普通用户 |

## 三、目录结构

```
frontend/
├─ index.html
├─ vite.config.js          # 含 /api 到 http://localhost:8080 的代理
├─ package.json
├─ .env.development        # 开发环境变量
├─ .env.production
└─ src/
   ├─ main.js              # 入口，注册 Element Plus 与图标
   ├─ App.vue
   ├─ router/index.js      # 路由 + 登录守卫
   ├─ stores/user.js       # 登录状态（Pinia）
   ├─ utils/request.js     # axios 封装（统一处理 code/token）
   ├─ api/index.js         # 所有后端接口的调用封装
   ├─ mock/index.js        # 内置演示数据
   ├─ layouts/MainLayout.vue
   ├─ components/ProductCard.vue
   └─ views/               # 各页面
```

## 四、与后端的接口约定（联调时请按此对齐）

- 统一响应格式：`{ "code": 1, "message": "ok", "data": ... }`，`code=1` 成功、`code=0` 失败。
- 登录后前端在请求头带 `Authorization: Bearer <token>`。
- 列表接口的 `data` 统一为 `{ "list": [...], "total": 10 }`。

| 功能      | 方法 & 路径                                                     | 说明                                                     |                 |
| ------- | ----------------------------------------------------------- | ------------------------------------------------------ | --------------- |
| 注册      | `POST /api/auth/register`                                   | body: `{username, password, nickname, phone}`          |                 |
| 登录      | `POST /api/auth/login`                                      | body: `{username, password}` → data: `{token, user}`   |                 |
| 分类列表    | `GET /api/products/categories`                                       | data: 分类数组                                             |                 |
| 商品列表    | `GET /api/products`                                         | query: `page,size,categoryId,status`                   |                 |
| 搜索商品    | `GET /api/products/search`                                  | query: `keyword,page,size,status`                      |                 |
| 商品详情    | `GET /api/products/{id}`                                    | 含 `images`、`sellerNickname`、`sellerAvatar`、`favorited`                                    |                 |
| 发布商品    | `POST /api/products`                                        | body 含 `title, categoryId, price, description, images` |                 |
| 编辑/删除   | `PUT /api/products/{id}` / `DELETE /api/products/{id}`      |                                                        |                 |
| 我发布的    | `GET /api/products/my`                                      |                                                        |                 |
| 收藏列表    | `GET /api/favorites`                                        |                                                        |                 |
| 收藏/取消   | `POST /api/favorites/{productId}` / `DELETE /api/favorites/{productId}` | 路径参数 productId                                    |                 |
| 下单      | `POST /api/orders/{productId}`                              | 路径参数 productId                                    |                 |
| 订单列表    | `GET /api/orders`                                           | query: `role=buyer` 或 `role=seller`、`status`          |                 |
| 改订单状态   | `PUT /api/orders/{id}/status/{status}`                      | 路径参数 id、status（2已完成 / 3已取消）               |                 |
| 发表评价    | `POST /api/reviews`                                         | body: `{orderId, score, content}`                      |                 |
| 商品评价    | `GET /api/reviews/product/{productId}`                      |                                                        |                 |
| 用户收到的评价 | `GET /api/reviews/user/{userId}`                            |                                                        |                 |
| 图片上传    | `POST /api/upload`                                          | multipart，字段名 `file`，data: `{url}`                     |                 |

> 订单状态码：`1 交易中`、`2 已完成`、`3 已取消`。
> 商品状态码：`0 下架`、`1 在售`、`2 已售出`、`3 审核中`。