<!--
  Profile.vue —— 个人中心           路由：/profile（需登录）
  =====================================================================
  布局（限宽 1200px）：
  ┌─────────────────────────────────────────────────────────────┐
  │ user-card 用户信息卡                                          │
  │   [头像]  昵称 + 角色标签（管理员/普通用户）                   │
  │           @用户名                                             │
  ├─────────────────────────────────────────────────────────────┤
  │ section 卡片，内含两个页签                                     │
  │   ┌ 我发布的 ────────────────────────────────────────────┐   │
  │   │ 每行：封面缩略图 | 标题 / 价格 / 状态标签 / 时间       │   │
  │   │       | [查看] [编辑] [删除]                          │   │
  │   │ 无数据时 el-empty「还没发布过商品」                    │   │
  │   └──────────────────────────────────────────────────────┘   │
  │   ┌ 收到的评价 ──────────────────────────────────────────┐   │
  │   │ 每行：评价人 + 星级 + 时间，下方评价内容               │   │
  │   │ 无数据时 el-empty「暂无评价」                          │   │
  │   └──────────────────────────────────────────────────────┘   │
  └─────────────────────────────────────────────────────────────┘
  说明：两个页签的数据按需加载（切到哪个页签才请求哪个接口），
        删除商品前会弹确认框，取消则不执行删除。
-->
<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getMyProducts, deleteProduct, getUserReviews } from '@/api'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('products')  // 当前页签：products 我发布的 / reviews 收到的评价
const products = ref([])           // 我发布的商品
const reviews = ref([])            // 我收到的评价
const loading = ref(false)

// 商品状态码 → 文案 / 标签配色
const statusMap = { 0: '已下架', 1: '在售', 2: '已售出', 3: '审核中' }
const statusType = { 0: 'info', 1: 'success', 2: 'danger', 3: 'warning' }

/**
 * 按当前页签加载对应数据（只请求当前页签需要的接口，避免多余请求）
 * 收到的评价需要当前用户 id 作为查询条件。
 */
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

/** 页签切换时重新加载数据 */
function onTabChange() {
  load()
}

/**
 * 删除商品：先弹确认框（用户点取消会 reject，直接 return 不删除），
 * 确认后再调删除接口并刷新列表。
 * @param {number} id 商品 id
 */
async function remove(id) {
  try {
    await ElMessageBox.confirm('确定删除该商品吗？', '提示', { type: 'warning' })
  } catch {
    return // 用户取消，静默返回
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
    <!-- ===== 用户信息卡：头像 + 昵称 + 角色 + 用户名 ===== -->
    <div class="card user-card">
      <el-avatar :size="64" :src="userStore.user?.avatar">{{ userStore.user?.nickname?.[0] }}</el-avatar>
      <div class="uinfo">
        <div class="uname">
          {{ userStore.user?.nickname }}
          <!-- 角色标签：管理员红色、普通用户灰色 -->
          <el-tag :type="userStore.isAdmin ? 'danger' : 'info'" size="small" effect="light">
            {{ userStore.isAdmin ? '管理员' : '普通用户' }}
          </el-tag>
        </div>
        <div class="text-muted">@{{ userStore.user?.username }}</div>
      </div>
    </div>

    <!-- ===== 两个页签：我发布的 / 收到的评价 ===== -->
    <div class="card section">
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <!-- 页签一：我发布的商品，每行可查看/编辑/删除 -->
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
              <!-- 编辑按钮跳到 /edit/:id，复用 Publish.vue 的编辑模式 -->
              <div class="p-actions">
                <el-button size="small" @click="router.push(`/product/${p.id}`)">查看</el-button>
                <el-button size="small" type="primary" @click="router.push(`/edit/${p.id}`)">编辑</el-button>
                <el-button size="small" type="danger" @click="remove(p.id)">删除</el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 页签二：别人对我的评价 -->
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
/* 用户信息卡横向排列：头像在左，信息在右 */
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
/* 商品行：左缩略图 / 中间信息（flex:1 撑开）/ 右按钮组 */
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
  min-width: 0; /* 配合下面的 ellipsis，防止长标题把按钮挤出去 */
}
/* 标题超长时单行省略 */
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
/* 按钮组不参与收缩，保证始终完整可见 */
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