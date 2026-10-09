<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getFavorites, removeFavorite } from '@/api'
import ProductCard from '@/components/ProductCard.vue'

const list = ref([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await getFavorites()
    list.value = res.list
  } finally {
    loading.value = false
  }
}

async function cancel(item) {
  try {
    await removeFavorite(item.id)
    ElMessage.success('已取消收藏')
    load()
  } catch (e) {
    ElMessage.error(e.message || '操作失败')
  }
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <h2>我的收藏</h2>
    <el-empty v-if="!loading && list.length === 0" description="还没有收藏任何商品" />
    <div v-else v-loading="loading" class="grid">
      <div v-for="item in list" :key="item.id" class="fav-item">
        <span class="cancel" @click="cancel(item)">取消收藏</span>
        <ProductCard :product="item" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  min-height: 200px;
}
.fav-item {
  position: relative;
}
.cancel {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 5;
  background: rgba(0, 0, 0, 0.45);
  color: #fff;
  font-size: 12px;
  padding: 3px 8px;
  border-radius: 10px;
  cursor: pointer;
}
.cancel:hover {
  background: #f56c6c;
}
@media (max-width: 960px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>