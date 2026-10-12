// =====================================================================
// API 层：统一前端所有后端调用。
// 约定：后端返回 { code, message, data }，code 200 成功（解包逻辑见 utils/request.js）。
// 每个函数都是同一个套路：
//   · VITE_USE_MOCK=true  → 走 mock/index.js 的内置演示数据（后端没写好也能跑页面）
//   · VITE_USE_MOCK=false → 走真实 http 请求（地址前缀 /api，由 vite 代理到后端）
// 页面只 import 这里的函数，不直接写 axios，便于日后统一替换。
// =====================================================================
import request from '@/utils/request'
import * as mock from '@/mock'

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

// ---------- 认证 ----------

/**
 * 登录
 * @param {{username: string, password: string}} data 登录表单
 * @returns {Promise<object>} 登录数据：token + 用户信息（扁平结构，无嵌套 user 字段）
 */
export function login(data) {
  if (USE_MOCK) return mock.login(data)
  return request.post('/auth/login', data)
}

/**
 * 注册新用户（成功后不自动登录，需跳登录页）
 * @param {{username: string, password: string, nickname?: string, phone?: string}} data
 */
export function register(data) {
  if (USE_MOCK) return mock.register(data)
  return request.post('/auth/register', data)
}

// ---------- 分类 ----------

/**
 * 获取全部分类（首页分类栏、发布页下拉框使用）
 * @returns {Promise<Array<{id: number, name: string}>>}
 */
export function getCategories() {
  if (USE_MOCK) return mock.getCategories()
  return request.get('/categories')
}

// ---------- 商品 ----------

/**
 * 分页查询商品列表
 * @param {{page?: number, size?: number, keyword?: string, categoryId?: number|null, status?: number}} params
 * @returns {Promise<{list: Array, total: number}>}
 */
export function getProducts(params) {
  if (USE_MOCK) return mock.getProducts(params)
  return request.get('/products', { params })
}

/**
 * 查询商品详情（含 images 图片数组、seller 卖家信息）
 * @param {number|string} id 商品 ID
 */
export function getProduct(id) {
  if (USE_MOCK) return mock.getProduct(id)
  return request.get(`/products/${id}`)
}

/**
 * 发布新商品
 * @param {{title: string, categoryId: number, price: number, description?: string, images?: string[]}} data
 */
export function createProduct(data) {
  if (USE_MOCK) return mock.createProduct(data)
  return request.post('/products', data)
}

/**
 * 修改商品（编辑页保存时调用）
 * @param {number|string} id 商品 ID
 * @param {object} data 同 createProduct
 */
export function updateProduct(id, data) {
  if (USE_MOCK) return mock.updateProduct(id, data)
  return request.put(`/products/${id}`, data)
}

/**
 * 删除商品（个人中心里删除自己发布的商品）
 * @param {number|string} id 商品 ID
 */
export function deleteProduct(id) {
  if (USE_MOCK) return mock.deleteProduct(id)
  return request.delete(`/products/${id}`)
}

/**
 * 查询"我发布的"商品列表（个人中心使用）
 * @returns {Promise<{list: Array, total: number}>}
 */
export function getMyProducts() {
  if (USE_MOCK) return mock.getMyProducts()
  return request.get('/products/my')
}

// ---------- 收藏 ----------

/**
 * 查询我的收藏列表（收藏页使用）
 * @returns {Promise<{list: Array, total: number}>}
 */
export function getFavorites() {
  if (USE_MOCK) return mock.getFavorites()
  return request.get('/favorites')
}

/**
 * 收藏商品
 * @param {number|string} productId 商品 ID
 */
export function addFavorite(productId) {
  if (USE_MOCK) return mock.addFavorite(productId)
  return request.post('/favorites', { productId })
}

/**
 * 取消收藏
 * @param {number|string} productId 商品 ID
 */
export function removeFavorite(productId) {
  if (USE_MOCK) return mock.removeFavorite(productId)
  return request.delete(`/favorites/${productId}`)
}

/**
 * 查询某商品是否已被当前用户收藏（详情页初始化收藏按钮状态）
 * @param {number|string} productId 商品 ID
 * @returns {Promise<boolean>}
 */
export function isFavorite(productId) {
  // mock 的 isFavorite 是同步函数，包一层 Promise 保持调用方式一致
  if (USE_MOCK) return Promise.resolve(mock.isFavorite(productId))
  return request.get(`/favorites/check/${productId}`)
}

// ---------- 订单 ----------

/**
 * 查询订单列表
 * @param {{role?: 'buyer'|'seller', status?: number|string}} params
 *        role=buyer 查"我买到的"，role=seller 查"我卖出的"
 * @returns {Promise<{list: Array, total: number}>}
 */
export function getOrders(params) {
  if (USE_MOCK) return mock.getOrders(params)
  return request.get('/orders', { params })
}

/**
 * 下单购买商品（买家点击"立即购买"）
 * @param {number|string} productId 商品 ID
 */
export function createOrder(productId) {
  if (USE_MOCK) return mock.createOrder(productId)
  return request.post('/orders', { productId })
}

/**
 * 修改订单状态（确认交易、确认收货、取消订单等）
 * @param {number|string} id 订单 ID
 * @param {number} status 0 待确认 / 1 交易中 / 2 已完成 / 3 已取消
 */
export function updateOrderStatus(id, status) {
  if (USE_MOCK) return mock.updateOrderStatus(id, status)
  return request.put(`/orders/${id}/status`, { status })
}

// ---------- 评价 ----------

/**
 * 查询某商品的全部评价（详情页评价区）
 * @param {number|string} productId 商品 ID
 */
export function getProductReviews(productId) {
  if (USE_MOCK) return mock.getProductReviews(productId)
  return request.get(`/reviews/product/${productId}`)
}

/**
 * 查询某用户收到的评价（个人中心"收到的评价"页签）
 * @param {number|string} userId 用户 ID
 */
export function getUserReviews(userId) {
  if (USE_MOCK) return mock.getUserReviews(userId)
  return request.get(`/reviews/user/${userId}`)
}

/**
 * 提交订单评价
 * @param {{orderId: number, score: number, content?: string}} data 评分 1~5 与评价内容
 */
export function createReview(data) {
  if (USE_MOCK) return mock.createReview(data)
  return request.post('/reviews', data)
}

// ---------- 图片上传 ----------

/**
 * 上传商品图片
 * @param {File} file 浏览器 File 对象
 * @returns {Promise<{url: string}>} 图片可访问地址（mock 下为本地 data URL）
 */
export function uploadImage(file) {
  if (USE_MOCK) return mock.uploadImage(file)
  // 真实接口需用 multipart/form-data，字段名为 file
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
}