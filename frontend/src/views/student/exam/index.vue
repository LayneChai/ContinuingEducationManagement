<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { getStudentCoursesApi } from '../../../api/course'
import { getStudentExamDetailApi, getStudentExamsApi, submitStudentExamApi } from '../../../api/exam'

const loading = ref(false)
const courses = ref([])
const selectedCourseId = ref(null)
const exams = ref([])
const activeExamId = ref(null)
const examDetail = ref(null)
const submitting = ref(false)
const result = ref(null)
const answers = reactive({})

const metrics = computed(() => [
  { label: '可参加考试', value: exams.value.length },
  { label: '已参加次数', value: exams.value.reduce((sum, item) => sum + (item.myAttemptCount || 0), 0) },
  { label: '通过考试', value: exams.value.filter((item) => item.latestPass === 1).length },
  { label: '当前选题数', value: examDetail.value?.questionCount || 0 },
])

onMounted(async () => {
  await loadCourses()
})

async function loadCourses() {
  courses.value = await getStudentCoursesApi()
  selectedCourseId.value = courses.value[0]?.id || null
  await loadExams()
}

async function loadExams() {
  loading.value = true
  try {
    exams.value = await getStudentExamsApi(selectedCourseId.value ? { courseId: selectedCourseId.value } : undefined)
    const targetId = activeExamId.value || exams.value[0]?.id
    if (targetId) {
      await openExam(targetId)
    } else {
      activeExamId.value = null
      examDetail.value = null
      result.value = null
    }
  } finally {
    loading.value = false
  }
}

async function openExam(examId) {
  activeExamId.value = examId
  examDetail.value = await getStudentExamDetailApi(examId)
  result.value = null
  resetAnswers()
}

function resetAnswers() {
  Object.keys(answers).forEach((key) => delete answers[key])
  ;(examDetail.value?.questions || []).forEach((question) => {
    answers[question.id] = question.questionType === 2 ? [] : ''
  })
}

function questionTypeText(type) {
  return ['单选', '多选', '判断', '简答'][type - 1] || '未知'
}

function examStatusText(status) {
  return ['未发布', '已发布', '已结束'][status] || '未知'
}

function buildSubmitPayload() {
  return {
    answers: (examDetail.value?.questions || []).map((question) => ({
      questionId: question.id,
      answer: Array.isArray(answers[question.id]) ? answers[question.id].join(',') : answers[question.id],
    })),
  }
}

