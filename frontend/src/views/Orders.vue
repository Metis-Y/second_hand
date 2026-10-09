<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getOrders, updateOrderStatus, createReview } from '@/api'

const role = ref('buyer')
const statusFilter = ref('')
const list = ref([])
const loading = ref(false)

const reviewDialog = ref(false)
const reviewSaving = ref(false)
const reviewForm = reactive({ orderId: null, score: 5, content: '' })
const reviewedOrders = ref(new Set())

const statusMap = { 0: '待确认', 1: '交易中', 2: '已完成', 3: '已取消' }
const statusType = { 0: 'warning', 1: 'primary', 2: 'success', 3: 'info' }

async function load() {
  loading.value = true
  try {
    const res = await getOrders({ role: role.value, status: statusFilter.value })
    list.value = res.list
  } finally {
    loading.value = false
  }
}

function switchRole(r) {
  role.value = r
  statusFilter.value = ''
  load()
}

function onStatusFilter() {
  load()
}

async function changeStatus(order, status) {
  try {
    await updateOrderStatus(order.id, status)
    ElMessage.success('操作成功')
    load()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

function openReview(order) {
  reviewForm.orderId = order.id
  reviewForm.score = 5
  reviewForm.content = ''
  reviewDialog.value = true
}

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

function isReviewed(orderId) {
  return reviewedOrders.value.has(orderId)
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <h2>我的订单</h2>

    <el-tabs v-model="role" @tab-change="switchRole">
      <el-tab-pane label="我买到的" name="buyer" />
      <el-tab-pane label="我卖出的" name="seller" />
    </el-tabs>

    <el-radio-group v-model="statusFilter" class="status-filter" @change="onStatusFilter">
      <el-radio-button value="">全部</el-radio-button>
      <el-radio-button value="0">待确认</el-radio-button>
      <el-radio-button value="1">交易中</el-radio-button>
      <el-radio-button value="2">已完成</el-radio-button>
      <el-radio-button value="3">已取消</el-radio-button>
    </el-radio-group>

    <div class="card table-card" v-loading="loading">
      <el-table :data="list">
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
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <!-- 买家 -->
            <template v-if="role === 'buyer'">
              <el-button v-if="row.status === 0" size="small" type="info" @click="changeStatus(row, 3)">取消订单</el-button>
              <el-button v-if="row.status === 1" size="small" type="success" @click="changeStatus(row, 2)">确认收货</el-button>
              <el-button v-if="row.status === 2 && !isReviewed(row.id)" size="small" type="primary" @click="openReview(row)">评价</el-button>
              <span v-if="row.status === 2 && isReviewed(row.id)" class="text-muted">已评价</span>
            </template>
            <!-- 卖家 -->
            <template v-else>
              <el-button v-if="row.status === 0" size="small" type="primary" @click="changeStatus(row, 1)">确认交易</el-button>
              <el-button v-if="row.status === 0" size="small" type="info" @click="changeStatus(row, 3)">取消</el-button>
              <el-button v-if="row.status === 1" size="small" type="success" @click="changeStatus(row, 2)">完成交易</el-button>
              <el-button v-if="row.status === 2 && !isReviewed(row.id)" size="small" type="primary" @click="openReview(row)">评价</el-button>
              <span v-if="row.status === 2 && isReviewed(row.id)" class="text-muted">已评价</span>
            </template>
          </template>
        </el-table-column>
        <template #empty>
          <el-empty description="暂无订单" :image-size="80" />
        </template>
      </el-table>
    </div>

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
.table-card {
  padding: 8px;
}
.product-link {
  color: var(--sh-primary);
}
.review-form .row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
</style>