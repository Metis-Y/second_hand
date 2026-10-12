/**
 * =====================================================================
 * 路由配置
 * ---------------------------------------------------------------------
 * 路由分三层结构：
 *   1. '/'        —— 主框架路由，加载 MainLayout.vue（顶栏 + 内容区 + 底栏），
 *                    所有需要"带导航栏"的页面都作为它的 children 嵌套其中。
 *   2. 独立整页路由 —— /login、/register、404，不套主框架，铺满整屏。
 *
 * meta 字段约定：
 *   · title        —— 页面标题，路由守卫里用它设置 document.title
 *   · requiresAuth —— 是否需要登录，守卫据此拦截未登录用户
 * =====================================================================
 */
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    // 主框架：进入 '/' 自动重定向到首页
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/home',
    // 子路由会被渲染进 MainLayout 内部的 <RouterView />
    children: [
      // 首页：商品列表 + 分类筛选 + 分页
      { path: 'home', name: 'home', component: () => import('@/views/Home.vue'), meta: { title: '首页' } },
      // 商品详情：轮播图 + 商品信息 + 评价列表
      { path: 'product/:id', name: 'product-detail', component: () => import('@/views/ProductDetail.vue'), meta: { title: '商品详情' } },
      // 发布商品（需登录）
      { path: 'publish', name: 'publish', component: () => import('@/views/Publish.vue'), meta: { title: '发布商品', requiresAuth: true } },
      // 编辑商品：复用 Publish.vue 组件，靠路由上有没有 :id 区分"发布"还是"编辑"
      { path: 'edit/:id', name: 'edit-product', component: () => import('@/views/Publish.vue'), meta: { title: '编辑商品', requiresAuth: true } },
      // 我的收藏（需登录）
      { path: 'favorites', name: 'favorites', component: () => import('@/views/Favorites.vue'), meta: { title: '我的收藏', requiresAuth: true } },
      // 我的订单：买家/卖家两个视角（需登录）
      { path: 'orders', name: 'orders', component: () => import('@/views/Orders.vue'), meta: { title: '我的订单', requiresAuth: true } },
      // 个人中心：我发布的商品 + 收到的评价（需登录）
      { path: 'profile', name: 'profile', component: () => import('@/views/Profile.vue'), meta: { title: '个人中心', requiresAuth: true } }
    ]
  },
  // 以下三页为独立整页布局，不经过 MainLayout
  { path: '/login', name: 'login', component: () => import('@/views/Login.vue'), meta: { title: '登录' } },
  { path: '/register', name: 'register', component: () => import('@/views/Register.vue'), meta: { title: '注册' } },
  // 兜底路由：任意未匹配的地址都进 404 页
  { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('@/views/NotFound.vue'), meta: { title: '404' } }
]

const router = createRouter({
  // HTML5 History 模式：地址栏不带 #（需服务端把未知路径回退到 index.html）
  history: createWebHistory(),
  routes,
  // 每次切换页面回到顶部
  scrollBehavior() {
    return { top: 0 }
  }
})

/**
 * 全局前置守卫：在每次跳转前执行
 * 1. 目标页面要求登录（meta.requiresAuth）但没有 token → 跳登录页，
 *    并用 query.redirect 记住原地址，登录成功后可跳回。
 * 2. 否则放行，同时根据 meta.title 更新浏览器标签页标题。
 * @param {import('vue-router').RouteLocationNormalized} to 目标路由
 * @returns {true|object} 返回 true 放行；返回路由对象表示重定向
 */
router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  document.title = to.meta.title ? `${to.meta.title} · 校园二手` : '校园二手交易平台'
  return true
})

export default router