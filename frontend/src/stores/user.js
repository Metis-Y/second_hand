import { defineStore } from 'pinia'
import { login as apiLogin, register as apiRegister } from '@/api'

export const useUserStore = defineStore('user', {
  // state：响应式数据，初始化时从 localStorage 恢复上次的登录态
  state: () => ({
    token: localStorage.getItem('token') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null')
  }),
  // getters：由 state 派生出来的计算属性
  getters: {
    /** 是否已登录（有 token 即视为已登录） */
    isLogin: (state) => !!state.token,
    /** 是否是管理员（后端约定 role === 1 为管理员，0 为普通用户） */
    isAdmin: (state) => state.user?.role === 1
  },
  actions: {
    /**
     * 登录：调用接口 → 把返回的 token 和 user 写入状态与 localStorage
     * @param {{username: string, password: string}} payload 登录表单
     * @returns {Promise<{token: string, user: object}>} 后端返回的登录数据
     */
    async login(payload) {
      const data = await apiLogin(payload)
      const { token, ...user } = data
      this.token = token
      this.user = user
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify(user))
      return data
    },
    /**
     * 注册：调用接口 → 注册成功即自动登录（写入 token 和 user，与登录一致）
     * @param {{username: string, password: string, nickname?: string, phone?: string}} payload
     * @returns {Promise<{token: string, user: object}>} 后端返回的数据
     */
    async register(payload) {
      const data = await apiRegister(payload)
      const { token, ...user } = data
      this.token = token
      this.user = user
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify(user))
      return data
    },
    /** 退出登录：清空内存状态与 localStorage，使页面回到未登录形态 */
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('user')
    }
  }
})