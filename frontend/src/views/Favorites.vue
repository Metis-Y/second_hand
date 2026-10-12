<!--
  Favorites.vue —— 我的收藏页        路由：/favorites（需登录）
  =====================================================================
  布局（限宽 1200px）：
  ┌───────────────────────────────────────────────────────┐
  │  标题「我的收藏」                                       │
  ├───────────────────────────────────────────────────────┤
  │  grid 商品网格（宽屏 4 列，≤960px 降 2 列）             │
  │    每个格子 = ProductCard 商品卡片                      │
  │              + 右上角「取消收藏」浮层按钮                │
  │  没有收藏时显示 el-empty「还没有收藏任何商品」           │
  └───────────────────────────────────────────────────────┘
  说明：卡片本身点击进详情，只有右上角的胶囊按钮才是取消收藏。
-->
<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getFavorites, removeFavorite } from '@/api'
import ProductCard from '@/components/ProductCard.vue'

const list = ref([])        // 收藏的商品列表
const loading = ref(false)  // 加载遮罩

/** 拉取当前用户的收藏列表 */
async function load() {
  loading.value = true
  try {
    const res = await getFavorites()
    list.value = res.list
  } finally {
    loading.value = false
  }
}

/**
 * 取消收藏：调用接口成功后重新拉取列表（让该商品从网格中消失）
 * @param {{id: number}} item 收藏列表中的商品对象
 */
async function cancel(item) {
  try {
    await removeFavorite(item.id)
    ElMessage.success('已取消收藏')
    load()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <h2>我的收藏</h2>
    <!-- 空状态与网格互斥显示；加载中显示遮罩但不闪现空状态 -->
    <el-empty v-if="!loading && list.length === 0" description="还没有收藏任何商品" />
    <div v-else v-loading="loading" class="grid">
      <!-- 外层包一层用于给卡片叠一个「取消收藏」浮层按钮 -->
      <div v-for="item in list" :key="item.id" class="fav-item">
        <span class="cancel" @click="cancel(item)">取消收藏</span>
        <ProductCard :product="item" />
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 与首页保持一致的 4 列商品网格 */
.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  min-height: 200px;
}
/* 定位上下文：让 .cancel 相对卡片定位 */
.fav-item {
  position: relative;
}
/* 取消收藏胶囊：叠在卡片右上角，z-index 高于卡片保证可点 */
.cancel {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 5;
  background: rgba(0, 0, 0, 0.45);
  color: #fff;
  font-size: 12px;
  padding: 3px 8px;
  border-radius: 10px;
  cursor: pointer;
}
.cancel:hover {
  background: #f56c6c;
}
/* 窄屏降为 2 列 */
@media (max-width: 960px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>