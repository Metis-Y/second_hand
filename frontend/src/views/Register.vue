<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()      // el-form 实例，用于触发校验
const loading = ref(false) // 提交中状态
// confirm 只用于前端校验，不会提交给后端；gender 0=未知(提交时转 null)、1=男、2=女
const form = reactive({ username: '', password: '', confirm: '', nickname: '', phone: '', gender: 0 })

// 表单校验规则：长度、必填、两次密码一致性、手机号格式
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 16, message: '用户名 2-16 位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ],
  confirm: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      // 自定义校验：与 form.password 比较，一致则放行（cb()），否则抛错
      validator: (rule, value, cb) => (value === form.password ? cb() : cb(new Error('两次密码不一致'))),
      trigger: 'blur'
    }
  ],
  phone: [
    // 选填：不填时正则不匹配但非必填，填写后必须是 1 开头的 11 位数字
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

/**
 * 提交流程：校验 → 调用注册接口（只传后端需要的字段）→ 注册成功自动登录进首页
 * 确认密码 confirm 只用于前端校验，不参与提交；性别 0（未知）提交时转成 null。
 */
async function submit() {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.register({
      username: form.username,
      password: form.password,
      nickname: form.nickname,
      phone: form.phone,
      gender: form.gender || null
    })
    ElMessage.success('注册成功')
    router.push('/')
  } catch (e) {
    ElMessage.error(e.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card card">
      <h2 class="title">注册</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="submit">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="用户名 2-16 位" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="选填" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="选填" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="form.gender">
            <el-radio :value="0">未知</el-radio>
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirm">
          <el-input v-model="form.confirm" type="password" show-password placeholder="再次输入密码" />
        </el-form-item>
        <el-button type="primary" class="submit" :loading="loading" @click="submit">注册</el-button>
        <div class="foot">
          已有账号？
          <router-link to="/login" class="link">去登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
/* 与 Login.vue 完全一致的整屏居中样式，保持两个页面观感统一 */
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f6f8;
  padding: 16px;
}
.auth-card {
  width: 380px;
}
.title {
  text-align: center;
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