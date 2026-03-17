<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { getTeacherCoursesApi } from '../../../api/course'
import {
  createTeacherAssignmentApi,
  getTeacherAssignmentDetailApi,
  getTeacherAssignmentsApi,
  getTeacherAssignmentSubmissionsApi,
  publishTeacherAssignmentApi,
  reviewTeacherSubmissionApi,
  updateTeacherAssignmentApi,
} from '../../../api/assignment'

const courses = ref([])
const selectedCourseId = ref(null)
const assignments = ref([])
const activeAssignmentId = ref(null)
const assignmentDetail = ref(null)
const submissions = ref([])
const loading = ref(false)

const assignmentDialog = reactive({
  visible: false,
  isEdit: false,
  saving: false,
  assignmentId: null,
  form: defaultAssignmentForm(),
})

const reviewDialog = reactive({
  visible: false,
  saving: false,
  submissionId: null,
  studentName: '',
  form: {
    score: 90,
    comment: '',
    aiComment: '',
  },
})

const metrics = computed(() => [
  { label: '作业总数', value: assignments.value.length },
  { label: '已发布', value: assignments.value.filter((item) => item.status === 1).length },
  { label: '提交数量', value: submissions.value.length },
  { label: '已批改', value: submissions.value.filter((item) => item.status === 2).length },
])

onMounted(async () => {
  await loadCourses()
})

function defaultAssignmentForm() {
  return {
    courseId: null,
    title: '',
    description: '',
    totalScore: 100,
    allowResubmit: 0,
    deadline: '',
  }
}

async function loadCourses() {
  courses.value = await getTeacherCoursesApi({ status: 1 })
  selectedCourseId.value = courses.value[0]?.id || null
  if (selectedCourseId.value) {
    await loadAssignments()
  }
}

async function loadAssignments() {
  if (!selectedCourseId.value) return
  loading.value = true
  try {
    assignments.value = await getTeacherAssignmentsApi({ courseId: selectedCourseId.value })
    const targetId = activeAssignmentId.value || assignments.value[0]?.id
    if (targetId) {
      await openAssignment(targetId)
    } else {
      activeAssignmentId.value = null
      assignmentDetail.value = null
      submissions.value = []
    }
  } finally {
    loading.value = false
  }
}

async function openAssignment(assignmentId) {
  activeAssignmentId.value = assignmentId
  const [detail, submissionList] = await Promise.all([
    getTeacherAssignmentDetailApi(assignmentId),
    getTeacherAssignmentSubmissionsApi(assignmentId),
  ])
  assignmentDetail.value = detail
  submissions.value = submissionList
}

function openCreateDialog() {
  assignmentDialog.visible = true
  assignmentDialog.isEdit = false
  assignmentDialog.assignmentId = null
  assignmentDialog.form = { ...defaultAssignmentForm(), courseId: selectedCourseId.value }
}

function openEditDialog() {
  if (!assignmentDetail.value) return
  assignmentDialog.visible = true
  assignmentDialog.isEdit = true
  assignmentDialog.assignmentId = assignmentDetail.value.id
  assignmentDialog.form = {
    courseId: assignmentDetail.value.courseId,
    title: assignmentDetail.value.title,
    description: assignmentDetail.value.description,
    totalScore: Number(assignmentDetail.value.totalScore || 100),
    allowResubmit: assignmentDetail.value.allowResubmit,
    deadline: assignmentDetail.value.deadline,
  }
}

async function submitAssignment() {
  assignmentDialog.saving = true
  try {
    if (assignmentDialog.isEdit) {
      await updateTeacherAssignmentApi(assignmentDialog.assignmentId, assignmentDialog.form)
      ElMessage.success('作业已更新')
    } else {
      await createTeacherAssignmentApi(assignmentDialog.form)
      ElMessage.success('作业已创建')
    }
    assignmentDialog.visible = false
    await loadAssignments()
  } catch (error) {
    ElMessage.error(error.message || '保存作业失败')
  } finally {
    assignmentDialog.saving = false
  }
}

async function publishAssignment(assignment) {
  try {
    await publishTeacherAssignmentApi(assignment.id)
    ElMessage.success('作业已发布')
    await openAssignment(assignment.id)
    await loadAssignments()
  } catch (error) {
    ElMessage.error(error.message || '发布失败')
  }
}

function openReviewDialog(submission) {
  reviewDialog.visible = true
  reviewDialog.submissionId = submission.submissionId
  reviewDialog.studentName = submission.studentName
  reviewDialog.form = {
    score: submission.score ?? Number(assignmentDetail.value?.totalScore || 100),
    comment: submission.comment || '',
    aiComment: submission.aiComment || '',
  }
}

async function submitReview() {
  reviewDialog.saving = true
  try {
    await reviewTeacherSubmissionApi(reviewDialog.submissionId, reviewDialog.form)
    ElMessage.success('批改完成')
    reviewDialog.visible = false
    await openAssignment(activeAssignmentId.value)
  } catch (error) {
    ElMessage.error(error.message || '批改失败')
  } finally {
    reviewDialog.saving = false
  }
}

function assignmentStatusText(status) {
  return ['草稿', '已发布', '已结束'][status] || '未知'
}

