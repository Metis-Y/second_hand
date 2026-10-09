import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/home',
    children: [
      { path: 'home', name: 'home', component: () => import('@/views/Home.vue'), meta: { title: '首页' } },
      { path: 'product/:id', name: 'product-detail', component: () => import('@/views/ProductDetail.vue'), meta: { title: '商品详情' } },
      { path: 'publish', name: 'publish', component: () => import('@/views/Publish.vue'), meta: { title: '发布商品', requiresAuth: true } },
      { path: 'edit/:id', name: 'edit-product', component: () => import('@/views/Publish.vue'), meta: { title: '编辑商品', requiresAuth: true } },
      { path: 'favorites', name: 'favorites', component: () => import('@/views/Favorites.vue'), meta: { title: '我的收藏', requiresAuth: true } },
      { path: 'orders', name: 'orders', component: () => import('@/views/Orders.vue'), meta: { title: '我的订单', requiresAuth: true } },
      { path: 'profile', name: 'profile', component: () => import('@/views/Profile.vue'), meta: { title: '个人中心', requiresAuth: true } }
    ]
  },
  { path: '/login', name: 'login', component: () => import('@/views/Login.vue'), meta: { title: '登录' } },
  { path: '/register', name: 'register', component: () => import('@/views/Register.vue'), meta: { title: '注册' } },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('@/views/NotFound.vue'), meta: { title: '404' } }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 路由守卫：需要登录的页面，未登录跳转登录页
router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  document.title = to.meta.title ? `${to.meta.title} · 校园二手` : '校园二手交易平台'
  return true
})

export default router