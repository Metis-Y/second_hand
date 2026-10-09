<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProduct, getProductReviews, isFavorite, addFavorite, removeFavorite, createOrder } from '@/api'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const productId = route.params.id
const loading = ref(true)
const product = ref(null)
const reviews = ref([])
const favorited = ref(false)

const statusMap = { 0: '已下架', 1: '在售', 2: '已售出', 3: '审核中' }
const statusType = { 0: 'info', 1: 'success', 2: 'danger', 3: 'warning' }
const statusText = computed(() => (product.value ? statusMap[product.value.status] : ''))
const statusTagType = computed(() => (product.value ? statusType[product.value.status] : 'info'))
const isMine = computed(() => product.value && userStore.user && product.value.userId === userStore.user.id)
const canBuy = computed(() => product.value && product.value.status === 1 && !isMine.value)

async function load() {
  loading.value = true
  try {
    product.value = await getProduct(productId)
    const rev = await getProductReviews(productId)
    reviews.value = rev.list || []
    if (userStore.isLogin) {
      favorited.value = await isFavorite(productId)
    }
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function toggleFavorite() {
  if (!userStore.isLogin) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  try {
    if (favorited.value) {
      await removeFavorite(productId)
      favorited.value = false
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite(productId)
      favorited.value = true
      ElMessage.success('收藏成功')
    }
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

async function buy() {
  if (!userStore.isLogin) {
    router.push({ name: 'login', query: { redirect: route.fullPath } })
    return
  }
  try {
    await createOrder(productId)
    ElMessage.success('下单成功，请到订单页处理')
    router.push('/orders')
  } catch (e) {
    ElMessage.error(e.message || '下单失败')
  }
}

onMounted(load)
</script>

<template>
  <div v-loading="loading" class="page-container">
    <div v-if="product" class="detail">
      <div class="left card">
        <el-carousel v-if="product.images?.length" height="400px" indicator-position="outside">
          <el-carousel-item v-for="(img, i) in product.images" :key="i">
            <el-image :src="img" fit="contain" class="carousel-img" />
          </el-carousel-item>
        </el-carousel>
        <el-empty v-else description="暂无图片" />
      </div>

      <div class="right card">
        <h1 class="title">{{ product.title }}</h1>
        <div class="price-row">
          <span class="price">￥{{ product.price }}</span>
          <el-tag :type="statusTagType" effect="light">{{ statusText }}</el-tag>
        </div>
        <div class="info text-muted">
          分类：{{ product.categoryId }} · 发布时间：{{ product.createTime }}
        </div>

        <div class="seller">
          <el-avatar :size="36" :src="product.seller?.avatar">{{ product.seller?.nickname?.[0] }}</el-avatar>
          <span>{{ product.seller?.nickname }}</span>
        </div>

        <div class="actions">
          <el-button :type="favorited ? 'warning' : 'default'" @click="toggleFavorite">
            <el-icon v-if="favorited"><StarFilled /></el-icon>
            <el-icon v-else><Star /></el-icon>
            {{ favorited ? '已收藏' : '收藏' }}
          </el-button>
          <el-button type="danger" size="large" :disabled="!canBuy" @click="buy">
            {{ isMine ? '这是你发布的商品' : product.status === 1 ? '立即购买' : statusText }}
          </el-button>
        </div>
      </div>
    </div>

    <div v-if="product" class="card section">
      <h3>商品描述</h3>
      <p class="desc">{{ product.description || '暂无描述' }}</p>
    </div>

    <div class="card section">
      <h3>商品评价（{{ reviews.length }}）</h3>
      <el-empty v-if="reviews.length === 0" description="暂无评价" :image-size="80" />
      <div v-for="r in reviews" :key="r.id" class="review-item">
        <div class="review-head">
          <span class="reviewer">{{ r.reviewerNickname || '匿名' }}</span>
          <el-rate :model-value="r.score" disabled />
        </div>
        <p class="review-content">{{ r.content || '（未填写内容）' }}</p>
        <div class="text-muted">{{ r.createTime }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.detail {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 16px;
}
.left {
  padding: 8px;
}
.carousel-img {
  width: 100%;
  height: 400px;
}
.right {
  display: flex;
  flex-direction: column;
}
.title {
  font-size: 22px;
  margin: 0 0 12px;
}
.price-row {
  display: flex;
  align-items: center;
  gap: 12px;
}
.price {
  font-size: 28px;
}
.info {
  margin: 12px 0;
}
.seller {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 8px;
}
.actions {
  margin-top: auto;
  display: flex;
  gap: 12px;
  padding-top: 16px;
}
.section {
  margin-top: 16px;
}
.desc {
  color: #606266;
  line-height: 1.7;
  white-space: pre-wrap;
}
.review-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.review-head {
  display: flex;
  align-items: center;
  gap: 12px;
}
.reviewer {
  font-weight: 600;
}
.review-content {
  color: #606266;
  margin: 6px 0;
}
@media (max-width: 860px) {
  .detail {
    grid-template-columns: 1fr;
  }
}
</style>