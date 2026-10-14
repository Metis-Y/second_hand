<!--
  Home.vue —— 首页 / 商品列表页         路由：/home?keyword=xxx
  =====================================================================
  页面布局（自上而下，整体限宽 1200px 居中）：
  ┌─────────────────────────────────────────────────────────┐
  │ category-bar 分类导航栏（带图）                           │
  │   「全部」+ 各分类（图标 + 名称），单选高亮               │
  ├─────────────────────────────────────────────────────────┤
  │ grid 商品宫格（宽屏 4 列，窄屏 ≤960px 降为 2 列）           │
  │   └ ProductCard × N（数据来自分页接口）                    │
  │   无数据时显示 el-empty「暂无商品」                         │
  ├─────────────────────────────────────────────────────────┤
  │ pager 分页器（仅当总数超过一页时显示）                       │
  └─────────────────────────────────────────────────────────┘
  数据流：顶栏搜索框写入 URL query.keyword → 本页 watch 到变化后重新加载。
-->
<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getCategories, getProducts } from '@/api'
import ProductCard from '@/components/ProductCard.vue'

const route = useRoute()

// ---- 页面状态 ----
const categories = ref([])        // 分类列表（筛选栏用）
const activeCategory = ref(null)  // 当前选中分类 id，null 表示「全部」
const keyword = ref('')           // 当前搜索关键字
const list = ref([])              // 当前页商品
const total = ref(0)              // 商品总数（分页器用）
const page = ref(1)               // 当前页码
const size = 12                   // 每页条数（常量，不需要响应式）
const loading = ref(false)        // 加载中状态，驱动 v-loading 遮罩

/**
 * 拉取当前条件下的商品列表。
 * 三个查询条件：页码、关键字、分类；结果覆盖 list 与 total。
 * 用 try/finally 保证请求失败时也会关掉 loading 遮罩。
 */
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

/**
 * 切换分类：点击后回到第 1 页再加载
 * @param {number|null} id 分类 id，null 代表「全部」
 */
function onCategory(id) {
  activeCategory.value = id
  page.value = 1
  load()
}

/**
 * 分页器翻页回调
 * @param {number} p 目标页码
 */
function onPage(p) {
  page.value = p
  load()
}

// 首次进入：并行准备分类列表，并从 URL 读取关键字后加载商品
onMounted(async () => {
  try {
    categories.value = await getCategories()
  } catch { /* ignore */ }
  keyword.value = route.query.keyword || ''
  load()
})

// 顶部搜索框修改 query 时刷新（从别的页面搜索跳过来也会触发）
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
    <!-- ===== 分类导航栏：全部 + 各分类（图标+名称），点击即筛选 ===== -->
    <div class="category-bar card">
      <div class="cat" :class="{ active: activeCategory === null }" @click="onCategory(null)">
        <div class="cat-img cat-img-all">全</div>
        <span class="cat-name">全部</span>
      </div>
      <div
        v-for="c in categories"
        :key="c.id"
        class="cat"
        :class="{ active: activeCategory === c.id }"
        @click="onCategory(c.id)"
      >
        <img class="cat-img" :src="c.image" :alt="c.name" />
        <span class="cat-name">{{ c.name }}</span>
      </div>
    </div>

    <!-- ===== 商品宫格：无数据展示空状态，否则展示卡片网格 ===== -->
    <el-empty v-if="!loading && list.length === 0" description="暂无商品" />
    <div v-else v-loading="loading" class="grid">
      <ProductCard v-for="p in list" :key="p.id" :product="p" />
    </div>

    <!-- ===== 分页器：总数超过一页才渲染 ===== -->
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
/* 分类导航：横向排列，放不下自动换行 */
.category-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 16px;
  padding: 12px;
}
/* 单个分类：图标在上、名称在下 */
.cat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  width: 72px;
  padding: 8px 4px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.15s;
}
.cat-img {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
  background: #f2f3f5;
}
/* 「全部」没有图片，用圆形占位 + 文字 */
.cat-img-all {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  color: #909399;
}
.cat-name {
  font-size: 13px;
  color: #606266;
  line-height: 1;
}
.cat:hover {
  background: #f5f7fa;
}
/* 选中态：底色高亮 + 名称主题色 + 图标描边 */
.cat.active {
  background: #e8f1fd;
}
.cat.active .cat-name {
  color: var(--sh-primary);
  font-weight: 600;
}
.cat.active .cat-img {
  box-shadow: 0 0 0 2px var(--sh-primary);
}
/* 商品网格：宽屏 4 列；min-height 防止加载时页面高度塌陷 */
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
/* 窄屏（平板/手机）降为 2 列 */
@media (max-width: 960px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>