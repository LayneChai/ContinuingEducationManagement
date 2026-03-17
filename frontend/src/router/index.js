import { createRouter, createWebHistory } from 'vue-router'

import AppLayout from '../layouts/AppLayout.vue'
import { useAuthStore } from '../stores/auth'
import { usePortalStore } from '../stores/portal'

const viewModules = import.meta.glob('../views/**/*.vue')

const staticRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/login/LoginView.vue'),
    meta: { public: true },
  },
  {
    path: '/',
    name: 'RootLayout',
    component: AppLayout,
    redirect: '/portal/loading',
    children: [
      {
        path: 'portal/loading',
        name: 'PortalLoading',
        component: () => import('../views/common/PortalLoadingView.vue'),
        meta: { hidden: true },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/common/NotFoundView.vue'),
    meta: { public: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes: staticRoutes,
})

let routesLoaded = false

function resolveViewComponent(componentPath) {
  if (!componentPath || componentPath === 'Layout') {
    return AppLayout
  }
  const key = `../views/${componentPath}.vue`
  return viewModules[key] || viewModules['../views/common/FeatureComingView.vue']
}

function transformMenuToRoutes(menus) {
  return menus.map((group) => {
    const children = (group.children || []).map((item) => ({
      path: item.path.replace(`${group.path}/`, ''),
      name: item.name,
      component: resolveViewComponent(item.component),
      meta: {
        title: item.title,
        icon: item.icon,
        permission: item.permission,
      },
    }))

    return {
      path: group.path,
      name: group.name,
      component: AppLayout,
      redirect: children[0] ? `${group.path}/${children[0].path}` : '/portal/loading',
      children,
      meta: {
        title: group.title,
        icon: group.icon,
      },
    }
  })
}

async function ensureDynamicRoutes() {
  if (routesLoaded) {
    return
  }
  const portalStore = usePortalStore()
  if (!portalStore.menus.length) {
    await portalStore.loadMenusAndDashboard()
  }
  transformMenuToRoutes(portalStore.menus).forEach((route) => router.addRoute(route))
  routesLoaded = true
}

router.beforeEach(async (to) => {
  const authStore = useAuthStore()

  if (to.meta.public) {
    if (to.path === '/login' && authStore.token) {
      await ensureDynamicRoutes()
      return usePortalStore().defaultRoute || '/portal/loading'
    }
    return true
  }

  if (!authStore.token) {
    return '/login'
  }

  if (!authStore.profileLoaded) {
    await authStore.fetchProfile()
  }

  await ensureDynamicRoutes()

  if (to.path === '/' || to.path === '/portal/loading') {
    return usePortalStore().defaultRoute || '/portal/loading'
  }

  return true
})

export function resetDynamicRoutes() {
  routesLoaded = false
}

export default router
