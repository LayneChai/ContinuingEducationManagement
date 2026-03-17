import http from './http'

export const getStudentAiSessionsApi = (params) => http.get('/student/ai/sessions', { params })
export const getStudentAiMessagesApi = (sessionId) => http.get(`/student/ai/sessions/${sessionId}/messages`)

export const getTeacherAiSessionsApi = (params) => http.get('/teacher/ai/sessions', { params })
export const getTeacherAiMessagesApi = (sessionId) => http.get(`/teacher/ai/sessions/${sessionId}/messages`)

export async function streamStudentAiChat(payload) {
  const token = localStorage.getItem('ce_token')
  const response = await fetch('/api/student/ai/chat/stream', {
    method: 'POST',
    headers: {
      Accept: 'text/event-stream',
      'Cache-Control': 'no-cache',
      'Content-Type': 'application/json',
      satoken: token || '',
    },
    body: JSON.stringify(payload),
  })

  if (!response.ok || !response.body) {
    throw new Error('AI 流式连接失败')
  }

  return response
}
