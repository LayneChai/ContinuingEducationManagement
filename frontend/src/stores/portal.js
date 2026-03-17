import { defineStore } from 'pinia'

import { getDashboardApi, getMenusApi } from '../api/portal'

export const usePortalStore = defineStore('portal', {
  state: () => ({
    menus: [],
    dashboard: null,
  }),
  getters: {
    defaultRoute: (state) => state.menus?.[0]?.children?.[0]?.path || '/portal/loading',
  },
  actions: {
    async loadMenusAndDashboard() {
      const [menus, dashboard] = await Promise.all([getMenusApi(), getDashboardApi()])
      this.menus = menus || []
      this.dashboard = dashboard || null
    },
    clear() {
      this.menus = []
      this.dashboard = null
    },
  },
})