function submissionStatusText(status) {
  return ['未提交', '已提交', '已批改'][status] || '未知'
}
</script>

<template>
  <div class="page-wrap assignment-page">
    <div class="page-heading">
      <div>
        <h1>作业管理</h1>
        <p>按课程查看作业、发布作业并集中批改学生提交。</p>
      </div>
      <div class="top-actions">
        <el-select v-model="selectedCourseId" placeholder="选择课程" style="width: 260px" @change="loadAssignments">
          <el-option v-for="course in courses" :key="course.id" :label="course.title" :value="course.id" />
        </el-select>
        <el-button type="primary" @click="openCreateDialog">新增作业</el-button>
      </div>
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
            <div class="item-top">
              <strong>{{ item.title }}</strong>
              <el-tag size="small" :type="item.status === 1 ? 'success' : 'info'">{{ assignmentStatusText(item.status) }}</el-tag>
            </div>
            <span>总分 {{ item.totalScore }} · 截止 {{ item.deadline || '未设置' }}</span>
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
            <div class="top-actions">
              <el-button @click="openEditDialog">编辑作业</el-button>
              <el-button type="primary" plain @click="publishAssignment(assignmentDetail)">发布作业</el-button>
            </div>
          </div>
          <div class="meta-row">
            <span>总分 {{ assignmentDetail.totalScore }}</span>
            <span>重复提交 {{ assignmentDetail.allowResubmit === 1 ? '允许' : '不允许' }}</span>
            <span>截止时间 {{ assignmentDetail.deadline || '未设置' }}</span>
            <span>状态 {{ assignmentStatusText(assignmentDetail.status) }}</span>
          </div>

          <div class="submission-list">
            <div v-for="item in submissions" :key="item.submissionId" class="soft-item submission-item">
              <div>
                <strong>{{ item.studentName }}</strong>
                <span>{{ submissionStatusText(item.status) }} · 提交 {{ item.submitCount }} 次</span>
                <span>{{ item.submittedTime || '尚未提交' }}</span>
              </div>
              <div class="submission-side">
                <span>当前评分：{{ item.score ?? '未评分' }}</span>
                <el-button type="primary" plain @click="openReviewDialog(item)">批改</el-button>
              </div>
            </div>
            <div v-if="!submissions.length" class="empty-state">当前还没有学生提交作业。</div>
          </div>
        </template>
      </main>
    </section>

    <el-dialog v-model="assignmentDialog.visible" :title="assignmentDialog.isEdit ? '编辑作业' : '新增作业'" width="720px">
      <el-form label-position="top">
        <div class="dialog-grid two-col">
          <el-form-item label="作业标题"><el-input v-model="assignmentDialog.form.title" /></el-form-item>
          <el-form-item label="总分"><el-input-number v-model="assignmentDialog.form.totalScore" :min="1" /></el-form-item>
        </div>
        <div class="dialog-grid two-col">
          <el-form-item label="截止时间">
            <el-date-picker v-model="assignmentDialog.form.deadline" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
          </el-form-item>
          <el-form-item label="允许重复提交"><el-switch v-model="assignmentDialog.form.allowResubmit" :active-value="1" :inactive-value="0" /></el-form-item>
        </div>
        <el-form-item label="作业说明"><el-input v-model="assignmentDialog.form.description" type="textarea" :rows="5" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignmentDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="assignmentDialog.saving" @click="submitAssignment">保存作业</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reviewDialog.visible" :title="`批改：${reviewDialog.studentName}`" width="620px">
      <el-form label-position="top">
        <el-form-item label="评分"><el-input-number v-model="reviewDialog.form.score" :min="0" :max="Number(assignmentDetail?.totalScore || 100)" /></el-form-item>
        <el-form-item label="教师评语"><el-input v-model="reviewDialog.form.comment" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="AI 建议评语"><el-input v-model="reviewDialog.form.aiComment" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="reviewDialog.saving" @click="submitReview">提交批改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.assignment-page { gap: 18px; }
.top-actions, .block-head, .item-top, .detail-head, .submission-item { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.workspace { display: grid; grid-template-columns: 360px minmax(0, 1fr); gap: 18px; }
.dense-list, .submission-list { margin-top: 14px; display: grid; gap: 12px; }
.assignment-item {
  text-align: left; border: 1px solid var(--line-soft); background: rgba(255,255,255,.62); border-radius: 16px; padding: 16px; cursor: pointer; display: grid; gap: 8px;
}
.assignment-item.active { border-color: rgba(161,100,47,.45); background: rgba(255,248,240,.95); }
.detail-head h3 { margin: 0; }
.detail-head p, .assignment-item span, .meta-row, .submission-item span, .empty-state { color: var(--text-secondary); }
.meta-row { display: flex; flex-wrap: wrap; gap: 16px; margin: 12px 0 18px; }
.submission-side { display: grid; justify-items: end; gap: 8px; }
.dialog-grid { display: grid; gap: 14px; }
.two-col { grid-template-columns: repeat(2, 1fr); }
@media (max-width: 1024px) { .workspace { grid-template-columns: 1fr; } }
@media (max-width: 760px) { .two-col { grid-template-columns: 1fr; } .top-actions, .detail-head, .submission-item { align-items: flex-start; flex-direction: column; } }
</style>
