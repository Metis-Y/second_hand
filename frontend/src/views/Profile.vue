<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getMyProducts, deleteProduct, getUserReviews } from '@/api'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('products')
const products = ref([])
const reviews = ref([])
const loading = ref(false)

const statusMap = { 0: '已下架', 1: '在售', 2: '已售出', 3: '审核中' }
const statusType = { 0: 'info', 1: 'success', 2: 'danger', 3: 'warning' }

async function load() {
  loading.value = true
  try {
    if (activeTab.value === 'products') {
      products.value = (await getMyProducts()).list
    } else {
      reviews.value = (await getUserReviews(userStore.user.id)).list
    }
  } finally {
    loading.value = false
  }
}

function onTabChange() {
  load()
}

async function remove(id) {
  try {
    await ElMessageBox.confirm('确定删除该商品吗？', '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await deleteProduct(id)
    ElMessage.success('已删除')
    load()
  } catch (e) {
    ElMessage.error(e.message || '删除失败')
  }
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <div class="card user-card">
      <el-avatar :size="64" :src="userStore.user?.avatar">{{ userStore.user?.nickname?.[0] }}</el-avatar>
      <div class="uinfo">
        <div class="uname">
          {{ userStore.user?.nickname }}
          <el-tag :type="userStore.isAdmin ? 'danger' : 'info'" size="small" effect="light">
            {{ userStore.isAdmin ? '管理员' : '普通用户' }}
          </el-tag>
        </div>
        <div class="text-muted">@{{ userStore.user?.username }}</div>
      </div>
    </div>

    <div class="card section">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <el-tab-pane label="我发布的" name="products">
          <div v-loading="loading">
            <el-empty v-if="!loading && products.length === 0" description="还没发布过商品" />
            <div v-for="p in products" :key="p.id" class="prod-row">
              <el-image :src="p.cover" fit="cover" class="p-cover" />
              <div class="p-info">
                <div class="p-title">{{ p.title }}</div>
                <div class="p-meta">
                  <span class="price">￥{{ p.price }}</span>
                  <el-tag :type="statusType[p.status]" size="small" effect="light">{{ statusMap[p.status] }}</el-tag>
                </div>
                <div class="text-muted">{{ p.createTime }}</div>
              </div>
              <div class="p-actions">
                <el-button size="small" @click="router.push(`/product/${p.id}`)">查看</el-button>
                <el-button size="small" type="primary" @click="router.push(`/edit/${p.id}`)">编辑</el-button>
                <el-button size="small" type="danger" @click="remove(p.id)">删除</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="收到的评价" name="reviews">
          <div v-loading="loading">
            <el-empty v-if="!loading && reviews.length === 0" description="暂无评价" />
            <div v-for="r in reviews" :key="r.id" class="review-row">
              <div class="r-head">
                <span class="r-name">{{ r.reviewerNickname || '匿名' }}</span>
                <el-rate :model-value="r.score" disabled />
                <span class="text-muted">{{ r.createTime }}</span>
              </div>
              <p class="r-content">{{ r.content || '（未填写内容）' }}</p>
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
.prod-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}
.p-cover {
  width: 80px;
  height: 60px;
  border-radius: 6px;
  flex-shrink: 0;
}
.p-info {
  flex: 1;
  min-width: 0;
}
.p-title {
  font-size: 15px;
  color: #303133;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.p-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 4px;
}
.p-actions {
  flex-shrink: 0;
}
.review-row {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
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
</style>