// =====================================================================
// 前端演示数据（mock）
// 当 .env.development 里 VITE_USE_MOCK=true 时启用，方便后端未就绪时预览页面。
// 后端写完、联调时把该开关改为 false，前端即切换为真实 http 请求。
// 这里每个函数返回的，就是后端统一响应 { code, message, data } 里的 data。
// =====================================================================

const delay = (ms = 200) => new Promise((r) => setTimeout(r, ms))
const now = () => new Date().toISOString().slice(0, 19).replace('T', ' ')

// 生成本地占位图（SVG data URI，离线也能显示）
function svgImg(text, bg = '#97b8e6') {
  const safe = String(text || '').slice(0, 8)
  const svg =
    '<svg xmlns="http://www.w3.org/2000/svg" width="400" height="300">' +
    `<rect width="400" height="300" fill="${bg}"/>` +
    `<text x="200" y="150" font-family="sans-serif" font-size="26" fill="#ffffff" text-anchor="middle" dominant-baseline="middle">${safe}</text>` +
    '</svg>'
  return 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg)
}

const categoryColors = { 1: '#5b8ff9', 2: '#5ad8a6', 3: '#f6bd16', 4: '#9270ca' }

// ---------- 基础数据 ----------
const categories = [
  { id: 1, name: '教材', image: svgImg('教材', categoryColors[1]), sort: 0, createTime: now() },
  { id: 2, name: '电子产品', image: svgImg('电子', categoryColors[2]), sort: 1, createTime: now() },
  { id: 3, name: '生活用品', image: svgImg('生活', categoryColors[3]), sort: 2, createTime: now() },
  { id: 4, name: '其他', image: svgImg('其他', categoryColors[4]), sort: 3, createTime: now() }
]

const users = [
  { id: 1, username: 'admin', password: '123456', nickname: '管理员', phone: '13800000000', gender: 1, role: 1, avatar: svgImg('管') },
  { id: 2, username: 'xiaoming', password: '123456', nickname: '小明', phone: '13811112222', gender: 1, role: 0, avatar: svgImg('明') },
  { id: 3, username: 'xiaohong', password: '123456', nickname: '小红', phone: '13833334444', gender: 2, role: 0, avatar: svgImg('红') }
]

const catalog = [
  { title: '高等数学（同济第七版·上册）', categoryId: 1, price: 12.0, seller: 2, description: '九成新，笔记工整，无缺页，适合考研第一轮复习。' },
  { title: '数据结构（严蔚敏）', categoryId: 1, price: 10.0, seller: 2, description: '计算机专业教材，有少量划线，封面完好。' },
  { title: '大学英语四级真题汇编', categoryId: 1, price: 15.0, seller: 3, description: '全新未写，含近三年真题听力。' },
  { title: '考研政治 1000 题', categoryId: 1, price: 18.0, seller: 3, description: '只做了前两章，其余全新。' },
  { title: '罗技 M330 无线鼠标', categoryId: 2, price: 45.0, seller: 2, description: '自用半年，手感很好，无拆无修，附接收器。' },
  { title: 'AirPods 2 蓝牙耳机', categoryId: 2, price: 320.0, seller: 3, description: '成色 95 新，续航正常，原装充电盒。' },
  { title: 'Kindle Paperwhite 3', categoryId: 2, price: 280.0, seller: 2, description: '墨水屏护眼，看书神器，屏幕无划痕。' },
  { title: 'TP-Link 千兆路由器', categoryId: 2, price: 60.0, seller: 3, description: '升级 Wi-Fi6 换下，功能正常，配件齐全。' },
  { title: '折叠书桌', categoryId: 3, price: 30.0, seller: 2, description: '宿舍神器，桌面可折叠，不占地方。' },
  { title: '护眼台灯', categoryId: 3, price: 25.0, seller: 3, description: '三档调光，USB 供电，毕业出。' },
  { title: '瑜伽垫（加厚）', categoryId: 3, price: 20.0, seller: 2, description: '10mm 加厚防滑，只用过几次，已清洁。' },
  { title: '斯伯丁篮球', categoryId: 3, price: 50.0, seller: 3, description: '7 号球，手感好，轻微使用痕迹。' },
  { title: '24 寸山地自行车', categoryId: 4, price: 260.0, seller: 2, description: '车况良好，刹车灵敏，赠送车锁。' },
  { title: '小熊加湿器', categoryId: 4, price: 40.0, seller: 3, description: '静音大容量，宿舍办公室都适用。' }
]

// ---------- localStorage 持久化 ----------
function readList(key) {
  try { return JSON.parse(localStorage.getItem(key) || '[]') } catch { return [] }
}
function writeList(key, list) {
  localStorage.setItem(key, JSON.stringify(list))
}

