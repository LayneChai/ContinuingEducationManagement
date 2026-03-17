<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import { getTeacherCoursesApi } from '../../../api/course'
import {
  createTeacherExamApi,
  createTeacherQuestionApi,
  deleteTeacherQuestionApi,
  getTeacherExamDetailApi,
  getTeacherExamsApi,
  getTeacherQuestionsApi,
  publishTeacherExamApi,
  updateTeacherExamApi,
  updateTeacherQuestionApi,
} from '../../../api/exam'

const loading = ref(false)
const courses = ref([])
const selectedCourseId = ref(null)
const questions = ref([])
const exams = ref([])
const activeExamId = ref(null)
const examDetail = ref(null)

const questionDialog = reactive({
  visible: false,
  isEdit: false,
  saving: false,
  questionId: null,
  form: defaultQuestionForm(),
})

const examDialog = reactive({
  visible: false,
  isEdit: false,
  saving: false,
  examId: null,
  form: defaultExamForm(),
})

const metrics = computed(() => [
  { label: '课程考试', value: exams.value.length },
  { label: '题库数量', value: questions.value.length },
  { label: '已发布考试', value: exams.value.filter((item) => item.status === 1).length },
  { label: '简答题数量', value: questions.value.filter((item) => item.questionType === 4).length },
])

const selectedQuestionMap = computed(() => {
  const ids = new Set(examDialog.form.questionIds || [])
  return questions.value.filter((item) => ids.has(item.id))
})

onMounted(async () => {
  await loadCourses()
})

function defaultQuestionForm() {
  return {
    courseId: null,
    questionType: 1,
    stem: '',
    analysis: '',
    difficulty: 1,
    score: 10,
    correctAnswer: '',
    status: 1,
    options: [
      { optionLabel: 'A', optionContent: '', isCorrect: 1, sort: 1 },
      { optionLabel: 'B', optionContent: '', isCorrect: 0, sort: 2 },
      { optionLabel: 'C', optionContent: '', isCorrect: 0, sort: 3 },
      { optionLabel: 'D', optionContent: '', isCorrect: 0, sort: 4 },
    ],
  }
}

function defaultExamForm() {
  return {
    courseId: null,
    title: '',
    description: '',
    durationMinutes: 60,
    passScore: 60,
    attemptLimit: 1,
    startTime: '',
    endTime: '',
    questionIds: [],
  }
}

async function loadCourses() {
  courses.value = await getTeacherCoursesApi({ status: 1 })
  selectedCourseId.value = courses.value[0]?.id || null
  if (selectedCourseId.value) {
    await loadCourseResources()
  }
}

async function loadCourseResources() {
  if (!selectedCourseId.value) return
  loading.value = true
  try {
    const [questionData, examData] = await Promise.all([
      getTeacherQuestionsApi(selectedCourseId.value),
      getTeacherExamsApi({ courseId: selectedCourseId.value }),
    ])
    questions.value = questionData
    exams.value = examData
    const targetId = activeExamId.value || exams.value[0]?.id
    if (targetId) {
      await loadExamDetail(targetId)
    } else {
      activeExamId.value = null
      examDetail.value = null
    }
  } finally {
    loading.value = false
  }
}

async function loadExamDetail(examId) {
  activeExamId.value = examId
  examDetail.value = await getTeacherExamDetailApi(examId)
}

function onCourseChange() {
  activeExamId.value = null
  examDetail.value = null
  loadCourseResources()
}

function openCreateQuestion() {
  questionDialog.visible = true
  questionDialog.isEdit = false
  questionDialog.questionId = null
  questionDialog.form = { ...defaultQuestionForm(), courseId: selectedCourseId.value }
}

function openEditQuestion(question) {
  questionDialog.visible = true
  questionDialog.isEdit = true
  questionDialog.questionId = question.id
  questionDialog.form = {
    courseId: question.courseId,
    questionType: question.questionType,
    stem: question.stem,
    analysis: question.analysis,
    difficulty: question.difficulty,
    score: Number(question.score || 0),
    correctAnswer: question.correctAnswer,
    status: question.status,
    options: (question.options || []).map((item, index) => ({
      optionLabel: item.optionLabel,
      optionContent: item.optionContent,
      isCorrect: parseCorrectFlag(question.correctAnswer, item.optionLabel, question.questionType),
      sort: item.sort ?? index + 1,
    })),
  }
}

