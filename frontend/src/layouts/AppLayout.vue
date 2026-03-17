<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { usePortalStore } from '../stores/portal'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const portalStore = usePortalStore()

const activeMenu = computed(() => route.path)
const menuGroups = computed(() => portalStore.menus || [])
const dashboardTitle = computed(() => portalStore.dashboard?.title || '启智继续教育管理系统')

async function handleLogout() {
  await authStore.logout()
  portalStore.clear()
  router.replace('/login')
}

function onMenuSelect(path) {
  router.push(path)
}
</script>

<template>
  <div class="shell">
    <aside class="sidebar app-card">
      <div class="brand-block">
        <span class="brand-tag">QiZhi AI</span>
        <h1>启智继续教育</h1>
        <p>三端协同的继续教育与智能辅导平台</p>
      </div>

      <div class="menu-groups">
        <section v-for="group in menuGroups" :key="group.name" class="menu-group">
          <div class="group-title">
            <el-icon><component :is="group.icon || 'Grid'" /></el-icon>
            <span>{{ group.title }}</span>
          </div>
          <el-menu
            :default-active="activeMenu"
            class="side-menu"
            @select="onMenuSelect"
          >
            <el-menu-item
              v-for="item in group.children || []"
              :key="item.path"
              :index="item.path"
            >
              <el-icon><component :is="item.icon || 'Menu'" /></el-icon>
              <span>{{ item.title }}</span>
            </el-menu-item>
          </el-menu>
        </section>
      </div>
    </aside>

    <main class="main-panel">
      <header class="topbar app-card">
        <div>
          <div class="eyebrow">Continuing Education Platform</div>
          <div class="topbar-title">{{ dashboardTitle }}</div>
        </div>
        <div class="user-tools">
          <div class="user-chip">
            <strong>{{ authStore.userName }}</strong>
            <span>{{ authStore.roles.join(' / ') }}</span>
          </div>
          <el-button type="primary" plain @click="handleLogout">退出登录</el-button>
        </div>
      </header>

      <section class="content app-card">
        <router-view />
      </section>
    </main>
  </div>
</template>

<style scoped lang="scss">
.shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 280px minmax(0, 1fr);
  gap: 18px;
  padding: 18px;
}

.sidebar {
  padding: 22px 18px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.brand-block {
  padding: 12px;

  h1 {
    margin: 12px 0 8px;
    font-family: var(--font-display);
    font-size: 28px;
  }

  p {
    margin: 0;
    color: var(--text-secondary);
    line-height: 1.7;
  }
}

.brand-tag {
  display: inline-flex;
  padding: 6px 12px;
  border-radius: 999px;
  background: var(--brand-soft);
  color: var(--brand-deep);
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.menu-groups {
  display: grid;
  gap: 18px;
}

.menu-group {
  display: grid;
  gap: 8px;
}

.group-title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 10px;
  color: var(--text-secondary);
  font-size: 13px;
}

:deep(.side-menu) {
  border-right: none;
  background: transparent;
}

:deep(.side-menu .el-menu-item) {
  border-radius: 14px;
  margin-bottom: 6px;
  color: var(--text-primary);
}

:deep(.side-menu .el-menu-item.is-active) {
  background: linear-gradient(135deg, rgba(161, 100, 47, 0.16), rgba(111, 149, 96, 0.12));
  color: var(--brand-deep);
}

.main-panel {
  display: grid;
  grid-template-rows: auto 1fr;
  gap: 18px;
}

.topbar {
  padding: 18px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
}

.eyebrow {
  color: var(--text-secondary);
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.topbar-title {
  margin-top: 6px;
  font-size: 28px;
  font-family: var(--font-display);
}

.user-tools {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-chip {
  padding: 10px 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.65);
  border: 1px solid var(--line-soft);
}

.user-chip strong,
.user-chip span {
  display: block;
}

.user-chip span {
  margin-top: 4px;
  color: var(--text-secondary);
  font-size: 12px;
}

.content {
  padding: 24px;
}

@media (max-width: 1024px) {
  .shell {
    grid-template-columns: 1fr;
  }

  .sidebar {
    order: 2;
  }
}

@media (max-width: 720px) {
  .shell {
    padding: 12px;
  }

  .topbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .user-tools {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
