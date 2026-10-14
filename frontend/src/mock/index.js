// =====================================================================
// 前端演示数据（mock）
// ---------------------------------------------------------------------
// 当 .env.development 里 VITE_USE_MOCK=true 时启用，方便后端未就绪时预览页面。
// 后端写完、联调时把该开关改为 false，前端即切换为真实 http 请求（见 api/index.js）。
//
// 重要约定：
//   1. 这里每个函数返回的，就是后端统一响应 { code, message, data } 里的 data。
//   2. 函数签名、参数、返回结构与真实接口保持一致，切换开关后页面无需改动。
//   3. 为了模拟网络延迟，每个函数开头都 await delay()，页面能看到 loading 效果。
//
// 数据存放位置：
//   · categories / users   —— 写死在代码里的基础数据
//   · products             —— 首次访问时用 catalog 播种，之后存 localStorage
//   · 收藏 / 订单 / 评价     —— 全部存 localStorage，键名以 mock_ 开头
//   · 每个用户只能看到自己的收藏和订单，靠 currentUser() 区分身份
// =====================================================================

/** 模拟网络延迟，让页面 loading 状态可见（默认 200ms） */
const delay = (ms = 200) => new Promise((r) => setTimeout(r, ms))
/** 生成 "YYYY-MM-DD HH:mm:ss" 格式的当前时间字符串，与后端返回格式保持一致 */
const now = () => new Date().toISOString().slice(0, 19).replace('T', ' ')

/**
 * 生成本地占位图（SVG data URI，离线也能显示，无需任何图片文件）
 * @param {string} text 图上显示的文字（最多取 8 个字）
 * @param {string} bg   背景色
 * @returns {string} 可直接用作 img src 的 data URL
 */
function svgImg(text, bg = '#97b8e6') {
  const safe = String(text || '').slice(0, 8)
  const svg =
    '<svg xmlns="http://www.w3.org/2000/svg" width="400" height="300">' +
    `<rect width="400" height="300" fill="${bg}"/>` +
    `<text x="200" y="150" font-family="sans-serif" font-size="26" fill="#ffffff" text-anchor="middle" dominant-baseline="middle">${safe}</text>` +
    '</svg>'
  return 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg)
}

/** 每个分类一种配色，让占位图和商品图片有区分度 */
const categoryColors = { 1: '#5b8ff9', 2: '#5ad8a6', 3: '#f6bd16', 4: '#9270ca' }

// ---------- 基础数据 ----------

/** 商品分类：id 与选框、筛选栏一一对应 */
const categories = [
  { id: 1, name: '教材', image: svgImg('教材', categoryColors[1]), sort: 0, createTime: now() },
  { id: 2, name: '电子产品', image: svgImg('电子', categoryColors[2]), sort: 1, createTime: now() },
  { id: 3, name: '生活用品', image: svgImg('生活', categoryColors[3]), sort: 2, createTime: now() },
  { id: 4, name: '其他', image: svgImg('其他', categoryColors[4]), sort: 3, createTime: now() }
]

/**
 * 演示账号（密码统一 123456）
 * role: 1 = 管理员，0 = 普通用户
 * 说明：这里只是前端演示用的假账号，真实密码校验在后端完成。
 */
const users = [
  { id: 1, username: 'admin', password: '123456', nickname: '管理员', phone: '13800000000', gender: 1, role: 1, avatar: svgImg('管') },
  { id: 2, username: 'xiaoming', password: '123456', nickname: '小明', phone: '13811112222', gender: 1, role: 0, avatar: svgImg('明') },
  { id: 3, username: 'xiaohong', password: '123456', nickname: '小红', phone: '13833334444', gender: 2, role: 0, avatar: svgImg('红') }
]

/**
 * 商品种子数据：首次打开站点时用来生成初始商品列表。
 * seller 字段是发布者的用户 id（2 = 小明，3 = 小红）。
 */
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

/**
 * 读取一个列表型 localStorage 键（不存在或内容损坏时返回空数组）
 * @param {string} key 存储键名
 */
function readList(key) {
  try { return JSON.parse(localStorage.getItem(key) || '[]') } catch { return [] }
}
/** 把一个列表写入 localStorage */
function writeList(key, list) {
  localStorage.setItem(key, JSON.stringify(list))
}

/**
 * 为商品生成 count 张同色系占位图，模拟"多图商品"
 * @param {string} title 商品标题（取前几个字印在图上）
 * @param {number} categoryId 分类 id，决定配色
 * @param {number} count 图片张数
 */
function buildImages(title, categoryId, count = 3) {
  const c = categoryColors[categoryId] || '#97b8e6'
  const arr = []
  for (let i = 0; i < count; i++) arr.push(svgImg(`${title.slice(0, 5)} ${i + 1}`, c))
  return arr
}

/**
 * 把种子数据 catalog 中的一条，转换成完整的商品对象（补 id、图片、时间、状态等字段）
 * @param {object} s catalog 中的一条记录
 * @param {number} id 分配的商品 id
 */
