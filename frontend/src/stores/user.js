/**
 * =====================================================================
 * 用户状态仓库（Pinia）
 * ---------------------------------------------------------------------
 * 全局唯一地保存"当前登录用户"信息，页面通过 useUserStore() 读取。
 * 登录态同时写入 localStorage，刷新页面后依然保持登录（免重新登录）。
 * localStorage 中保存两项：token（令牌）和 user（用户信息 JSON）。
 * =====================================================================
 */
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
     * 注册：只转发给接口，注册成功后不自动登录（由页面引导去登录页）
     * @param {{username: string, password: string, nickname?: string, phone?: string}} payload
     */
    register(payload) {
      return apiRegister(payload)
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