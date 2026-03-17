<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { getTeacherCoursesApi } from '../../../api/course'
import { getTeacherAiMessagesApi, getTeacherAiSessionsApi } from '../../../api/ai'

const courses = ref([])
const selectedCourseId = ref(null)
const sessions = ref([])
const activeSessionId = ref(null)
const messages = ref([])
const loading = ref(false)

const metrics = computed(() => [
  { label: '会话总数', value: sessions.value.length },
  { label: '学生人数', value: new Set(sessions.value.map((item) => item.userId)).size },
  { label: '消息总量', value: sessions.value.reduce((sum, item) => sum + (item.messageCount || 0), 0) },
  { label: '当前会话消息', value: messages.value.length },
])

onMounted(async () => {
  await loadCourses()
})

async function loadCourses() {
  courses.value = await getTeacherCoursesApi({ status: 1 })
  selectedCourseId.value = courses.value[0]?.id || null
  await loadSessions()
}

async function loadSessions() {
  loading.value = true
  try {
    sessions.value = await getTeacherAiSessionsApi(selectedCourseId.value ? { courseId: selectedCourseId.value } : undefined)
    const targetId = activeSessionId.value || sessions.value[0]?.id
    if (targetId) {
      await openSession(targetId)
    } else {
      activeSessionId.value = null
      messages.value = []
    }
  } finally {
    loading.value = false
  }
}

async function openSession(sessionId) {
  activeSessionId.value = sessionId
  try {
    messages.value = await getTeacherAiMessagesApi(sessionId)
  } catch (error) {
    ElMessage.error(error.message || '加载会话消息失败')
  }
}

function roleLabel(role) {
  return role === 'assistant' ? 'AI 助手' : role === 'user' ? '学生提问' : role
}
</script>

<template>
  <div class="page-wrap teacher-ai-page">
    <div class="page-heading">
      <div>
        <h1>AI 记录</h1>
        <p>教师可按课程查看学生与课程 AI 助手的历史会话，掌握学生提问重点与辅导轨迹。</p>
      </div>
      <div class="top-actions">
        <el-select v-model="selectedCourseId" clearable placeholder="选择课程" style="width: 260px" @change="loadSessions">
          <el-option v-for="course in courses" :key="course.id" :label="course.title" :value="course.id" />
        </el-select>
        <el-button @click="loadSessions">刷新</el-button>
      </div>
    </div>

    <section class="stat-grid">
      <article v-for="item in metrics" :key="item.label" class="app-card stat-card">
        <div class="label">{{ item.label }}</div>
        <div class="value">{{ item.value }}</div>
      </article>
    </section>

    <section class="workspace">
      <aside class="app-card panel-block list-panel">
        <div class="block-head">
          <h3>会话列表</h3>
          <span>{{ sessions.length }} 条</span>
        </div>
        <div class="soft-list dense-list">
          <button
            v-for="session in sessions"
            :key="session.id"
            class="session-item"
            :class="{ active: session.id === activeSessionId }"
            @click="openSession(session.id)"
          >
            <div class="session-top">
              <strong>{{ session.title }}</strong>
              <el-tag size="small">{{ session.courseTitle }}</el-tag>
            </div>
            <span>学生：{{ session.userName || '未知用户' }}</span>
            <span>消息数：{{ session.messageCount || 0 }} · 最近更新：{{ session.lastMessageTime || '--' }}</span>
          </button>
          <div v-if="!sessions.length && !loading" class="soft-item">
            <strong>暂无 AI 会话</strong>
            <span>当前课程下还没有学生发起 AI 辅导对话。</span>
          </div>
        </div>
      </aside>

      <main class="app-card panel-block message-panel">
        <div v-if="!activeSessionId" class="empty-state">请选择左侧会话查看消息详情。</div>
        <template v-else>
          <div class="block-head">
            <h3>消息详情</h3>
            <span>{{ messages.length }} 条消息</span>
          </div>
          <div class="message-list">
            <article
              v-for="message in messages"
              :key="message.id"
              class="message-item"
              :class="message.role"
            >
              <div class="message-meta">
                <strong>{{ roleLabel(message.role) }}</strong>
                <span>{{ message.createTime || '--' }}</span>
              </div>
              <div class="message-content">{{ message.content }}</div>
            </article>
          </div>
        </template>
      </main>
    </section>
  </div>
</template>

<style scoped lang="scss">
.teacher-ai-page { gap: 18px; }
.top-actions, .block-head, .session-top, .message-meta { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.workspace { display: grid; grid-template-columns: 360px minmax(0, 1fr); gap: 18px; }
.dense-list { margin-top: 14px; }
.session-item {
  text-align: left;
  display: grid;
  gap: 8px;
  padding: 16px;
  border-radius: 16px;
  border: 1px solid var(--line-soft);
  background: rgba(255,255,255,.62);
  cursor: pointer;
}
.session-item.active { border-color: rgba(161,100,47,.45); background: rgba(255,248,240,.95); }
.session-item span, .message-meta span, .empty-state { color: var(--text-secondary); }
.message-list { display: grid; gap: 14px; margin-top: 14px; }
.message-item {
  padding: 16px;
  border-radius: 18px;
  border: 1px solid var(--line-soft);
  background: rgba(255,255,255,.68);
}
.message-item.assistant {
  background: rgba(246, 249, 242, 0.9);
}
.message-content {
  margin-top: 10px;
  line-height: 1.8;
  white-space: pre-wrap;
}
@media (max-width: 1024px) { .workspace { grid-template-columns: 1fr; } }
@media (max-width: 760px) { .top-actions, .message-meta { flex-direction: column; align-items: flex-start; } }
</style>