async function submitExam() {
  if (!examDetail.value) return
  try {
    await ElMessageBox.confirm('确认提交本次考试答案吗？提交后将立即判定客观题成绩。', '交卷确认', { type: 'warning' })
    submitting.value = true
    result.value = await submitStudentExamApi(examDetail.value.id, buildSubmitPayload())
    ElMessage.success('交卷成功')
    await loadExams()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '交卷失败')
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="page-wrap student-exam-page">
    <div class="page-heading">
      <div>
        <h1>在线考试</h1>
        <p>查看当前课程可参加的考试，在线答题并即时获得客观题判分结果。</p>
      </div>
      <el-select v-model="selectedCourseId" clearable placeholder="按课程筛选" style="width: 260px" @change="loadExams">
        <el-option v-for="course in courses" :key="course.id" :label="course.title" :value="course.id" />
      </el-select>
    </div>

    <section class="stat-grid">
      <article v-for="item in metrics" :key="item.label" class="app-card stat-card">
        <div class="label">{{ item.label }}</div>
        <div class="value">{{ item.value }}</div>
      </article>
    </section>

    <section class="workspace">
      <aside class="app-card list-panel">
        <div class="panel-head">
          <h3>考试列表</h3>
          <span>{{ exams.length }} 场</span>
        </div>
        <div class="exam-stack">
          <button
            v-for="exam in exams"
            :key="exam.id"
            class="exam-card"
            :class="{ active: exam.id === activeExamId }"
            @click="openExam(exam.id)"
          >
            <div class="exam-top">
              <strong>{{ exam.title }}</strong>
              <el-tag size="small" :type="exam.latestPass === 1 ? 'success' : 'warning'">{{ examStatusText(exam.status) }}</el-tag>
            </div>
            <span>{{ exam.courseTitle }}</span>
            <span>{{ exam.questionCount }} 题 · {{ exam.totalScore }} 分 · 已考 {{ exam.myAttemptCount || 0 }} 次</span>
          </button>
        </div>
      </aside>

      <main class="app-card paper-panel">
        <div v-if="!examDetail" class="empty-state">请选择一场考试开始答题。</div>
        <template v-else>
          <div class="paper-head">
            <div>
              <h2>{{ examDetail.title }}</h2>
              <p>{{ examDetail.description || '暂无考试说明' }}</p>
            </div>
            <el-button type="primary" :loading="submitting" @click="submitExam">提交试卷</el-button>
          </div>

          <div class="meta-row">
            <span>课程：{{ examDetail.courseTitle }}</span>
            <span>总分：{{ examDetail.totalScore }}</span>
            <span>及格：{{ examDetail.passScore }}</span>
            <span>次数限制：{{ examDetail.attemptLimit }}</span>
            <span>已参加：{{ examDetail.myAttemptCount }}</span>
          </div>

          <div v-if="result" class="result-banner app-card">
            <strong>本次提交结果</strong>
            <span>总分 {{ result.totalScore }}，客观题 {{ result.objectiveScore }}，主观题待批改 {{ result.pendingReviewCount }} 题。</span>
          </div>

          <div class="question-list">
            <section v-for="(question, index) in examDetail.questions || []" :key="question.id" class="question-card">
              <div class="question-head">
                <strong>{{ index + 1 }}. {{ question.stem }}</strong>
                <span>{{ questionTypeText(question.questionType) }} · {{ question.score }} 分</span>
              </div>

              <div v-if="question.questionType === 1 || question.questionType === 3" class="option-group">
                <el-radio-group v-model="answers[question.id]">
                  <el-radio v-for="option in question.options || []" :key="option.id || option.optionLabel" :value="option.optionLabel">
                    {{ option.optionLabel }}. {{ option.optionContent }}
                  </el-radio>
                </el-radio-group>
              </div>

              <div v-else-if="question.questionType === 2" class="option-group">
                <el-checkbox-group v-model="answers[question.id]">
                  <el-checkbox v-for="option in question.options || []" :key="option.id || option.optionLabel" :value="option.optionLabel">
                    {{ option.optionLabel }}. {{ option.optionContent }}
                  </el-checkbox>
                </el-checkbox-group>
              </div>

              <div v-else>
                <el-input v-model="answers[question.id]" type="textarea" :rows="5" placeholder="请输入你的作答内容" />
              </div>
            </section>
          </div>
        </template>
      </main>
    </section>
  </div>
</template>

<style scoped lang="scss">
.student-exam-page { gap: 18px; }
.workspace { display: grid; grid-template-columns: 320px minmax(0, 1fr); gap: 18px; }
.list-panel, .paper-panel { padding: 20px; }
.panel-head, .exam-top, .paper-head, .question-head { display: flex; justify-content: space-between; gap: 12px; }
.panel-head h3, .paper-head h2 { margin: 0; }
.exam-stack, .question-list { display: grid; gap: 12px; margin-top: 14px; }
.exam-card, .question-card {
  text-align: left; border: 1px solid var(--line-soft); background: rgba(255,255,255,.62); border-radius: 16px; padding: 16px;
}
.exam-card { cursor: pointer; display: grid; gap: 8px; }
.exam-card.active { border-color: rgba(161,100,47,.45); background: rgba(255,248,240,.95); }
.paper-head p, .meta-row, .exam-card span, .question-head span, .empty-state { color: var(--text-secondary); }
.meta-row { display: flex; flex-wrap: wrap; gap: 16px; margin: 14px 0; }
.result-banner { padding: 16px; margin-bottom: 16px; }
.result-banner strong, .result-banner span { display: block; }
.result-banner span { margin-top: 6px; color: var(--text-secondary); }
.option-group :deep(.el-radio-group), .option-group :deep(.el-checkbox-group) { display: grid; gap: 10px; }

@media (max-width: 1024px) { .workspace { grid-template-columns: 1fr; } }
@media (max-width: 760px) {
  .paper-head, .question-head { flex-direction: column; align-items: flex-start; }
}
</style>
