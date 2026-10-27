<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUnreadCount } from '@/api'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
// 全局登录状态：顶栏右侧根据 isLogin 切换"头像下拉"或"登录/注册按钮"
const userStore = useUserStore()

// 顶栏搜索框的输入内容
const keyword = ref('')

// 导航菜单高亮项：直接用当前路径匹配 el-menu-item 的 index
const activeMenu = computed(() => route.path)

/**
 * 顶栏搜索：跳到首页并把关键字放进 query。
 * Home.vue 中 watch 到 query.keyword 变化会重新拉取列表。
 */
function doSearch() {
  router.push({ name: 'home', query: keyword.value ? { keyword: keyword.value } : {} })
}

// 未读通知数量：顶栏铃铛上的红点，0 时不显示
const unread = ref(0)

/** 拉取未读数：未登录直接清零，不给后端发无意义的请求 */
async function loadUnread() {
  if (!userStore.isLogin) {
    unread.value = 0
    return
  }
  try {
    unread.value = await getUnreadCount()
  } catch {
    unread.value = 0
  }
}

onMounted(loadUnread)
// 每次切换页面重新拉一次：在消息中心读完回到别的页面，红点能及时消失
watch(() => route.path, loadUnread)
// 登录状态变化时也刷新：刚登录不该显示上一个账号的未读数
watch(() => userStore.isLogin, loadUnread)

/**
 * 用户头像下拉菜单的回调
 * @param {'profile'|'logout'} cmd 被点击项的 command 值
 */
function handleCommand(cmd) {
  if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'logout') {
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/home')
  }
}
</script>

<template>
  <div class="layout">
    <!-- ============ 顶栏：logo + 主导航 + 搜索/用户区 ============ -->
    <header class="header">
      <div class="header-inner">
        <!-- 左：站点 logo，点击返回首页 -->
        <div class="logo" @click="router.push('/home')">
          <span class="logo-badge">二</span>
          <span class="logo-text">校园二手</span>
        </div>

        <!-- 中：横向导航。router 属性使 index 值直接作为跳转地址 -->
        <el-menu
          class="nav"
          mode="horizontal"
          :default-active="activeMenu"
          :ellipsis="false"
          router
        >
          <el-menu-item index="/home">首页</el-menu-item>
          <el-menu-item index="/publish">发布</el-menu-item>
          <el-menu-item index="/favorites">收藏</el-menu-item>
          <el-menu-item index="/orders">订单</el-menu-item>
        </el-menu>

        <!-- 右：搜索框 + 登录态区域 -->
        <div class="header-right">
          <!-- 回车或点击放大镜都会触发搜索；清空输入框也立即回到全部商品 -->
          <el-input
            v-model="keyword"
            class="search"
            placeholder="搜索商品"
            clearable
            @keyup.enter="doSearch"
            @clear="doSearch"
          >
            <template #append>
              <el-button @click="doSearch"><el-icon><Search /></el-icon></el-button>
            </template>
          </el-input>

          <!-- 已登录：消息铃铛 + 未读红点，点头像进个人中心或退出 -->
          <template v-if="userStore.isLogin">
            <el-badge :value="unread" :max="99" :hidden="unread === 0" class="bell">
              <el-icon class="bell-icon" @click="router.push('/notifications')"><Bell /></el-icon>
            </el-badge>
            <el-dropdown @command="handleCommand">
              <span class="user">
                <el-avatar :size="30" :src="userStore.user?.avatar">{{ (userStore.user?.nickname || '用')[0] }}</el-avatar>
                <span class="nickname">{{ userStore.user?.nickname }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <!-- 未登录：显示登录 / 注册按钮 -->
          <template v-else>
            <el-button @click="router.push('/login')">登录</el-button>
            <el-button type="primary" @click="router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <!-- ============ 内容区：子页面在此渲染 ============ -->
    <main class="main">
      <RouterView />
    </main>

    <!-- ============ 底栏 ============ -->
    <footer class="footer">校园二手交易平台 · 仅供学习演示</footer>
  </div>
</template>

<style scoped>
/* 整页纵向三段式：header / main / footer，main 用 flex:1 撑开 */
.layout {
  min-height: 100%;
  display: flex;
  flex-direction: column;
}
/* 顶栏吸附在窗口顶部，滚动页面时始终可见 */
.header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}
/* 顶栏内部限宽居中，与页面内容区（1200px）对齐 */
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  height: 60px;
  gap: 24px;
  padding: 0 16px;
}
.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  flex-shrink: 0;
}
/* logo 左侧方块图标 */
.logo-badge {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  background: var(--sh-primary);
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}
.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}
/* 导航占满中间空隙，把右侧区域推到最右 */
.nav {
  border-bottom: none;
  flex: 1;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.search {
  width: 200px;
}
.user {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #303133;
}
.nickname {
  font-size: 14px;
}
.main {
  flex: 1;
}
.footer {
  text-align: center;
  color: #c0c4cc;
  font-size: 13px;
  padding: 20px;
}
/* 顶栏消息铃铛：与头像同高，hover 变主题色 */
.bell {
  cursor: pointer;
  display: inline-flex;
  align-items: center;
}
.bell-icon {
  font-size: 20px;
  color: #606266;
  transition: color 0.15s;
}
.bell:hover .bell-icon {
  color: var(--sh-primary);
}
</style>