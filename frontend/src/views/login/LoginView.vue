<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'

import { useAuthStore } from '../../stores/auth'
import { usePortalStore } from '../../stores/portal'

const router = useRouter()
const authStore = useAuthStore()
const portalStore = usePortalStore()
const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: 'Admin@123456',
})

async function submit() {
  loading.value = true
  try {
    await authStore.login(form)
    await authStore.fetchProfile()
    await portalStore.loadMenusAndDashboard()
    ElMessage.success('欢迎回来')
    router.replace(portalStore.defaultRoute)
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-screen">
    <section class="hero-panel">
      <div class="hero-copy">
        <span class="hero-tag">AI-enabled continuing education</span>
        <h1>启智继续教育管理系统</h1>
        <p>
          以课程为核心，把教学、学习、考核、证书与智能辅导组织成一套统一的继续教育体验，让管理更有秩序，让学习更有节奏。
        </p>
      </div>
      <div class="hero-badge-row">
        <div class="hero-badge">
          <strong>3 端</strong>
          <span>管理员 / 教师 / 学员协同</span>
        </div>
        <div class="hero-badge">
          <strong>AI</strong>
          <span>课程辅导与学习问答贯通</span>
        </div>
      </div>
      <div class="hero-grid">
        <article class="hero-card app-card">
          <strong>管理员</strong>
          <span>平台总览、课程审核、证书审核、公告与数据看板</span>
        </article>
        <article class="hero-card app-card">
          <strong>教师</strong>
          <span>课程、考试、作业与 AI 学情记录管理</span>
        </article>
        <article class="hero-card app-card">
          <strong>学生</strong>
          <span>学习、考试、作业、证书与课程 AI 辅导</span>
        </article>
      </div>
    </section>

    <section class="login-panel app-card">
      <div class="login-head">
        <span>Sign In</span>
        <h2>进入平台</h2>
      </div>

      <el-form label-position="top" @submit.prevent="submit">
        <el-form-item label="账号">
          <el-input v-model="form.username" size="large" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" size="large" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-button class="submit-btn" type="primary" size="large" :loading="loading" @click="submit">
          登录系统
        </el-button>
      </el-form>

      <div class="login-tip">
        默认管理员账号：`admin` / `Admin@123456`
      </div>
    </section>
  </div>
</template>

<style scoped lang="scss">
.login-screen {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.2fr 0.9fr;
  gap: 24px;
  padding: 28px;
}

.hero-panel,
.login-panel {
  padding: 36px;
}

.hero-panel {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background: linear-gradient(155deg, rgba(255, 250, 242, 0.82), rgba(248, 243, 236, 0.76));
}

.hero-copy {
  max-width: 780px;
}

.hero-tag {
  display: inline-flex;
  align-self: flex-start;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid var(--line-soft);
  color: var(--brand-deep);
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-panel h1 {
  margin: 24px 0 12px;
  max-width: 11ch;
  font-family: var(--font-display);
  font-size: 72px;
  line-height: 0.98;
}

.hero-panel p {
  max-width: 620px;
  color: var(--text-secondary);
  line-height: 1.9;
  font-size: 16px;
}

.hero-badge-row {
  display: flex;
  gap: 14px;
  margin-top: 18px;
  flex-wrap: wrap;
}

.hero-badge {
  min-width: 180px;
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(92, 71, 44, 0.08);
}

.hero-badge strong,
.hero-badge span {
  display: block;
}

.hero-badge strong {
  font-family: var(--font-display);
  font-size: 28px;
}

.hero-badge span {
  margin-top: 4px;
  color: var(--text-secondary);
}

.hero-grid {
  display: grid;
  gap: 14px;
  margin-top: 32px;
}

.hero-card {
  padding: 20px;
}

.hero-card strong,
.hero-card span {
  display: block;
}

.hero-card span {
  margin-top: 8px;
  color: var(--text-secondary);
  line-height: 1.7;
}

.login-panel {
  max-width: 520px;
  margin-left: auto;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: linear-gradient(180deg, rgba(255,255,255,.88), rgba(251,246,239,.88));
}

.login-head span {
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-size: 12px;
}

.login-head h2 {
  margin: 10px 0 24px;
  font-family: var(--font-display);
  font-size: 42px;
}

.submit-btn {
  width: 100%;
  margin-top: 10px;
  border-radius: 16px;
  height: 48px;
}

.login-tip {
  margin-top: 18px;
  color: var(--text-secondary);
  font-size: 13px;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(166, 102, 46, 0.08);
}

@media (max-width: 960px) {
  .login-screen {
    grid-template-columns: 1fr;
    padding: 16px;
  }

  .hero-panel h1 {
    font-size: 48px;
    max-width: none;
  }

  .login-panel {
    max-width: none;
  }
}
</style>