function addOption() {
  const label = String.fromCharCode(65 + questionDialog.form.options.length)
  questionDialog.form.options.push({ optionLabel: label, optionContent: '', isCorrect: 0, sort: questionDialog.form.options.length + 1 })
}

function removeOption(index) {
  questionDialog.form.options.splice(index, 1)
}

async function submitQuestion() {
  questionDialog.saving = true
  try {
    const payload = buildQuestionPayload(questionDialog.form)
    if (questionDialog.isEdit) {
      await updateTeacherQuestionApi(questionDialog.questionId, payload)
      ElMessage.success('题目已更新')
    } else {
      await createTeacherQuestionApi(payload)
      ElMessage.success('题目已创建')
    }
    questionDialog.visible = false
    await loadCourseResources()
  } catch (error) {
    ElMessage.error(error.message || '保存题目失败')
  } finally {
    questionDialog.saving = false
  }
}

async function removeQuestion(question) {
  try {
    await ElMessageBox.confirm(`确定删除题目「${question.stem.slice(0, 18)}...」吗？`, '删除题目', { type: 'warning' })
    await deleteTeacherQuestionApi(question.id)
    ElMessage.success('题目已删除')
    await loadCourseResources()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

function openCreateExam() {
  examDialog.visible = true
  examDialog.isEdit = false
  examDialog.examId = null
  examDialog.form = { ...defaultExamForm(), courseId: selectedCourseId.value }
}

function openEditExam(exam) {
  examDialog.visible = true
  examDialog.isEdit = true
  examDialog.examId = exam.id
  examDialog.form = {
    courseId: exam.courseId,
    title: exam.title,
    description: exam.description,
    durationMinutes: exam.durationMinutes,
    passScore: Number(exam.passScore || 60),
    attemptLimit: exam.attemptLimit,
    startTime: exam.startTime,
    endTime: exam.endTime,
    questionIds: (exam.questions || []).map((item) => item.id),
  }
}

async function submitExam() {
  examDialog.saving = true
  try {
    const payload = { ...examDialog.form }
    if (examDialog.isEdit) {
      await updateTeacherExamApi(examDialog.examId, payload)
      ElMessage.success('考试已更新')
      await loadCourseResources()
      await loadExamDetail(examDialog.examId)
    } else {
      const result = await createTeacherExamApi(payload)
      ElMessage.success('考试已创建')
      await loadCourseResources()
      await loadExamDetail(result.examId)
    }
    examDialog.visible = false
  } catch (error) {
    ElMessage.error(error.message || '保存考试失败')
  } finally {
    examDialog.saving = false
  }
}

async function publishExam(exam) {
  try {
    await publishTeacherExamApi(exam.id)
    ElMessage.success('考试已发布')
    await loadCourseResources()
    await loadExamDetail(exam.id)
  } catch (error) {
    ElMessage.error(error.message || '发布失败')
  }
}

function buildQuestionPayload(form) {
  const payload = {
    ...form,
    score: Number(form.score),
    options: form.questionType === 4 ? [] : form.options.map((item, index) => ({
      optionLabel: item.optionLabel,
      optionContent: item.optionContent,
      isCorrect: item.isCorrect ? 1 : 0,
      sort: item.sort ?? index + 1,
    })),
  }
  payload.correctAnswer = resolveCorrectAnswer(form)
  return payload
}

function resolveCorrectAnswer(form) {
  if (form.questionType === 4) {
    return form.correctAnswer || ''
  }
  if (form.questionType === 3) {
    return form.correctAnswer || 'TRUE'
  }
  const labels = form.options.filter((item) => item.isCorrect).map((item) => item.optionLabel)
  return labels.join(',')
}

function parseCorrectFlag(correctAnswer, optionLabel, questionType) {
  if (questionType === 4) return 0
  const targets = String(correctAnswer || '').split(',').map((item) => item.trim().toUpperCase())
  return targets.includes(String(optionLabel).toUpperCase()) ? 1 : 0
}

function questionTypeText(type) {
  return ['单选', '多选', '判断', '简答'][type - 1] || '未知'
}

function examStatusText(status) {
  return ['未发布', '已发布', '已结束'][status] || '未知'
}
</script>

<template>
  <div class="page-wrap exam-page">
    <div class="page-heading">
      <div>
        <h1>考试管理</h1>
        <p>基于课程题库快速组卷、发布考试，并查看每场考试的题目结构。</p>
      </div>
      <div class="top-actions">
        <el-select v-model="selectedCourseId" placeholder="选择课程" style="width: 260px" @change="onCourseChange">
          <el-option v-for="course in courses" :key="course.id" :label="course.title" :value="course.id" />
        </el-select>
        <el-button @click="openCreateQuestion">新增题目</el-button>
        <el-button type="primary" @click="openCreateExam">新建考试</el-button>
      </div>
    </div>

    <section class="stat-grid">
      <article v-for="item in metrics" :key="item.label" class="app-card stat-card">
        <div class="label">{{ item.label }}</div>
        <div class="value">{{ item.value }}</div>
      </article>
    </section>

    <section class="workspace">
      <aside class="left-column">
        <article class="app-card panel-block">
          <div class="block-head">
            <h3>题库</h3>
            <span>{{ questions.length }} 题</span>
          </div>
          <div class="soft-list dense-list">
            <div v-for="question in questions" :key="question.id" class="soft-item">
              <strong>{{ question.stem }}</strong>
              <span>{{ questionTypeText(question.questionType) }} · {{ question.score }} 分</span>
              <div class="item-actions">
                <el-button text @click="openEditQuestion(question)">编辑</el-button>
                <el-button text type="danger" @click="removeQuestion(question)">删除</el-button>
              </div>
            </div>
          </div>
        </article>
      </aside>

      <main class="right-column">
        <article class="app-card panel-block">
          <div class="block-head">
            <h3>考试列表</h3>
            <span>{{ exams.length }} 场</span>
          </div>
          <div class="exam-list">
            <button
              v-for="exam in exams"
              :key="exam.id"
              class="exam-item"
              :class="{ active: exam.id === activeExamId }"
              @click="loadExamDetail(exam.id)"
            >
              <div class="exam-top">
                <strong>{{ exam.title }}</strong>
                <el-tag size="small" :type="exam.status === 1 ? 'success' : 'info'">{{ examStatusText(exam.status) }}</el-tag>
              </div>
              <span>{{ exam.questionCount }} 题 · {{ exam.totalScore }} 分 · {{ exam.durationMinutes }} 分钟</span>
            </button>
          </div>
        </article>

        <article class="app-card panel-block detail-panel">
          <div v-if="!examDetail" class="empty-state">请选择一场考试查看详情。</div>
          <template v-else>
            <div class="block-head">
              <div>
                <h3>{{ examDetail.title }}</h3>
                <p>{{ examDetail.description || '暂无考试说明' }}</p>
              </div>
              <div class="top-actions">
                <el-button @click="openEditExam(examDetail)">编辑考试</el-button>
                <el-button type="primary" plain @click="publishExam(examDetail)">发布考试</el-button>
              </div>
            </div>
            <div class="meta-row">
              <span>总分 {{ examDetail.totalScore }}</span>
              <span>及格 {{ examDetail.passScore }}</span>
              <span>限时 {{ examDetail.durationMinutes }} 分钟</span>
              <span>次数 {{ examDetail.attemptLimit }}</span>
            </div>
            <div class="soft-list dense-list">
              <div v-for="item in examDetail.questions || []" :key="item.id" class="soft-item">
                <strong>{{ item.stem }}</strong>
                <span>{{ questionTypeText(item.questionType) }} · {{ item.score }} 分</span>
              </div>
            </div>
          </template>
        </article>
      </main>
    </section>

    <el-dialog v-model="questionDialog.visible" :title="questionDialog.isEdit ? '编辑题目' : '新增题目'" width="760px">
      <el-form label-position="top">
        <div class="dialog-grid two-col">
          <el-form-item label="题型">
            <el-select v-model="questionDialog.form.questionType">
              <el-option label="单选" :value="1" />
              <el-option label="多选" :value="2" />
              <el-option label="判断" :value="3" />
              <el-option label="简答" :value="4" />
            </el-select>
          </el-form-item>
          <el-form-item label="分值"><el-input-number v-model="questionDialog.form.score" :min="1" /></el-form-item>
        </div>
        <el-form-item label="题干"><el-input v-model="questionDialog.form.stem" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="题目解析"><el-input v-model="questionDialog.form.analysis" type="textarea" :rows="2" /></el-form-item>
        <template v-if="questionDialog.form.questionType !== 4">
          <div class="option-head">
            <h4>选项配置</h4>
            <el-button text @click="addOption">新增选项</el-button>
          </div>
          <div class="option-list">
            <div v-for="(option, index) in questionDialog.form.options" :key="index" class="option-row">
              <el-checkbox v-model="option.isCorrect" :true-value="1" :false-value="0" />
              <el-input v-model="option.optionLabel" style="width: 72px" />
              <el-input v-model="option.optionContent" />
              <el-button text type="danger" @click="removeOption(index)">删除</el-button>
            </div>
          </div>
        </template>
        <template v-else>
          <el-form-item label="参考答案"><el-input v-model="questionDialog.form.correctAnswer" type="textarea" :rows="3" /></el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="questionDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="questionDialog.saving" @click="submitQuestion">保存题目</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="examDialog.visible" :title="examDialog.isEdit ? '编辑考试' : '新建考试'" width="760px">
      <el-form label-position="top">
        <div class="dialog-grid two-col">
          <el-form-item label="考试名称"><el-input v-model="examDialog.form.title" /></el-form-item>
          <el-form-item label="考试时长(分钟)"><el-input-number v-model="examDialog.form.durationMinutes" :min="10" /></el-form-item>
        </div>
        <div class="dialog-grid three-col">
          <el-form-item label="及格分"><el-input-number v-model="examDialog.form.passScore" :min="1" /></el-form-item>
          <el-form-item label="作答次数"><el-input-number v-model="examDialog.form.attemptLimit" :min="1" /></el-form-item>
          <el-form-item label="课程"><el-input :model-value="courses.find((item) => item.id === selectedCourseId)?.title || ''" disabled /></el-form-item>
        </div>
        <div class="dialog-grid two-col">
          <el-form-item label="开始时间"><el-date-picker v-model="examDialog.form.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
          <el-form-item label="结束时间"><el-date-picker v-model="examDialog.form.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
        </div>
        <el-form-item label="考试说明"><el-input v-model="examDialog.form.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="选择题目">
          <el-select v-model="examDialog.form.questionIds" multiple filterable style="width: 100%">
            <el-option v-for="item in questions" :key="item.id" :label="`${item.stem}（${item.score}分）`" :value="item.id" />
          </el-select>
        </el-form-item>
        <div class="selected-question-list">
          <div v-for="item in selectedQuestionMap" :key="item.id" class="soft-item">
            <strong>{{ item.stem }}</strong>
            <span>{{ questionTypeText(item.questionType) }} · {{ item.score }} 分</span>
          </div>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="examDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="examDialog.saving" @click="submitExam">保存考试</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.exam-page { gap: 18px; }
.top-actions { display: flex; gap: 10px; flex-wrap: wrap; }
.workspace { display: grid; grid-template-columns: 0.9fr 1.1fr; gap: 18px; }
.left-column, .right-column { display: grid; gap: 18px; }
.block-head, .exam-top, .option-head { display: flex; justify-content: space-between; align-items: center; gap: 10px; }
.block-head h3, .option-head h4 { margin: 0; }
.dense-list { margin-top: 14px; }
.item-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 6px; }
.exam-list { display: grid; gap: 10px; margin-top: 14px; }
.exam-item {
  text-align: left; border: 1px solid var(--line-soft); background: rgba(255,255,255,.6); border-radius: 16px; padding: 16px; cursor: pointer; display: grid; gap: 8px;
}
.exam-item.active { border-color: rgba(161,100,47,.45); background: rgba(255,248,240,.95); }
.detail-panel p, .soft-item span, .exam-item span, .meta-row { color: var(--text-secondary); }
.meta-row { display: flex; flex-wrap: wrap; gap: 16px; margin: 10px 0 16px; }
.dialog-grid { display: grid; gap: 14px; }
.two-col { grid-template-columns: repeat(2, 1fr); }
.three-col { grid-template-columns: repeat(3, 1fr); }
.option-list, .selected-question-list { display: grid; gap: 10px; }
.option-row { display: grid; grid-template-columns: 40px 80px 1fr auto; gap: 10px; align-items: center; }
.empty-state { color: var(--text-secondary); }

@media (max-width: 1024px) { .workspace { grid-template-columns: 1fr; } }
@media (max-width: 760px) {
  .two-col, .three-col, .option-row { grid-template-columns: 1fr; }
}
</style>
