// =====================================================================
// API 层：统一前端所有后端调用。
// 约定：后端返回 { code, message, data }，code 200 成功。
// 当 VITE_USE_MOCK=true 时走内置演示数据，false 时走真实 http。
// =====================================================================
import request from '@/utils/request'
import * as mock from '@/mock'

const USE_MOCK = import.meta.env.VITE_USE_MOCK === 'true'

// ---------- 认证 ----------
export function login(data) {
  if (USE_MOCK) return mock.login(data)
  return request.post('/auth/login', data)
}

export function register(data) {
  if (USE_MOCK) return mock.register(data)
  return request.post('/auth/register', data)
}

// ---------- 分类 ----------
export function getCategories() {
  if (USE_MOCK) return mock.getCategories()
  return request.get('/categories')
}

// ---------- 商品 ----------
export function getProducts(params) {
  if (USE_MOCK) return mock.getProducts(params)
  return request.get('/products', { params })
}

export function getProduct(id) {
  if (USE_MOCK) return mock.getProduct(id)
  return request.get(`/products/${id}`)
}

export function createProduct(data) {
  if (USE_MOCK) return mock.createProduct(data)
  return request.post('/products', data)
}

export function updateProduct(id, data) {
  if (USE_MOCK) return mock.updateProduct(id, data)
  return request.put(`/products/${id}`, data)
}

export function deleteProduct(id) {
  if (USE_MOCK) return mock.deleteProduct(id)
  return request.delete(`/products/${id}`)
}

export function getMyProducts() {
  if (USE_MOCK) return mock.getMyProducts()
  return request.get('/products/my')
}

// ---------- 收藏 ----------
export function getFavorites() {
  if (USE_MOCK) return mock.getFavorites()
  return request.get('/favorites')
}

export function addFavorite(productId) {
  if (USE_MOCK) return mock.addFavorite(productId)
  return request.post('/favorites', { productId })
}

export function removeFavorite(productId) {
  if (USE_MOCK) return mock.removeFavorite(productId)
  return request.delete(`/favorites/${productId}`)
}

export function isFavorite(productId) {
  if (USE_MOCK) return Promise.resolve(mock.isFavorite(productId))
  return request.get(`/favorites/check/${productId}`)
}

// ---------- 订单 ----------
export function getOrders(params) {
  if (USE_MOCK) return mock.getOrders(params)
  return request.get('/orders', { params })
}

export function createOrder(productId) {
  if (USE_MOCK) return mock.createOrder(productId)
  return request.post('/orders', { productId })
}

export function updateOrderStatus(id, status) {
  if (USE_MOCK) return mock.updateOrderStatus(id, status)
  return request.put(`/orders/${id}/status`, { status })
}

// ---------- 评价 ----------
export function getProductReviews(productId) {
  if (USE_MOCK) return mock.getProductReviews(productId)
  return request.get(`/reviews/product/${productId}`)
}

export function getUserReviews(userId) {
  if (USE_MOCK) return mock.getUserReviews(userId)
  return request.get(`/reviews/user/${userId}`)
}

export function createReview(data) {
  if (USE_MOCK) return mock.createReview(data)
  return request.post('/reviews', data)
}

// ---------- 图片上传 ----------
export function uploadImage(file) {
  if (USE_MOCK) return mock.uploadImage(file)
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/upload', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
}