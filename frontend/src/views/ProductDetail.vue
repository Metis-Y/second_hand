<!--
  ProductDetail.vue —— 商品详情页      路由：/product/:id
  =====================================================================
  页面布局（自上而下，限宽 1200px）：
  ┌───────────────────────────────────────────────────────────┐
  │ detail 主区：左右两栏（宽屏 1.4fr : 1fr，窄屏 ≤860px 变单列）│
  │  ┌──────────────────────┐  ┌──────────────────────────┐   │
  │  │ left 图片轮播         │  │ right 商品信息           │   │
  │  │  400px 高的 el-carousel│  │  标题                    │   │
  │  │  无图显示 el-empty    │  │  价格 + 状态标签          │   │
  │  │                      │  │  分类 & 发布时间          │   │
  │  │                      │  │  卖家头像/昵称            │   │
  │  │                      │  │  [收藏] [立即购买]        │   │
  │  └──────────────────────┘  └──────────────────────────┘   │
  ├───────────────────────────────────────────────────────────┤
  │ section 商品描述（保留换行的纯文本）                        │
  ├───────────────────────────────────────────────────────────┤
  │ section 商品评价（条数 + 列表：评价人 / 星级 / 内容 / 时间） │
  └───────────────────────────────────────────────────────────┘
  权限相关的按钮禁用逻辑（isMine / canBuy）：
    · 自己发布的商品 → 购买按钮禁用并提示"这是你发布的商品"
    · 商品不在售（已售出 / 下架 / 审核中）→ 按钮禁用并显示状态文案
    · 未登录点收藏或购买 → 先跳登录页，并记住当前地址以便登录后返回
-->
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProduct, getProductReviews, isFavorite, addFavorite, removeFavorite, createOrder } from '@/api'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 商品 id 来自路由参数（/product/:id）
const productId = route.params.id
const loading = ref(true)      // 首屏加载遮罩
const product = ref(null)      // 商品详情对象（含 images、seller）
const reviews = ref([])        // 该商品的评价列表
const favorited = ref(false)   // 当前用户是否已收藏

// 商品状态码 → 文案 / 标签配色
const statusMap = { 0: '已下架', 1: '在售', 2: '已售出', 3: '审核中' }
const statusType = { 0: 'info', 1: 'success', 2: 'danger', 3: 'warning' }
const statusText = computed(() => (product.value ? statusMap[product.value.status] : ''))
const statusTagType = computed(() => (product.value ? statusType[product.value.status] : 'info'))
/** 是否是自己发布的商品：以 userId 与当前登录用户 id 比较 */
const isMine = computed(() => product.value && userStore.user && product.value.userId === userStore.user.id)
/** 是否可购买：商品在售、且不是我自己的商品 */
const canBuy = computed(() => product.value && product.value.status === 1 && !isMine.value)

/**
 * 加载详情：依次取商品详情、商品评价；已登录时再查收藏状态。
 * 收藏状态只在登录后查询，未登录保持 false，避免无谓的 401。
 */
async function load() {
  loading.value = true
  try {
    product.value = await getProduct(productId)
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
    loading.value = false
    return
  }
  try { const rev = await getProductReviews(productId); reviews.value = rev.list || [] } catch { /* 评价接口未就绪 */ }
  if (userStore.isLogin) {
    try { favorited.value = await isFavorite(productId) } catch { /* 收藏接口未就绪 */ }
  }
  loading.value = false
}

/**
 * 收藏 / 取消收藏切换。
 * 未登录先跳登录页（带 redirect 参数，登录后可回到本页）；
 * 已登录则按当前状态调用添加或移除接口，并同步 favorited。
 */
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

/**
 * 立即购买：未登录先跳登录，成功下单后引导去订单页跟进交易。
 * 后台会把商品置为"已售出"，避免重复下单。
 */
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
    <!-- ===== 主区：左图右信息 ===== -->
    <div v-if="product" class="detail">
      <!-- 左栏：图片轮播，无图片时显示空状态 -->
      <div class="left card">
        <el-carousel v-if="product.images?.length" height="400px" indicator-position="outside">
          <el-carousel-item v-for="(img, i) in product.images" :key="i">
            <el-image :src="img" fit="contain" class="carousel-img" />
          </el-carousel-item>
        </el-carousel>
        <el-empty v-else description="暂无图片" />
      </div>

      <!-- 右栏：标题、价格、状态、卖家、操作按钮 -->
      <div class="right card">
        <h1 class="title">{{ product.title }}</h1>
        <div class="price-row">
          <span class="price">￥{{ product.price }}</span>
          <el-tag :type="statusTagType" effect="light">{{ statusText }}</el-tag>
        </div>
        <div class="info text-muted">
          分类：{{ product.categoryName || '未知' }} · 发布时间：{{ product.createTime }}
        </div>

        <!-- 卖家信息条 -->
        <div class="seller">
          <el-avatar :size="36" :src="product.seller?.avatar">{{ product.seller?.nickname?.[0] }}</el-avatar>
          <span>{{ product.seller?.nickname }}</span>
        </div>

        <!-- 操作区：收藏（星标随状态切换实心/空心）+ 购买 -->
        <div class="actions">
          <el-button :type="favorited ? 'warning' : 'default'" @click="toggleFavorite">
            <el-icon v-if="favorited"><StarFilled /></el-icon>
            <el-icon v-else><Star /></el-icon>
            {{ favorited ? '已收藏' : '收藏' }}
          </el-button>
          <!-- 按钮文案按优先级：自己的商品 > 在售 > 其他状态 -->
          <el-button type="danger" size="large" :disabled="!canBuy" @click="buy">
            {{ isMine ? '这是你发布的商品' : product.status === 1 ? '立即购买' : statusText }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- ===== 商品描述区 ===== -->
    <div v-if="product" class="card section">
      <h3>商品描述</h3>
      <p class="desc">{{ product.description || '暂无描述' }}</p>
    </div>

    <!-- ===== 商品评价区：列表逐条展示评价人、星级、内容、时间 ===== -->
    <div class="card section">
      <h3>商品评价（{{ reviews.length }}）</h3>
      <el-empty v-if="reviews.length === 0" description="暂无评价" :image-size="80" />
      <div v-for="r in reviews" :key="r.id" class="review-item">
        <div class="review-head">
          <span class="reviewer">{{ r.reviewerNickname || '匿名' }}</span>
          <!-- disabled：详情页只读展示，不可修改评分 -->
          <el-rate :model-value="r.score" disabled />
        </div>
        <p class="review-content">{{ r.content || '（未填写内容）' }}</p>
        <div class="text-muted">{{ r.createTime }}</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 主区两栏：图片更宽（1.4 : 1） */
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
/* 右栏纵向排列，配合 .actions 的 margin-top:auto 把按钮组压到底部 */
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
/* 卖家信息用浅灰底块与周围内容区分 */
.seller {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  background: #f7f8fa;
  border-radius: 8px;
}
/* margin-top:auto 让操作按钮始终贴在右栏底部 */
.actions {
  margin-top: auto;
  display: flex;
  gap: 12px;
  padding-top: 16px;
}
.section {
  margin-top: 16px;
}
/* pre-wrap 保留描述里的换行与空格 */
.desc {
  color: #606266;
  line-height: 1.7;
  white-space: pre-wrap;
}
/* 单条评价：下划线分隔 */
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
/* 窄屏改为上下单列，图片在上信息在下 */
@media (max-width: 860px) {
  .detail {
    grid-template-columns: 1fr;
  }
}
</style>