<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

import { streamStudentAiChat } from '../../api/ai'

const courseId = ref(1)
const message = ref('请帮我解释这门课的核心知识点')
const chunks = ref([])
const sessionId = ref(null)
const streaming = ref(false)

async function sendMessage() {
  chunks.value = []
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
      const events = buffer.split('\n\n')
      buffer = events.pop() || ''

      events.forEach((eventBlock) => {
        let eventName = 'message'
        let data = ''
        eventBlock.split('\n').forEach((line) => {
          if (line.startsWith('event:')) eventName = line.slice(6).trim()
          if (line.startsWith('data:')) data += line.slice(5).trim()
        })
        if (eventName === 'session') {
          sessionId.value = Number(data)
        } else if (eventName === 'message') {
          chunks.value.push(data)
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
          <el-form-item label="课程 ID">
            <el-input-number v-model="courseId" :min="1" />
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
          <p v-if="!chunks.length" class="empty">等待 AI 返回内容...</p>
          <p v-else>{{ chunks.join('') }}</p>
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
  white-space: pre-wrap;
}

.empty {
  color: var(--text-secondary);
}

@media (max-width: 960px) {
  .ai-grid {
    grid-template-columns: 1fr;
  }
}
</style>
