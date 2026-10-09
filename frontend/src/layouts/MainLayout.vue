<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const keyword = ref('')

const activeMenu = computed(() => route.path)

function doSearch() {
  router.push({ name: 'home', query: keyword.value ? { keyword: keyword.value } : {} })
}

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
    <header class="header">
      <div class="header-inner">
        <div class="logo" @click="router.push('/home')">
          <span class="logo-badge">二</span>
          <span class="logo-text">校园二手</span>
        </div>

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

        <div class="header-right">
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

          <template v-if="userStore.isLogin">
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
          <template v-else>
            <el-button @click="router.push('/login')">登录</el-button>
            <el-button type="primary" @click="router.push('/register')">注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <main class="main">
      <RouterView />
    </main>

    <footer class="footer">校园二手交易平台 · 仅供学习演示</footer>
  </div>
</template>

<style scoped>
.layout {
  min-height: 100%;
  display: flex;
  flex-direction: column;
}
.header {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  z-index: 100;
}
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
</style>