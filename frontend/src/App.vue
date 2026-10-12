<!--
  App.vue —— 根组件
  布局：不渲染任何自有 DOM，整页结构完全由路由决定。
    · 带顶栏/底栏的页面 → 匹配 / 下的 MainLayout.vue
    · 登录、注册、404     → 独立整页布局（不带顶栏）
-->
<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <!-- 路由出口：根据当前 URL 渲染对应的页面组件 -->
  <RouterView />
</template>