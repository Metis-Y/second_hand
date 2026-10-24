<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getMyProducts, deleteProduct, getUserReviews, getUser, updateProfile, uploadImage } from '@/api'

const router = useRouter()
const userStore = useUserStore()

const activeTab = ref('products')  // 当前页签：products 我发布的 / reviews 收到的评价
const products = ref([])           // 我发布的商品
const reviews = ref([])            // 我收到的评价
const loading = ref(false)

const profile = ref(null)          // 当前用户完整资料（含手机号/性别，来自 GET /users/{id}）
const dialogVisible = ref(false)    // 编辑资料弹窗是否打开
const saving = ref(false)          // 保存中状态
const fileInput = ref()            // 隐藏的头像文件选择框
// 编辑表单：昵称/头像/手机号/性别（性别 0 未知、1 男、2 女，与注册页一致）
const form = reactive({ nickname: '', avatar: '', phone: '', gender: 0 })

// 性别码 → 文案
const genderMap = { 1: '男', 2: '女' }

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

/** 加载当前用户完整资料（含手机号/性别），用于资料卡展示与编辑回填 */
async function loadProfile() {
  try {
    profile.value = await getUser(userStore.user.id)
  } catch (e) {
    ElMessage.error(e.message || '加载资料失败')
  }
}

/** 打开编辑弹窗：用当前资料回填表单 */
function openEdit() {
  const p = profile.value || userStore.user
  form.nickname = p?.nickname || ''
  form.avatar = p?.avatar || ''
  form.phone = p?.phone || ''
  form.gender = p?.gender || 0
  dialogVisible.value = true
}

/** 触发头像文件选择框 */
function pickAvatar() {
  fileInput.value.click()
}

/** 选择头像文件后上传（单张，覆盖 form.avatar） */
async function onAvatarFile(e) {
  const file = e.target.files?.[0]
  e.target.value = ''
  if (!file) return
  try {
    const res = await uploadImage(file)
    form.avatar = res.url
  } catch {
    ElMessage.error('头像上传失败')
  }
}

/** 保存资料：调 PUT /users/me，成功后刷新资料卡并关闭弹窗 */
async function submitEdit() {
  saving.value = true
  try {
    await updateProfile({
      nickname: form.nickname,
      avatar: form.avatar,
      phone: form.phone,
      gender: form.gender || null
    })
    ElMessage.success('资料已更新')
    // 同步内存里的当前用户，让顶栏头像/昵称即时刷新
    if (userStore.user) {
      userStore.user.nickname = form.nickname
      userStore.user.avatar = form.avatar
      localStorage.setItem('user', JSON.stringify({ ...userStore.user, nickname: form.nickname, avatar: form.avatar }))
    }
    dialogVisible.value = false
    await loadProfile()
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  load()
  loadProfile()
})
</script>

<template>
  <div class="page-container">
    <!-- ===== 用户信息卡：头像 + 昵称 + 角色 + 性别 + 手机号，右上角编辑按钮 ===== -->
    <div class="card user-card">
      <el-avatar :size="64" :src="profile?.avatar || userStore.user?.avatar">{{ (profile?.nickname || userStore.user?.nickname)?.[0] }}</el-avatar>
      <div class="uinfo">
        <div class="uname">
          {{ profile?.nickname || userStore.user?.nickname }}
          <!-- 角色标签：管理员红色、普通用户灰色 -->
          <el-tag :type="userStore.isAdmin ? 'danger' : 'info'" size="small" effect="light">
            {{ userStore.isAdmin ? '管理员' : '普通用户' }}
          </el-tag>
          <!-- 性别标签：只有设置了性别才显示 -->
          <el-tag v-if="genderMap[profile?.gender]" size="small" effect="plain">{{ genderMap[profile?.gender] }}</el-tag>
        </div>
        <div class="text-muted">@{{ profile?.username || userStore.user?.username }}</div>
        <div v-if="profile?.phone" class="text-muted">手机号：{{ profile.phone }}</div>
      </div>
      <el-button class="edit-btn" @click="openEdit">编辑资料</el-button>
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

    <!-- ===== 编辑资料弹窗 ===== -->
    <el-dialog v-model="dialogVisible" title="编辑资料" width="420px">
      <el-form :model="form" label-width="72px">
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" maxlength="16" placeholder="昵称" />
        </el-form-item>
        <el-form-item label="头像">
          <div class="avatar-row">
            <el-avatar :size="56" :src="form.avatar">{{ form.nickname?.[0] }}</el-avatar>
            <el-button @click="pickAvatar">上传头像</el-button>
          </div>
          <input ref="fileInput" type="file" accept="image/*" hidden @change="onAvatarFile" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" maxlength="11" placeholder="手机号" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :value="0">未知</el-radio>
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
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
/* 编辑按钮靠右，与信息区分 */
.edit-btn {
  margin-left: auto;
  align-self: flex-start;
}
/* 头像编辑行：预览 + 上传按钮并排 */
.avatar-row {
  display: flex;
  align-items: center;
  gap: 12px;
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
</style>