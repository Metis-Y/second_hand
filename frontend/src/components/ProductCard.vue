<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const props = defineProps({
  product: { type: Object, required: true }
})
const router = useRouter()

const statusMap = { 0: '已下架', 1: '在售', 2: '已售出', 3: '审核中' }
const statusType = { 0: 'info', 1: 'success', 2: 'danger', 3: 'warning' }

const statusText = computed(() => statusMap[props.product.status] ?? '')
const statusTagType = computed(() => statusType[props.product.status] ?? 'info')

function goDetail() {
  router.push(`/product/${props.product.id}`)
}
</script>

<template>
  <div class="product-card card" @click="goDetail">
    <el-image class="cover" :src="product.cover" fit="cover">
      <template #error>
        <div class="img-fallback">{{ product.title?.[0] || '图' }}</div>
      </template>
    </el-image>
    <div class="body">
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
.product-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}
.cover {
  width: 100%;
  height: 160px;
  display: block;
}
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