function buildProduct(s, id) {
  const images = buildImages(s.title, s.categoryId, 3)
  return {
    id,
    userId: s.seller,
    categoryId: s.categoryId,
    title: s.title,
    description: s.description,
    price: s.price,
    cover: images[0],        // 列表页封面取第一张图
    status: 1,               // 1 = 在售
    createTime: now(),
    updateTime: now(),
    images
  }
}

/**
 * 加载商品列表：优先读 localStorage；没有则用 catalog 播种后存回去。
 * 这样用户发布的商品、状态变更在刷新后不会丢失。
 */
function loadProducts() {
  const raw = localStorage.getItem('mock_products')
  if (raw) { try { return JSON.parse(raw) } catch { /* ignore */ } }
  const seeded = catalog.map((s, i) => buildProduct(s, i + 1))
  localStorage.setItem('mock_products', JSON.stringify(seeded))
  return seeded
}

// 模块级可变状态：进程内共用一份商品数组
let products = loadProducts()
// 新增商品 id 从 1000 之后开始，避免与种子数据 1..14 冲突
let nextProductId = products.length + 1000

/** 把内存中的商品数组同步回 localStorage */
function saveProducts() { writeList('mock_products', products) }

/** 由用户 id 查昵称（商品的 nickname 字段就是靠它拼出来的） */
function nicknameOf(userId) {
  return users.find((u) => u.id === Number(userId))?.nickname || '用户'
}
/** 由用户 id 查头像地址 */
function avatarOf(userId) {
  return users.find((u) => u.id === Number(userId))?.avatar || svgImg('户')
}
/** 由商品 id 查商品对象（找不到返回 undefined） */
function findProduct(id) {
  return products.find((p) => p.id === Number(id))
}
/**
 * 取"当前登录用户"：从 localStorage 的 user 反查完整用户记录。
 * 找不到时兜底返回 xiaoming（users[1]），保证未登录也能演示浏览类页面。
 */
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
/** 把用户记录转成返回给前端的 UserDTO（token + 基础信息，去掉密码/手机号/性别） */
function toUserDTO(u) {
  return { id: u.id, username: u.username, nickname: u.nickname, avatar: u.avatar, role: u.role }
}

// ---------- 认证 ----------

/**
 * 登录：校验用户名 + 密码，成功返回 token 与用户信息（不含密码）
 * @param {{username: string, password: string}} 登录表单
 * @throws {Error} 用户名或密码错误
 */
export async function login({ username, password }) {
  await delay()
  const u = users.find((x) => x.username === username && x.password === password)
  if (!u) throw new Error('用户名或密码错误')
  return { token: `mock-token-${u.id}`, ...toUserDTO(u) }
}

/**
 * 注册：用户名查重 → 追加到 users 数组（仅本次会话内存有效，刷新即失效）
 * @param {{username: string, password: string, nickname?: string, phone?: string, gender?: number}} 注册表单
 * @throws {Error} 用户名已存在
 */
export async function register({ username, password, nickname, phone, gender }) {
  await delay()
  if (users.some((x) => x.username === username)) throw new Error('用户名已存在')
  const u = {
    id: Date.now(), username, password,
    nickname: nickname || username, phone: phone || '',
    gender: gender ?? null, role: 0, avatar: svgImg('新')
  }
  users.push(u)
  return { token: `mock-token-${u.id}`, ...toUserDTO(u) }
}

// ---------- 分类 ----------

/** 获取全部分类 */
export async function getCategories() {
  await delay(100)
  return categories
}

// ---------- 商品 ----------

/**
 * 分页查询商品列表（模拟后端的筛选 + 排序 + 分页）
 * @param {{page?: number, size?: number, keyword?: string, categoryId?: number|null, status?: number}} 查询条件
 * @returns {{list: Array, total: number}} list 中每条都补了 nickname 卖家昵称
 */
export async function getProducts({ page = 1, size = 12, keyword = '', categoryId = null, status } = {}) {
  await delay()
  // 新发布的排前面（按创建时间倒序）
  let list = [...products].sort((a, b) => (b.createTime > a.createTime ? 1 : -1))
  if (categoryId) list = list.filter((p) => p.categoryId === Number(categoryId))
  if (keyword) list = list.filter((p) => p.title.includes(keyword) || p.description.includes(keyword))
  if (status !== undefined && status !== null && status !== '') {
    list = list.filter((p) => p.status === Number(status))
  } else {
    list = list.filter((p) => p.status === 1) // 首页默认只展示「在售」
  }
  // 先取总数再切片，total 用于前端分页组件
  const total = list.length
  const start = (Number(page) - 1) * Number(size)
  const pageList = list.slice(start, start + Number(size)).map((p) => ({ ...p, nickname: nicknameOf(p.userId) }))
  return { list: pageList, total }
}

/**
 * 查询商品详情：比列表多返回 seller 对象（头像 + 昵称）
 * @throws {Error} 商品不存在
 */
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

