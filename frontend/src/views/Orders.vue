<!--
  Orders.vue —— 我的订单页           路由：/orders（需登录）
  =====================================================================
  布局（限宽 1200px）：
  ┌─────────────────────────────────────────────────────────────┐
  │  标题「我的订单」                                             │
  │  tabs 身份切换：[ 我买到的 | 我卖出的 ]                        │
  │  radio-group 状态筛选：[全部][待确认][交易中][已完成][已取消]   │
  ├─────────────────────────────────────────────────────────────┤
  │  card 订单表格 el-table                                      │
  │   商品（可点进详情）| 价格 | 卖家/买家 | 状态标签 | 下单时间   │
  │   | 操作按钮（按身份 + 订单状态动态显示）                      │
  ├─────────────────────────────────────────────────────────────┤
  │  dialog 评价弹窗（星级 + 文本内容）                            │
  └─────────────────────────────────────────────────────────────┘

  订单状态流转（与后端约定）：
    0 待确认 → 买家可取消 / 卖家可确认交易
    1 交易中 → 买卖双方都可确认完成
    2 已完成 → 双方可评价（评价过显示"已评价"）
    3 已取消 → 终态

  操作按钮完全由 role（我是买家还是卖家）和 row.status 决定：
    · 买家：status=0 取消订单；status=1 确认收货；status=2 评价
    · 卖家：status=0 确认交易 / 取消；status=1 完成交易；status=2 评价
-->
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getOrders, updateOrderStatus, createReview } from '@/api'

const role = ref('buyer')        // 视角：buyer 我买到的 / seller 我卖出的
const statusFilter = ref('')     // 状态筛选，'' 表示全部
const list = ref([])             // 订单列表
const loading = ref(false)       // 表格加载遮罩

// ---- 评价弹窗相关状态 ----
const reviewDialog = ref(false)  // 弹窗显隐
const reviewSaving = ref(false)  // 提交中
const reviewForm = reactive({ orderId: null, score: 5, content: '' })
// 本次会话内已评价过的订单 id，用于把"评价"按钮换成"已评价"文字
const reviewedOrders = ref(new Set())

// 订单状态码 → 文案 / 标签配色
const statusMap = { 0: '待确认', 1: '交易中', 2: '已完成', 3: '已取消' }
const statusType = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }

/** 按当前身份与状态筛选条件拉取订单列表 */
async function load() {
  loading.value = true
  try {
    const res = await getOrders({ role: role.value, status: statusFilter.value })
    list.value = res.list
  } finally {
    loading.value = false
  }
}

/**
 * el-tabs 切换身份（买家/卖家）
 * 切换时重置状态筛选，避免上一个视角的筛选条件残留
 * @param {'buyer'|'seller'} r 目标身份
 */
function switchRole(r) {
  role.value = r
  statusFilter.value = ''
  load()
}

/** 状态筛选变化后重新查询 */
function onStatusFilter() {
  load()
}

/**
 * 修改订单状态（取消 / 确认交易 / 确认收货 / 完成交易共用）
 * @param {object} order 当前行订单
 * @param {number} status 目标状态码
 */
