<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<template>
  <div class="not-found">
    <h1>404</h1>
    <p>页面不存在</p>
    <el-button type="primary" @click="router.push('/home')">回首页</el-button>
  </div>
</template>

<style scoped>
/* 占满整屏并用 flex 居中，gap 控制三行间距 */
.not-found {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
h1 {
  font-size: 72px;
  margin: 0;
  color: #c0c4cc;
}
</style>