import axios from 'axios'
import { ElMessage } from 'element-plus'

// 统一 axios 实例：baseURL 默认 /api（开发环境由 vite 代理到后端 8080）
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 10000
})

/**
 * 请求拦截器：为每个请求自动附加登录令牌
 * 有 token 时加上请求头 Authorization: Bearer <token>
 */
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

/**
 * 响应拦截器：统一解包响应体、统一错误提示
 * 成功分支：把 { code, message, data } 拆开，只返回 data
 * 失败分支：按错误类型提示，并抛出异常交给页面处理
 */
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 非约定格式（如文件流、或后端直接返回数据）时原样返回
    if (res === null || typeof res !== 'object' || res.code === undefined) {
      return res
    }
    // 成功（code = 1）：只把 data 交给调用方
    if (res.code === 1) {
      return res.data
    }
    // 失败（code = 0）：直接展示后端 message
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  // 网络层错误（超时、断网、5xx 等）：error.response 可能为空，做兜底文案
  (error) => {
    // 未登录/token 失效：后端返回 HTTP 401 状态码
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      ElMessage.error('登录已过期，请重新登录')
      // 记住当前地址，登录成功后跳回；已处于登录/注册页则不带 redirect，避免原地循环
      const current = window.location.pathname + window.location.search
      let redirect = encodeURIComponent(current)
      if (current.startsWith('/login') || current.startsWith('/register')) {
        redirect = ''
      }
      // 延迟 600ms 让用户能看清提示，再整页跳转（刷新以重置所有内存状态）
      setTimeout(() => {
        if (redirect) {
          window.location.href = `/login?redirect=${redirect}`
        } else {
          window.location.href = '/login'
        }
      }, 600)
      return Promise.reject(new Error('未登录或登录已过期'))
    }
    const msg = error.response?.data?.message || error.message || '网络异常'
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default request