function buildImages(title, categoryId, count = 3) {
  const c = categoryColors[categoryId] || '#97b8e6'
  const arr = []
  for (let i = 0; i < count; i++) arr.push(svgImg(`${title.slice(0, 5)} ${i + 1}`, c))
  return arr
}

function buildProduct(s, id) {
  const images = buildImages(s.title, s.categoryId, 3)
  return {
    id,
    userId: s.seller,
    categoryId: s.categoryId,
    title: s.title,
    description: s.description,
    price: s.price,
    cover: images[0],
    status: 1,
    createTime: now(),
    updateTime: now(),
    images
  }
}

function loadProducts() {
  const raw = localStorage.getItem('mock_products')
  if (raw) { try { return JSON.parse(raw) } catch { /* ignore */ } }
  const seeded = catalog.map((s, i) => buildProduct(s, i + 1))
  localStorage.setItem('mock_products', JSON.stringify(seeded))
  return seeded
}

let products = loadProducts()
let nextProductId = products.length + 1000

function saveProducts() { writeList('mock_products', products) }

function nicknameOf(userId) {
  return users.find((u) => u.id === Number(userId))?.nickname || '用户'
}
function avatarOf(userId) {
  return users.find((u) => u.id === Number(userId))?.avatar || svgImg('户')
}
function findProduct(id) {
  return products.find((p) => p.id === Number(id))
}
function currentUser() {
  const raw = localStorage.getItem('user')
  if (raw) {
    try {
      const u = JSON.parse(raw)
      const found = users.find((x) => x.id === u.id)
      if (found) return found
    } catch { /* ignore */ }
  }
  return users[1]
}
function stripPassword(u) {
  const c = { ...u }
  delete c.password
  return c
}

// ---------- 认证 ----------
export async function login({ username, password }) {
  await delay()
  const u = users.find((x) => x.username === username && x.password === password)
  if (!u) throw new Error('用户名或密码错误')
  return { token: `mock-token-${u.id}`, user: stripPassword(u) }
}

export async function register({ username, password, nickname, phone }) {
  await delay()
  if (users.some((x) => x.username === username)) throw new Error('用户名已存在')
  const u = {
    id: Date.now(), username, password,
    nickname: nickname || username, phone: phone || '',
    gender: 0, role: 0, avatar: svgImg('新')
  }
  users.push(u)
  return { token: `mock-token-${u.id}`, user: stripPassword(u) }
}

// ---------- 分类 ----------
export async function getCategories() {
  await delay(100)
  return categories
}

// ---------- 商品 ----------
export async function getProducts({ page = 1, size = 12, keyword = '', categoryId = null, status } = {}) {
  await delay()
  let list = [...products].sort((a, b) => (b.createTime > a.createTime ? 1 : -1))
  if (categoryId) list = list.filter((p) => p.categoryId === Number(categoryId))
  if (keyword) list = list.filter((p) => p.title.includes(keyword) || p.description.includes(keyword))
  if (status !== undefined && status !== null && status !== '') {
    list = list.filter((p) => p.status === Number(status))
  } else {
    list = list.filter((p) => p.status === 1) // 首页默认只展示「在售」
  }
  const total = list.length
  const start = (Number(page) - 1) * Number(size)
  const pageList = list.slice(start, start + Number(size)).map((p) => ({ ...p, nickname: nicknameOf(p.userId) }))
  return { list: pageList, total }
}

export async function getProduct(id) {
  await delay(100)
  const p = findProduct(id)
  if (!p) throw new Error('商品不存在')
  return {
    ...p,
    nickname: nicknameOf(p.userId),
    seller: { id: p.userId, nickname: nicknameOf(p.userId), avatar: avatarOf(p.userId) }
  }
}

export async function createProduct(data) {
  await delay()
  const u = currentUser()
  const images = (data.images && data.images.length ? data.images : [svgImg(data.title, categoryColors[data.categoryId])])
  const p = {
    id: nextProductId++, userId: u.id,
    categoryId: Number(data.categoryId), title: data.title,
    description: data.description || '', price: Number(data.price),
    cover: images[0], status: 1, createTime: now(), updateTime: now(), images
  }
  products.unshift(p)
  saveProducts()
  return p
}

export async function updateProduct(id, data) {
  await delay()
  const p = findProduct(id)
  if (!p) throw new Error('商品不存在')
  p.title = data.title
  p.description = data.description || ''
  p.price = Number(data.price)
  p.categoryId = Number(data.categoryId)
  if (data.images && data.images.length) {
    p.images = data.images
    p.cover = data.images[0]
  }
  p.updateTime = now()
  saveProducts()
  return p
}