async function changeStatus(order, status) {
  try {
    await updateOrderStatus(order.id, status)
    ElMessage.success('操作成功')
    load()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

/**
 * 打开评价弹窗，并把评分重置为默认 5 星、清空上次内容
 * @param {object} order 待评价的订单
 */
function openReview(order) {
  reviewForm.orderId = order.id
  reviewForm.score = 5
  reviewForm.content = ''
  reviewDialog.value = true
}

/** 提交评价：成功后记入 reviewedOrders 并刷新列表 */
async function submitReview() {
  reviewSaving.value = true
  try {
    await createReview({ ...reviewForm })
    reviewedOrders.value.add(reviewForm.orderId)
    reviewDialog.value = false
    ElMessage.success('评价成功')
    load()
  } catch (e) {
    ElMessage.error(e.message || '评价失败')
  } finally {
    reviewSaving.value = false
  }
}

/**
 * 判断某订单是否已评价过（驱动按钮/文字切换）
 * @param {number} orderId
 * @returns {boolean}
 */
function isReviewed(orderId) {
  return reviewedOrders.value.has(orderId)
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <h2>我的订单</h2>

    <!-- ===== 身份切换：我买到的 / 我卖出的 ===== -->
    <el-tabs v-model="role" @tab-change="switchRole">
      <el-tab-pane label="我买到的" name="buyer" />
      <el-tab-pane label="我卖出的" name="seller" />
    </el-tabs>

    <!-- ===== 状态筛选：所有筛选走同一个接口参数 status ===== -->
    <el-radio-group v-model="statusFilter" class="status-filter" @change="onStatusFilter">
      <el-radio-button value="">全部</el-radio-button>
      <el-radio-button value="0">待确认</el-radio-button>
      <el-radio-button value="1">交易中</el-radio-button>
      <el-radio-button value="2">已完成</el-radio-button>
      <el-radio-button value="3">已取消</el-radio-button>
    </el-radio-group>

    <!-- ===== 订单表格 ===== -->
    <div class="card table-card" v-loading="loading">
      <el-table :data="list">
        <!-- 商品名：可点击跳详情，商品被删除时显示占位文案 -->
        <el-table-column label="商品" min-width="160">
          <template #default="{ row }">
            <router-link class="product-link" :to="`/product/${row.productId}`">
              {{ row.product?.title || '商品已删除' }}
            </router-link>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="100">
          <template #default="{ row }"><span class="price">￥{{ row.price }}</span></template>
        </el-table-column>
        <!-- 表头与取值都随身份切换：买家看卖家，卖家看买家 -->
        <el-table-column :label="role === 'buyer' ? '卖家' : '买家'" width="100">
          <template #default="{ row }">{{ role === 'buyer' ? row.sellerNickname : row.buyerNickname }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType[row.status]">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" width="170">
          <template #default="{ row }">{{ row.createTime }}</template>
        </el-table-column>

        <!-- 操作列：按身份 + 状态显示不同按钮 -->
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <!-- 买家视角 -->
            <template v-if="role === 'buyer'">
              <el-button v-if="row.status === 0" size="small" type="info" @click="changeStatus(row, 3)">取消订单</el-button>
              <el-button v-if="row.status === 1" size="small" type="success" @click="changeStatus(row, 2)">确认收货</el-button>
              <el-button v-if="row.status === 2 && !isReviewed(row.id)" size="small" type="primary" @click="openReview(row)">评价</el-button>
              <span v-if="row.status === 2 && isReviewed(row.id)" class="text-muted">已评价</span>
            </template>
            <!-- 卖家视角 -->
            <template v-else>
              <el-button v-if="row.status === 0" size="small" type="primary" @click="changeStatus(row, 1)">确认交易</el-button>
              <el-button v-if="row.status === 0" size="small" type="info" @click="changeStatus(row, 3)">取消</el-button>
              <el-button v-if="row.status === 1" size="small" type="success" @click="changeStatus(row, 2)">完成交易</el-button>
              <el-button v-if="row.status === 2 && !isReviewed(row.id)" size="small" type="primary" @click="openReview(row)">评价</el-button>
              <span v-if="row.status === 2 && isReviewed(row.id)" class="text-muted">已评价</span>
            </template>
          </template>
        </el-table-column>

        <!-- 表格空状态 -->
        <template #empty>
          <el-empty description="暂无订单" :image-size="80" />
        </template>
      </el-table>
    </div>

    <!-- ===== 评价弹窗：星级 + 文本，内容选填（最多 500 字） ===== -->
    <el-dialog v-model="reviewDialog" title="评价订单" width="420px">
      <div class="review-form">
        <div class="row">
          <span>评分：</span>
          <el-rate v-model="reviewForm.score" />
        </div>
        <el-input v-model="reviewForm.content" type="textarea" :rows="4" maxlength="500" show-word-limit placeholder="说说这次交易的感受（选填）" />
      </div>
      <template #footer>
        <el-button @click="reviewDialog = false">取消</el-button>
        <el-button type="primary" :loading="reviewSaving" @click="submitReview">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.status-filter {
  margin: 0 0 16px;
}
/* 表格卡片留一点内边距，避免表格贴边 */
.table-card {
  padding: 8px;
}
.product-link {
  color: var(--sh-primary);
}
/* 评价弹窗里"评分："与星级横向对齐 */
.review-form .row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
</style>