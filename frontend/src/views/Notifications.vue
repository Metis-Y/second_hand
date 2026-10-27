<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getNotifications, markAllNotificationsRead, markNotificationRead } from '@/api'

const list = ref([])        // 通知列表
const total = ref(0)        // 总条数（分页器用）
const page = ref(1)         // 当前页码
const size = ref(10)        // 每页条数
const loading = ref(false)  // 加载遮罩

// 有没有未读：决定「全部已读」按钮是否可点
const hasUnread = computed(() => list.value.some((n) => n.isRead === 0))

/**
 * 后端返回的是 ISO 时间（2026-10-27T18:30:00），
 * 这里把 T 换成空格并截到分钟，显示成 2026-10-27 18:30
 */
function formatTime(t) {
  return t ? String(t).replace('T', ' ').slice(0, 16) : ''
}

/** 拉取当前页通知 */
async function load() {
  loading.value = true
  try {
    const res = await getNotifications({ page: page.value, size: size.value })
    list.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

/**
 * 点击一条通知：未读的顺手标记已读
 * @param {object} item 通知对象
 */
async function onItemClick(item) {
  if (item.isRead === 1) return
  await markNotificationRead(item.id)
  item.isRead = 1              // 本地直接改，不用重新请求整个列表
}

/** 全部标记已读：成功后把当前页的都置为已读 */
async function readAll() {
  if (!hasUnread.value) return
  await markAllNotificationsRead()
  list.value.forEach((n) => (n.isRead = 1))
  ElMessage.success('已全部标记为已读')
}

/** 翻页 */
function onPage(p) {
  page.value = p
  load()
}

onMounted(load)
</script>

<template>
  <div class="page-container">
    <div class="head">
      <h2>我的消息</h2>
      <el-button :disabled="!hasUnread" @click="readAll">全部已读</el-button>
    </div>

    <el-empty v-if="!loading && list.length === 0" description="还没有收到消息" />

    <div v-else v-loading="loading" class="card list">
      <div
        v-for="item in list"
        :key="item.id"
        class="item"
        :class="{ unread: item.isRead === 0 }"
        @click="onItemClick(item)"
      >
        <!-- 未读红点：已读时用 visibility 隐藏，保持左右对齐不变 -->
        <span class="dot" :class="{ hide: item.isRead === 1 }"></span>
        <div class="content">{{ item.content }}</div>
        <div class="text-muted time">{{ formatTime(item.createTime) }}</div>
      </div>
    </div>

    <!-- 总数超过一页才显示分页器 -->
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
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.head h2 {
  margin: 0;
  font-size: 20px;
}
/* 每条通知一行：红点 + 内容 + 时间 */
.list {
  padding: 0;
  overflow: hidden;
}
.item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  border-bottom: 1px solid #f0f2f5;
  cursor: pointer;
  transition: background 0.15s;
}
.item:last-child {
  border-bottom: none;
}
.item:hover {
  background: #f7f9fc;
}
/* 未读：底色略微加深，内容加粗 */
.item.unread {
  background: #f0f7ff;
}
.item.unread .content {
  font-weight: 600;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f56c6c;
  flex-shrink: 0;
}
.dot.hide {
  visibility: hidden;
}
.content {
  flex: 1;
  font-size: 14px;
  color: #303133;
  line-height: 22px;
}
.time {
  flex-shrink: 0;
}
.pager {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>