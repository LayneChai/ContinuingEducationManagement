<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { getStudentCoursesApi } from '../../../api/course'
import { getStudentAssignmentDetailApi, getStudentAssignmentsApi, submitStudentAssignmentApi } from '../../../api/assignment'

const courses = ref([])
const selectedCourseId = ref(null)
const assignments = ref([])
const activeAssignmentId = ref(null)
const assignmentDetail = ref(null)
const loading = ref(false)

const submitForm = reactive({
  content: '',
  fileName: '',
  fileUrl: '',
  fileSize: null,
  fileType: '',
})
const submitting = ref(false)

const metrics = computed(() => [
  { label: '我的作业', value: assignments.value.length },
  { label: '待提交', value: assignments.value.filter((item) => item.submissionStatus === 0).length },
  { label: '待批改', value: assignments.value.filter((item) => item.submissionStatus === 1).length },
  { label: '已批改', value: assignments.value.filter((item) => item.submissionStatus === 2).length },
])

onMounted(async () => {
  await loadCourses()
})

async function loadCourses() {
  courses.value = await getStudentCoursesApi()
  selectedCourseId.value = courses.value[0]?.id || null
  await loadAssignments()
}

async function loadAssignments() {
  loading.value = true
  try {
    assignments.value = await getStudentAssignmentsApi(selectedCourseId.value ? { courseId: selectedCourseId.value } : undefined)
    const targetId = activeAssignmentId.value || assignments.value[0]?.id
    if (targetId) {
      await openAssignment(targetId)
    } else {
      activeAssignmentId.value = null
      assignmentDetail.value = null
    }
  } finally {
    loading.value = false
  }
}

async function openAssignment(assignmentId) {
  activeAssignmentId.value = assignmentId
  assignmentDetail.value = await getStudentAssignmentDetailApi(assignmentId)
  submitForm.content = assignmentDetail.value.submission?.content || ''
  submitForm.fileName = assignmentDetail.value.submission?.fileName || ''
  submitForm.fileUrl = assignmentDetail.value.submission?.fileUrl || ''
  submitForm.fileSize = null
  submitForm.fileType = ''
}

async function submitAssignment() {
  if (!assignmentDetail.value) return
  submitting.value = true
  try {
    await submitStudentAssignmentApi(assignmentDetail.value.id, submitForm)
    ElMessage.success('作业已提交')
    await Promise.all([loadAssignments(), openAssignment(assignmentDetail.value.id)])
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

function assignmentStatusText(status) {
  return ['草稿', '已发布', '已结束'][status] || '未知'
}

function submissionStatusText(status) {
  return ['未提交', '已提交', '已批改'][status] || '未提交'
}
</script>

<template>
  <div class="page-wrap student-assignment-page">
    <div class="page-heading">
      <div>
        <h1>作业提交</h1>
        <p>查看各课程作业要求、提交内容，并回看教师批改结果。</p>
      </div>
      <el-select v-model="selectedCourseId" clearable placeholder="按课程筛选" style="width: 260px" @change="loadAssignments">
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
      <aside class="app-card panel-block">
        <div class="block-head">
          <h3>作业列表</h3>
          <span>{{ assignments.length }} 份</span>
        </div>
        <div class="soft-list dense-list">
          <button
            v-for="item in assignments"
            :key="item.id"
            class="assignment-item"
            :class="{ active: item.id === activeAssignmentId }"
            @click="openAssignment(item.id)"
          >
            <strong>{{ item.title }}</strong>
            <span>{{ item.courseTitle }}</span>
            <span>{{ submissionStatusText(item.submissionStatus) }} · 评分 {{ item.reviewScore ?? '--' }}</span>
          </button>
        </div>
      </aside>

      <main class="app-card panel-block detail-panel">
        <div v-if="!assignmentDetail" class="empty-state">请选择一份作业查看详情。</div>
        <template v-else>
          <div class="block-head detail-head">
            <div>
              <h3>{{ assignmentDetail.title }}</h3>
              <p>{{ assignmentDetail.description || '暂无作业说明' }}</p>
            </div>
            <el-tag :type="assignmentDetail.submission?.status === 2 ? 'success' : 'warning'">
              {{ submissionStatusText(assignmentDetail.submission?.status || 0) }}
            </el-tag>
          </div>
          <div class="meta-row">
            <span>课程 {{ assignmentDetail.courseTitle }}</span>
            <span>总分 {{ assignmentDetail.totalScore }}</span>
            <span>重复提交 {{ assignmentDetail.allowResubmit === 1 ? '允许' : '不允许' }}</span>
            <span>截止 {{ assignmentDetail.deadline || '未设置' }}</span>
            <span>状态 {{ assignmentStatusText(assignmentDetail.status) }}</span>
          </div>

          <el-form label-position="top">
            <el-form-item label="提交内容">
              <el-input v-model="submitForm.content" type="textarea" :rows="6" placeholder="请输入作业内容" />
            </el-form-item>
            <div class="dialog-grid two-col">
              <el-form-item label="附件名称"><el-input v-model="submitForm.fileName" placeholder="如：作业答案.docx" /></el-form-item>
              <el-form-item label="附件地址"><el-input v-model="submitForm.fileUrl" placeholder="可先填写文件访问地址" /></el-form-item>
            </div>
            <el-button type="primary" :loading="submitting" @click="submitAssignment">提交作业</el-button>
          </el-form>

          <div v-if="assignmentDetail.submission" class="review-card app-card">
            <h4>批改结果</h4>
            <div class="soft-list">
              <div class="soft-item">
                <strong>当前状态</strong>
                <span>{{ submissionStatusText(assignmentDetail.submission.status) }} · 提交 {{ assignmentDetail.submission.submitCount }} 次</span>
              </div>
              <div class="soft-item">
                <strong>教师评分</strong>
                <span>{{ assignmentDetail.submission.score ?? '暂未评分' }}</span>
              </div>
              <div class="soft-item">
                <strong>教师评语</strong>
                <span>{{ assignmentDetail.submission.comment || '暂无评语' }}</span>
              </div>
            </div>
          </div>
        </template>
      </main>
    </section>
  </div>
</template>

<style scoped lang="scss">
.student-assignment-page { gap: 18px; }
.workspace { display: grid; grid-template-columns: 320px minmax(0, 1fr); gap: 18px; }
.block-head, .detail-head { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.dense-list { margin-top: 14px; }
.assignment-item { text-align: left; display: grid; gap: 8px; padding: 16px; border-radius: 16px; border: 1px solid var(--line-soft); background: rgba(255,255,255,.62); cursor: pointer; }
.assignment-item.active { border-color: rgba(161,100,47,.45); background: rgba(255,248,240,.95); }
.detail-head h3 { margin: 0; }
.assignment-item span, .detail-head p, .meta-row, .empty-state { color: var(--text-secondary); }
.meta-row { display: flex; flex-wrap: wrap; gap: 16px; margin: 12px 0 18px; }
.review-card { margin-top: 18px; padding: 18px; }
.review-card h4 { margin: 0 0 12px; }
.dialog-grid { display: grid; gap: 14px; }
.two-col { grid-template-columns: repeat(2, 1fr); }
@media (max-width: 1024px) { .workspace { grid-template-columns: 1fr; } }
@media (max-width: 760px) { .two-col { grid-template-columns: 1fr; } .detail-head { flex-direction: column; align-items: flex-start; } }
</style>
