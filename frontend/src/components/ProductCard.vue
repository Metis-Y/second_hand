<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addFavorite, removeFavorite } from '@/api'
import { useUserStore } from '@/stores/user'

// 父组件必须传入完整的商品对象：id / title / price / cover / status / sellerNickname / sellerAvatar / favorited
const props = defineProps({
  product: { type: Object, required: true },
  // 是否展示收藏心形按钮（首页开启，收藏页用自己的「取消收藏」按钮，不开启）
  showFavorite: { type: Boolean, default: false }
})
const router = useRouter()
const userStore = useUserStore()

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

/** 点击卖家昵称：进入卖家个人主页（用 .stop 阻止冒泡到整卡点击） */
function goSeller() {
  router.push(`/user/${props.product.userId}`)
}

/** 收藏 / 取消收藏：未登录先跳登录，已登录调接口并在本地翻转状态 */
async function toggleFavorite() {
  if (!userStore.isLogin) {
    ElMessage.warning('请先登录')
    router.push({ name: 'login', query: { redirect: router.currentRoute.value.fullPath } })
    return
  }
  try {
    if (props.product.favorited) {
      await removeFavorite(props.product.id)
      props.product.favorited = false
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite(props.product.id)
      props.product.favorited = true
      ElMessage.success('已收藏')
    }
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}
</script>

<template>
  <!-- 整张卡片绑定点击事件，hover 时上浮（样式见 .product-card:hover） -->
  <div class="product-card card" @click="goDetail">
    <!-- 封面图；图片地址失效时走 #error 插槽，显示标题首字占位 -->
    <el-image class="cover" :src="product.cover" fit="cover" lazy>
      <template #error>
        <div class="img-fallback">{{ product.title?.[0] || '图' }}</div>
      </template>
    </el-image>
    <!-- 收藏心形按钮：仅当父组件传 show-favorite 时展示 -->
    <button
      v-if="showFavorite"
      class="fav-btn"
      :class="{ on: product.favorited }"
      :title="product.favorited ? '取消收藏' : '收藏'"
      @click.stop="toggleFavorite"
    >{{ product.favorited ? '♥' : '♡' }}</button>
    <div class="body">
      <!-- 标题固定两行高度，保证同排卡片对齐 -->
      <div class="title">{{ product.title }}</div>
      <div class="meta">
        <span class="price">￥{{ product.price }}</span>
        <el-tag :type="statusTagType" size="small" effect="light">{{ statusText }}</el-tag>
      </div>
      <div class="text-muted seller" @click.stop="goSeller">
        <el-avatar :size="20" :src="product.sellerAvatar">{{ product.sellerNickname?.[0] }}</el-avatar>
        <span>{{ product.sellerNickname }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.product-card {
  position: relative;
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s;
  padding: 0;
  overflow: hidden;
}
/* 收藏心形按钮：叠在封面右上角，红心=已收藏 */
.fav-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 3;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.35);
  color: #fff;
  font-size: 15px;
  line-height: 28px;
  text-align: center;
  cursor: pointer;
}
.fav-btn.on {
  color: #f56c6c;
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
/* 卖家头像 + 昵称可点，hover 高亮提示可进入卖家主页 */
.seller {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
}
.seller:hover {
  color: var(--sh-primary);
}
</style>