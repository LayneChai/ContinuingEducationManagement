<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import DOMPurify from 'dompurify'
import { marked } from 'marked'

import { streamStudentAiChat } from '../../api/ai'
import { getStudentCoursesApi } from '../../api/course'

const courseId = ref(null)
const courses = ref([])
const message = ref('请帮我解释这门课的核心知识点')
const answer = ref('')
const sessionId = ref(null)
const streaming = ref(false)

marked.setOptions({ breaks: true, gfm: true })

const renderedAnswer = computed(() => DOMPurify.sanitize(marked.parse(answer.value || '')))

onMounted(loadCourses)

async function loadCourses() {
  try {
    courses.value = await getStudentCoursesApi()
    if (!courseId.value && courses.value.length) {
      courseId.value = courses.value[0].id
    }
  } catch (error) {
    ElMessage.error(error.message || '课程加载失败')
  }
}

async function sendMessage() {
  if (!courseId.value) {
    ElMessage.warning('请先选择已报名课程')
    return
  }
  answer.value = ''
  streaming.value = true
  try {
    const response = await streamStudentAiChat({
      sessionId: sessionId.value,
      courseId: courseId.value,
      message: message.value,
    })

    const reader = response.body.getReader()
    const decoder = new TextDecoder('utf-8')
    let buffer = ''

    while (true) {
      const { value, done } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      const events = buffer.split(/\r?\n\r?\n/)
      buffer = events.pop() || ''

      events.forEach((eventBlock) => {
        let eventName = 'message'
        let data = ''
        eventBlock.split(/\r?\n/).forEach((line) => {
          if (line.startsWith('event:')) eventName = line.slice(6).trim()
          if (line.startsWith('data:')) data += line.slice(5)
        })
        if (eventName === 'session') {
          sessionId.value = Number(data)
        } else if (eventName === 'message') {
          answer.value += data
        } else if (eventName === 'error') {
          ElMessage.error(data)
        }
      })
    }
  } catch (error) {
    ElMessage.error(error.message || 'AI 请求失败')
  } finally {
    streaming.value = false
  }
}
</script>

<template>
  <div class="page-wrap">
    <div class="page-heading">
      <div>
        <h1>AI 辅导</h1>
        <p>接入课程上下文的 DeepSeek 流式辅导窗口，支持继续沿用历史会话。</p>
      </div>
    </div>

    <section class="content-grid ai-grid">
      <article class="app-card panel-block">
        <h3>发起对话</h3>
        <el-form label-position="top">
          <el-form-item label="已报名课程">
            <el-select v-model="courseId" placeholder="请选择已报名课程" style="width: 100%" no-data-text="暂无已报名课程">
              <el-option v-for="item in courses" :key="item.id" :label="item.title" :value="item.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="问题">
            <el-input v-model="message" type="textarea" :rows="5" />
          </el-form-item>
          <el-button type="primary" :loading="streaming" @click="sendMessage">发送并流式接收</el-button>
        </el-form>
      </article>

      <article class="app-card panel-block">
        <h3>回答内容</h3>
        <div class="chat-stream">
          <p v-if="!answer" class="empty">等待 AI 返回内容...</p>
          <div v-else class="markdown-body" v-html="renderedAnswer"></div>
        </div>
      </article>
    </section>
  </div>
</template>

<style scoped>
.ai-grid {
  grid-template-columns: 0.95fr 1.05fr;
}

.chat-stream {
  min-height: 320px;
  padding: 18px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid var(--line-soft);
  line-height: 1.8;
}

.empty {
  color: var(--text-secondary);
}

.markdown-body :deep(p) {
  margin: 0 0 12px;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  margin: 0 0 12px;
  padding-left: 20px;
}

.markdown-body :deep(li) {
  margin-bottom: 6px;
}

.markdown-body :deep(pre) {
  margin: 12px 0;
  padding: 14px;
  overflow: auto;
  border-radius: 12px;
  background: rgba(34, 41, 47, 0.92);
  color: #f3f5f7;
}

.markdown-body :deep(code) {
  padding: 2px 6px;
  border-radius: 6px;
  background: rgba(161, 100, 47, 0.12);
  font-family: Consolas, 'Courier New', monospace;
}

.markdown-body :deep(pre code) {
  padding: 0;
  background: transparent;
}

.markdown-body :deep(blockquote) {
  margin: 12px 0;
  padding: 8px 14px;
  border-left: 4px solid rgba(161, 100, 47, 0.55);
  background: rgba(161, 100, 47, 0.08);
  color: var(--text-secondary);
}

@media (max-width: 960px) {
  .ai-grid {
    grid-template-columns: 1fr;
  }
}
</style>
