<!--
  ProductCard.vue —— 商品卡片（可复用组件）
  =====================================================================
  使用场景：首页商品宫格、我的收藏页
  卡片布局（上下结构，整卡可点击进入详情）：
  ┌──────────────┐
  │   cover      │  商品封面图（160px 高，加载失败时降级为标题首字）
  ├──────────────┤
  │   title      │  商品标题（最多两行，超出省略）
  │ ￥price  tag │  价格 + 状态标签（在售/已售出/已下架/审核中）
  │  nickname    │  卖家昵称（灰色小字）
  └──────────────┘
  通过 props.product 接收数据，不关心数据来源（首页或收藏页均可复用）。
-->
<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

// 父组件必须传入完整的商品对象：id / title / price / cover / status / nickname
const props = defineProps({
  product: { type: Object, required: true }
})
const router = useRouter()

// 商品状态码 → 中文文案 / 标签配色，与后端约定保持一致
const statusMap = { 0: '已下架', 1: '在售', 2: '已售出', 3: '审核中' }
const statusType = { 0: 'info', 1: 'success', 2: 'danger', 3: 'warning' }

// 未知状态码时用 ?? 兜底，避免标签显示空白或报错
const statusText = computed(() => statusMap[props.product.status] ?? '')
const statusTagType = computed(() => statusType[props.product.status] ?? 'info')

/** 点击卡片：跳转到该商品的详情页 */
function goDetail() {
  router.push(`/product/${props.product.id}`)
}
</script>

<template>
  <!-- 整张卡片绑定点击事件，hover 时上浮（样式见 .product-card:hover） -->
  <div class="product-card card" @click="goDetail">
    <!-- 封面图；图片地址失效时走 #error 插槽，显示标题首字占位 -->
    <el-image class="cover" :src="product.cover" fit="cover">
      <template #error>
        <div class="img-fallback">{{ product.title?.[0] || '图' }}</div>
      </template>
    </el-image>
    <div class="body">
      <!-- 标题固定两行高度，保证同排卡片对齐 -->
      <div class="title">{{ product.title }}</div>
      <div class="meta">
        <span class="price">￥{{ product.price }}</span>
        <el-tag :type="statusTagType" size="small" effect="light">{{ statusText }}</el-tag>
      </div>
      <div class="text-muted">{{ product.nickname }}</div>
    </div>
  </div>
</template>

<style scoped>
.product-card {
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
  padding: 0;
  overflow: hidden;
}
/* hover 上浮 + 阴影加强，提示可点击 */
.product-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
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
/* 标题固定 42px 高（两行 × 21px），超出部分用 -webkit-line-clamp 截断 */
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
</style>