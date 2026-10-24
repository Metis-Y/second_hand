<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const formRef = ref()      // el-form 实例，用于程序化触发校验
const loading = ref(false) // 提交中，按钮显示 loading 并防重复点击
// 预填演示账号，方便直接体验；reactive 让输入框双向绑定生效
const form = reactive({ username: 'xiaoming', password: '123456' })

// 表单校验规则：两项都必填，失焦（blur）时校验
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

/**
 * 提交流程：校验表单 → 调用 store.login → 提示成功并跳转
 * 校验不通过时 validate() 会抛错，流程自然中断，不会发请求。
 */
async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login({ ...form })
    ElMessage.success('登录成功')
    // redirect 由路由守卫写入（如从 /orders 被拦截过来的），登录后跳回原页面
    router.push(route.query.redirect || '/home')
  } catch (e) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card card">
      <h2 class="title">登录</h2>
      <p class="hint">演示账号：xiaoming / 123456 或 admin / 123456</p>
      <!-- @submit.prevent 拦截浏览器默认表单提交，统一走 submit() -->
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="submit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <!-- 密码框回车即可提交，少点一次按钮 -->
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" @keyup.enter="submit" />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" @click="submit">登录</el-button>
        <div class="foot">
          还没有账号？
          <router-link to="/register" class="link">去注册</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
/* 整屏容器：flex 居中，卡片永远在视口正中 */
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f6f8;
  padding: 16px;
}
/* 登录卡片固定宽度 */
.auth-card {
  width: 380px;
}
.title {
  text-align: center;
  margin: 0 0 4px;
}
/* 演示账号提示文字 */
.hint {
  text-align: center;
  color: #909399;
  font-size: 13px;
  margin: 0 0 20px;
}
.submit {
  width: 100%;
}
.foot {
  text-align: center;
  margin-top: 14px;
  font-size: 14px;
  color: #606266;
}
.link {
  color: var(--sh-primary);
}
</style>