/**
 * 发布商品：以当前登录用户为卖家，新商品插到列表最前面
 * 没传图片时自动生成一张标题占位图，保证列表页有图可显示
 */
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

/**
 * 修改商品：只更新可编辑字段；只有传了新图片才替换图集与封面
 * @throws {Error} 商品不存在
 */
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

/** 删除商品（真删除，同时从 localStorage 移除） */
export async function deleteProduct(id) {
  await delay()
  products = products.filter((p) => p.id !== Number(id))
  saveProducts()
  return true
}

/** 查询"我发布的"商品：按当前登录用户过滤 */
export async function getMyProducts() {
  await delay()
  const u = currentUser()
  return { list: products.filter((p) => p.userId === u.id), total: products.filter((p) => p.userId === u.id).length }
}

/**
 * 内部函数：把商品标记为"已售出"（status = 2）
 * 下单成功时调用，避免同一商品被重复购买。
 */
function markSold(productId) {
  const p = findProduct(productId)
  if (p) {
    p.status = 2
    p.updateTime = now()
    saveProducts()
  }
}

// ---------- 收藏 ----------

/**
 * 查询当前用户的收藏列表。
 * 收藏只存 { userId, productId, createTime }，这里再关联出商品完整信息；
 * 已删除的商品会被过滤掉（map 返回 null 后被 filter(Boolean) 去掉）。
 */
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

/** 添加收藏（已收藏过则直接返回 true，不重复写入） */
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

/** 取消收藏：过滤掉当前用户对该商品的收藏记录 */
export async function removeFavorite(productId) {
  await delay(80)
  const u = currentUser()
  const favs = readList('mock_favorites').filter((f) => !(f.userId === u.id && f.productId === Number(productId)))
  writeList('mock_favorites', favs)
  return true
}

/**
 * 判断当前用户是否已收藏某商品（同步返回，详情页初始化用）
 * @returns {boolean}
 */
export function isFavorite(productId) {
  const u = currentUser()
  return readList('mock_favorites').some((f) => f.userId === u.id && f.productId === Number(productId))
}

// ---------- 订单 ----------

/**
 * 下单：校验商品存在、不能买自己的、商品必须在售；
 * 下单成功后把商品置为"已售出"，新订单状态为 0（待确认）。
 * @throws {Error} 商品不存在 / 不能购买自己发布的商品 / 该商品当前不可购买
 */
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
    // 冗余存双方昵称，列表页就不用再逐个查用户
    buyerNickname: u.nickname, sellerNickname: nicknameOf(p.userId)
  }
  orders.unshift(order)
  writeList('mock_orders', orders)
  markSold(p.id)
  return order
}

/**
 * 查询订单列表
 * @param {{role?: 'buyer'|'seller', status?: number|string}} 查询条件
 *        role=buyer 查 buyerId 是我；role=seller 查 sellerId 是我
 * @returns {{list: Array, total: number}} 每条订单附带 product 商品信息
 */
export async function getOrders({ role = 'buyer', status } = {}) {
  await delay()
  const u = currentUser()
  let list = readList('mock_orders').filter((o) => (role === 'buyer' ? o.buyerId === u.id : o.sellerId === u.id))
  if (status !== undefined && status !== null && status !== '') list = list.filter((o) => o.status === Number(status))
  // 关联商品；商品已被删除时给个占位对象，保证列表不报错
  list = list.map((o) => ({ ...o, product: findProduct(o.productId) || { title: '商品已删除' } }))
  return { list, total: list.length }
}

/**
 * 修改订单状态（0 待确认 / 1 交易中 / 2 已完成 / 3 已取消）
 * @throws {Error} 订单不存在
 */
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

/**
 * 提交评价：校验订单存在、同一订单同一人只能评一次。
 * revieweeId 自动推断为"交易对方"：我是买家就评卖家，我是卖家就评买家。
 * @param {{orderId: number, score: number, content?: string}}
 * @throws {Error} 订单不存在 / 该订单已评价过
 */
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

/** 查询某商品下的全部评价（详情页评价区） */
export async function getProductReviews(productId) {
  await delay(100)
  return { list: readList('mock_reviews').filter((r) => r.productId === Number(productId)), total: 0 }
}

/** 查询某用户"收到的"评价（个人中心页签），即 revieweeId 是我的记录 */
export async function getUserReviews(userId) {
  await delay(100)
  return { list: readList('mock_reviews').filter((r) => r.revieweeId === Number(userId)), total: 0 }
}

// ---------- 图片上传（返回本地 data URL 以便预览） ----------

/**
 * 模拟图片上传：用 FileReader 把图片读成 base64 data URL 直接用作预览地址。
 * 好处是无需后端、不产生网络请求；坏处是数据存在内存/localStorage 里较大，
 * 真实环境应走 POST /api/upload 拿服务器地址。
 * @param {File} file
 * @returns {Promise<{url: string}>}
 */
export function uploadImage(file) {
  return new Promise((resolve) => {
    const reader = new FileReader()
    reader.onload = () => resolve({ url: reader.result })
    reader.readAsDataURL(file)
  })
}