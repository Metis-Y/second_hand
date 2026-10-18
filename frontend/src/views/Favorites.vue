<!--
  Favorites.vue —— 我的收藏页        路由：/favorites（需登录）
  =====================================================================
  布局（限宽 1200px）：
  ┌───────────────────────────────────────────────────────┐
  │  标题「我的收藏」                                       │
  ├───────────────────────────────────────────────────────┤
  │  grid 商品网格（宽屏 4 列，≤960px 降 2 列）             │
  │    每个格子渲染收藏记录自带字段：cover/title/price/status │
  │              + 右上角「取消收藏」浮层按钮                │
  │  没有收藏时显示 el-empty「还没有收藏任何商品」           │
  └───────────────────────────────────────────────────────┘
  说明：后端 getFavorites 联表 favorite+product，返回收藏对象（id/productId +
  商品 title/price/cover/status + favorited）；卡片进详情、取消收藏都用 productId。
-->
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getFavorites, removeFavorite } from '@/api'

const router = useRouter()
const list = ref([])        // 收藏记录列表（含联表商品字段）
const loading = ref(false)  // 加载遮罩

// 商品状态码 → 中文文案 / 标签配色，与后端约定保持一致
const statusMap = { 0: '已下架', 1: '在售', 2: '已售出', 3: '审核中' }
const statusType = { 0: 'info', 1: 'success', 2: 'danger', 3: 'warning' }
const statusText = (s) => statusMap[s] ?? ''
const tagType = (s) => statusType[s] ?? 'info'

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

/** 点击卡片：进入该商品的详情页（收藏记录里的商品 ID 在 productId 上） */
function goDetail(item) {
  router.push(`/product/${item.productId}`)
}

/**
 * 取消收藏：按商品ID删除，成功后重新拉取列表
 * @param {{productId: number}} item 收藏记录对象
 */
async function cancel(item) {
  try {
    await removeFavorite(item.productId)
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
      <div v-for="item in list" :key="item.id" class="fav-item" @click="goDetail(item)">
        <span class="cancel" @click.stop="cancel(item)">取消收藏</span>
        <el-image class="cover" :src="item.cover" fit="cover" lazy>
          <template #error>
            <div class="img-fallback">{{ item.title?.[0] || '图' }}</div>
          </template>
        </el-image>
        <div class="body">
          <div class="title">{{ item.title }}</div>
          <div class="meta">
            <span class="price">￥{{ item.price }}</span>
            <el-tag :type="tagType(item.status)" size="small" effect="light">{{ statusText(item.status) }}</el-tag>
          </div>
        </div>
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
/* 定位上下文 + 卡片外观：让 .cancel 相对卡片定位，整卡可点 */
.fav-item {
  position: relative;
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
  padding: 0;
  overflow: hidden;
  background: #fff;
  border-radius: 8px;
}
.fav-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
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
.cover {
  width: 100%;
  height: 160px;
  display: block;
}
/* 图片加载失败时的灰色占位块 */
.img-fallback {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eef1f6;
  color: #909399;
  font-size: 32px;
}
.body {
  padding: 10px 12px 14px;
}
/* 标题固定 42px 高（两行 × 21px），超出用 -webkit-line-clamp 截断 */
.title {
  font-size: 15px;
  color: #303133;
  height: 42px;
  line-height: 21px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}
/* 价格与状态标签左右分布 */
.meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 6px 0 4px;
}
.price {
  font-size: 17px;
}
/* 窄屏降为 2 列 */
@media (max-width: 960px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>