export async function deleteProduct(id) {
  await delay()
  products = products.filter((p) => p.id !== Number(id))
  saveProducts()
  return true
}

export async function getMyProducts() {
  await delay()
  const u = currentUser()
  return { list: products.filter((p) => p.userId === u.id), total: products.filter((p) => p.userId === u.id).length }
}

function markSold(productId) {
  const p = findProduct(productId)
  if (p) {
    p.status = 2
    p.updateTime = now()
    saveProducts()
  }
}

// ---------- 收藏 ----------
export async function getFavorites() {
  await delay()
  const u = currentUser()
  const favs = readList('mock_favorites').filter((f) => f.userId === u.id)
  const list = favs
    .map((f) => {
      const p = findProduct(f.productId)
      return p ? { ...p, nickname: nicknameOf(p.userId), favoriteTime: f.createTime } : null
    })
    .filter(Boolean)
  return { list, total: list.length }
}

export async function addFavorite(productId) {
  await delay(80)
  const u = currentUser()
  const favs = readList('mock_favorites')
  if (!favs.some((f) => f.userId === u.id && f.productId === Number(productId))) {
    favs.push({ id: Date.now(), userId: u.id, productId: Number(productId), createTime: now() })
    writeList('mock_favorites', favs)
  }
  return true
}

export async function removeFavorite(productId) {
  await delay(80)
  const u = currentUser()
  const favs = readList('mock_favorites').filter((f) => !(f.userId === u.id && f.productId === Number(productId)))
  writeList('mock_favorites', favs)
  return true
}

export function isFavorite(productId) {
  const u = currentUser()
  return readList('mock_favorites').some((f) => f.userId === u.id && f.productId === Number(productId))
}

// ---------- 订单 ----------
export async function createOrder(productId) {
  await delay()
  const u = currentUser()
  const p = findProduct(productId)
  if (!p) throw new Error('商品不存在')
  if (p.userId === u.id) throw new Error('不能购买自己发布的商品')
  if (p.status !== 1) throw new Error('该商品当前不可购买')
  const orders = readList('mock_orders')
  const order = {
    id: Date.now(), buyerId: u.id, sellerId: p.userId, productId: p.id,
    price: p.price, status: 0, createTime: now(), updateTime: now(),
    buyerNickname: u.nickname, sellerNickname: nicknameOf(p.userId)
  }
  orders.unshift(order)
  writeList('mock_orders', orders)
  markSold(p.id)
  return order
}

export async function getOrders({ role = 'buyer', status } = {}) {
  await delay()
  const u = currentUser()
  let list = readList('mock_orders').filter((o) => (role === 'buyer' ? o.buyerId === u.id : o.sellerId === u.id))
  if (status !== undefined && status !== null && status !== '') list = list.filter((o) => o.status === Number(status))
  list = list.map((o) => ({ ...o, product: findProduct(o.productId) || { title: '商品已删除' } }))
  return { list, total: list.length }
}

export async function updateOrderStatus(id, status) {
  await delay()
  const orders = readList('mock_orders')
  const order = orders.find((o) => o.id === Number(id))
  if (!order) throw new Error('订单不存在')
  order.status = Number(status)
  order.updateTime = now()
  writeList('mock_orders', orders)
  return order
}

// ---------- 评价 ----------
export async function createReview({ orderId, score, content }) {
  await delay()
  const u = currentUser()
  const orders = readList('mock_orders')
  const order = orders.find((o) => o.id === Number(orderId))
  if (!order) throw new Error('订单不存在')
  const reviews = readList('mock_reviews')
  if (reviews.some((r) => r.orderId === Number(orderId) && r.reviewerId === u.id)) throw new Error('该订单已评价过')
  const review = {
    id: Date.now(), orderId: Number(orderId),
    productId: order.productId,
    reviewerId: u.id, revieweeId: order.sellerId === u.id ? order.buyerId : order.sellerId,
    score: Number(score), content: content || '', createTime: now(),
    reviewerNickname: u.nickname, revieweeNickname: order.sellerId === u.id ? order.buyerNickname : order.sellerNickname
  }
  reviews.unshift(review)
  writeList('mock_reviews', reviews)
  return review
}

export async function getProductReviews(productId) {
  await delay(100)
  return { list: readList('mock_reviews').filter((r) => r.productId === Number(productId)), total: 0 }
}

export async function getUserReviews(userId) {
  await delay(100)
  return { list: readList('mock_reviews').filter((r) => r.revieweeId === Number(userId)), total: 0 }
}

// ---------- 图片上传（返回本地 data URL 以便预览） ----------
export function uploadImage(file) {
  return new Promise((resolve) => {
    const reader = new FileReader()
    reader.onload = () => resolve({ url: reader.result })
    reader.readAsDataURL(file)
  })
}