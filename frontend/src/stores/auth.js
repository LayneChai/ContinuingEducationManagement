import { defineStore } from 'pinia'

import { getProfileApi, loginApi, logoutApi } from '../api/auth'
import { resetDynamicRoutes } from '../router'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('ce_token') || '',
    user: null,
    profileLoaded: false,
  }),
  getters: {
    userName: (state) => state.user?.realName || state.user?.username || '未登录',
    roles: (state) => state.user?.roles || [],
  },
  actions: {
    async login(form) {
      const data = await loginApi(form)
      this.token = data.token
      this.user = data
      this.profileLoaded = false
      localStorage.setItem('ce_token', data.token)
    },
    async fetchProfile() {
      this.user = await getProfileApi()
      this.profileLoaded = true
    },
    async logout() {
      try {
        if (this.token) {
          await logoutApi()
        }
      } catch {
      }
      this.token = ''
      this.user = null
      this.profileLoaded = false
      localStorage.removeItem('ce_token')
      resetDynamicRoutes()
    },
  },
})
