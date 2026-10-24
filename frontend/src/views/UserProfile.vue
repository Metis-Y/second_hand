<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getUser, getProducts, getUserReviews } from '@/api'
import ProductCard from '@/components/ProductCard.vue'

const route = useRoute()
const userId = route.params.id

const activeTab = ref('products')  // 当前页签：products 在售商品 / reviews 收到的评价
const profile = ref(null)          // 用户资料（含手机号/性别）
const products = ref([])           // TA 的在售商品
const reviews = ref([])            // TA 收到的评价
const loading = ref(false)

// 性别码 → 文案（1 男、2 女；0/空 不显示）
const genderMap = { 1: '男', 2: '女' }
const genderText = computed(() => genderMap[profile.value?.gender] || '')

/** 按当前页签加载数据（资料卡单独加载，页签数据按需加载） */
async function load() {
  loading.value = true
  try {
    if (activeTab.value === 'products') {
      const res = await getProducts({ userId, status: 1, page: 1, size: 100 })
      products.value = res.list
    } else {
      const res = await getUserReviews(userId)
      reviews.value = res.list
    }
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

/** 页签切换时重新加载数据 */
function onTabChange() {
  load()
}

onMounted(async () => {
  try {
    profile.value = await getUser(userId)
  } catch (e) {
    ElMessage.error(e.message || '加载失败')
  }
  load()
})
</script>

<template>
  <div class="page-container">
    <!-- ===== 资料卡：头像 + 昵称 + 角色 + 性别 + 用户名/手机号 ===== -->
    <div class="card user-card">
      <el-avatar :size="72" :src="profile?.avatar">{{ profile?.nickname?.[0] }}</el-avatar>
      <div class="uinfo">
        <div class="uname">
          {{ profile?.nickname }}
          <el-tag v-if="profile?.role === 1" type="danger" size="small" effect="light">管理员</el-tag>
          <el-tag v-if="genderText" size="small" effect="plain">{{ genderText }}</el-tag>
        </div>
        <div class="text-muted">@{{ profile?.username }}</div>
        <div v-if="profile?.phone" class="text-muted">手机号：{{ profile.phone }}</div>
      </div>
    </div>

    <!-- ===== 两个页签：在售商品 / 收到的评价 ===== -->
    <div class="card section">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="在售商品" name="products">
          <div v-loading="loading">
            <el-empty v-if="!loading && products.length === 0" description="TA 还没有在售的商品" />
            <div v-else class="grid">
              <ProductCard v-for="p in products" :key="p.id" :product="p" show-favorite />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="收到的评价" name="reviews">
          <div v-loading="loading">
            <el-empty v-if="!loading && reviews.length === 0" description="暂无评价" />
            <div v-for="r in reviews" :key="r.id" class="review-row">
              <div class="r-main">
                <div class="r-head">
                  <el-avatar :size="28" :src="r.reviewerAvatar">{{ r.reviewerNickname?.[0] }}</el-avatar>
                  <span class="r-name">{{ r.reviewerNickname || '匿名' }}</span>
                  <el-rate :model-value="r.score" disabled />
                  <span class="text-muted">{{ r.createTime }}</span>
                </div>
                <p class="r-content">{{ r.content || '（未填写内容）' }}</p>
              </div>
              <router-link :to="`/product/${r.productId}`" class="r-product">
                <el-image v-if="r.productCover" :src="r.productCover" fit="cover" class="r-cover" />
                <span class="r-ptitle">{{ r.productTitle || '查看商品' }}</span>
              </router-link>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<style scoped>
.user-card {
  display: flex;
  align-items: center;
  gap: 16px;
}
.uname {
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}
.section {
  margin-top: 16px;
}
/* 商品宫格：与首页一致的 5 列布局 */
.grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
  min-height: 200px;
}
.review-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.r-main {
  flex: 1;
  min-width: 0;
}
.r-head {
  display: flex;
  align-items: center;
  gap: 12px;
}
.r-name {
  font-weight: 600;
}
.r-content {
  color: #606266;
  margin: 6px 0 0;
}
/* 评价右侧的商品入口：封面 + 标题，点击进详情 */
.r-product {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: #606266;
}
.r-cover {
  width: 48px;
  height: 48px;
  border-radius: 6px;
}
.r-ptitle {
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
/* 窄屏降为 2 列 */
@media (max-width: 960px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>