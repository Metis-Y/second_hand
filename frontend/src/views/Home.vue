<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getCategories, getProducts } from '@/api'
import ProductCard from '@/components/ProductCard.vue'

const route = useRoute()

const categories = ref([])
const activeCategory = ref(null)
const keyword = ref('')
const list = ref([])
const total = ref(0)
const page = ref(1)
const size = 12
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const res = await getProducts({
      page: page.value,
      size,
      keyword: keyword.value,
      categoryId: activeCategory.value
    })
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function onCategory(id) {
  activeCategory.value = id
  page.value = 1
  load()
}

function onPage(p) {
  page.value = p
  load()
}

onMounted(async () => {
  try {
    categories.value = await getCategories()
  } catch { /* ignore */ }
  keyword.value = route.query.keyword || ''
  load()
})

// 顶部搜索框修改 query 时刷新
watch(
  () => route.query.keyword,
  (v) => {
    keyword.value = v || ''
    page.value = 1
    load()
  }
)
</script>

<template>
  <div class="page-container">
    <div class="category-bar card">
      <span class="cat" :class="{ active: activeCategory === null }" @click="onCategory(null)">全部</span>
      <span
        v-for="c in categories"
        :key="c.id"
        class="cat"
        :class="{ active: activeCategory === c.id }"
        @click="onCategory(c.id)"
      >{{ c.name }}</span>
    </div>

    <el-empty v-if="!loading && list.length === 0" description="暂无商品" />
    <div v-else v-loading="loading" class="grid">
      <ProductCard v-for="p in list" :key="p.id" :product="p" />
    </div>

    <div v-if="total > size" class="pager">
      <el-pagination
        background
        layout="prev, pager, next"
        :total="total"
        :page-size="size"
        :current-page="page"
        @current-change="onPage"
      />
    </div>
  </div>
</template>

<style scoped>
.category-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}
.cat {
  padding: 6px 16px;
  border-radius: 18px;
  background: #f2f3f5;
  color: #606266;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.15s;
}
.cat:hover {
  background: #e8f1fd;
  color: var(--sh-primary);
}
.cat.active {
  background: var(--sh-primary);
  color: #fff;
}
.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  min-height: 200px;
}
.pager {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
@media (max-width: 960px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>