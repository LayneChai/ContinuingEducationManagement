<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { auditAdminCourseApi, getAdminCourseDetailApi, getAdminCoursesApi } from '../../../api/admin'

const filters = reactive({
  keyword: '',
  auditStatus: 0,
  status: undefined,
})

const courses = ref([])
const activeCourseId = ref(null)
const courseDetail = ref(null)
const loading = ref(false)

const auditDialog = reactive({
  visible: false,
  saving: false,
  form: {
    auditStatus: 1,
    auditRemark: '',
  },
})

const metrics = computed(() => [
  { label: '课程总数', value: courses.value.length },
  { label: '待审核', value: courses.value.filter((item) => item.auditStatus === 0).length },
  { label: '已通过', value: courses.value.filter((item) => item.auditStatus === 1).length },
  { label: '已驳回', value: courses.value.filter((item) => item.auditStatus === 2).length },
])

onMounted(loadCourses)

async function loadCourses() {
  loading.value = true
  try {
    courses.value = await getAdminCoursesApi(filters)
    const targetId = activeCourseId.value || courses.value[0]?.id
    if (targetId) {
      await openCourse(targetId)
    } else {
      activeCourseId.value = null
      courseDetail.value = null
    }
  } finally {
    loading.value = false
  }
}

async function openCourse(courseId) {
  activeCourseId.value = courseId
  courseDetail.value = await getAdminCourseDetailApi(courseId)
}

function openAuditDialog(status) {
  auditDialog.visible = true
  auditDialog.form = {
    auditStatus: status,
    auditRemark: courseDetail.value?.auditRemark || '',
  }
}

async function submitAudit() {
  auditDialog.saving = true
  try {
    await auditAdminCourseApi(activeCourseId.value, auditDialog.form)
    ElMessage.success('课程审核完成')
    auditDialog.visible = false
    await loadCourses()
    await openCourse(activeCourseId.value)
  } catch (error) {
    ElMessage.error(error.message || '审核失败')
  } finally {
    auditDialog.saving = false
  }
}

function auditText(status) {
  return ['待审', '通过', '驳回'][status] || '未知'
}

function statusText(status) {
  return ['草稿', '上架', '下架'][status] || '未知'
}
</script>

<template>
  <div class="page-wrap admin-course-audit-page">
    <div class="page-heading">
      <div>
        <h1>课程审核</h1>
        <p>管理员在这里查看教师提交的课程内容，并根据章节与课时完整度决定是否通过。</p>
      </div>
    </div>

    <section class="stat-grid">
      <article v-for="item in metrics" :key="item.label" class="app-card stat-card">
        <div class="label">{{ item.label }}</div>
        <div class="value">{{ item.value }}</div>
      </article>
    </section>

    <section class="toolbar app-card">
      <el-input v-model="filters.keyword" placeholder="搜索课程标题或编码" clearable @keyup.enter="loadCourses" />
      <el-select v-model="filters.auditStatus" placeholder="审核状态" clearable>
        <el-option label="待审" :value="0" />
        <el-option label="通过" :value="1" />
        <el-option label="驳回" :value="2" />
      </el-select>
      <el-select v-model="filters.status" placeholder="课程状态" clearable>
        <el-option label="草稿" :value="0" />
        <el-option label="上架" :value="1" />
        <el-option label="下架" :value="2" />
      </el-select>
      <el-button @click="loadCourses">查询</el-button>
    </section>

    <section class="workspace">
      <aside class="app-card panel-block list-panel">
        <div class="block-head">
          <h3>待审课程</h3>
          <span>{{ courses.length }} 门</span>
        </div>
        <div class="soft-list dense-list">
          <button
            v-for="course in courses"
            :key="course.id"
            class="course-item"
            :class="{ active: course.id === activeCourseId }"
            @click="openCourse(course.id)"
          >
            <div class="item-top">
              <strong>{{ course.title }}</strong>
              <el-tag size="small" :type="course.auditStatus === 1 ? 'success' : course.auditStatus === 2 ? 'danger' : 'warning'">
                {{ auditText(course.auditStatus) }}
              </el-tag>
            </div>
            <span>{{ course.teacherName }} · {{ course.categoryName || '未分类' }}</span>
            <span>{{ statusText(course.status) }} · {{ course.totalLessons }} 课时</span>
          </button>
        </div>
      </aside>

      <main class="app-card panel-block detail-panel">
        <div v-if="!courseDetail" class="empty-state">请选择左侧课程查看审核详情。</div>
        <template v-else>
          <div class="block-head detail-head">
            <div>
              <h3>{{ courseDetail.title }}</h3>
              <p>{{ courseDetail.subtitle || courseDetail.description || '暂无课程介绍' }}</p>
            </div>
            <div class="top-actions">
              <el-button type="danger" plain @click="openAuditDialog(2)">驳回</el-button>
              <el-button type="primary" @click="openAuditDialog(1)">审核通过</el-button>
            </div>
          </div>

          <div class="meta-row">
            <span>教师 {{ courseDetail.teacherName }}</span>
            <span>分类 {{ courseDetail.categoryName || '未分类' }}</span>
            <span>要求时长 {{ courseDetail.requiredHours }} 小时</span>
            <span>课程状态 {{ statusText(courseDetail.status) }}</span>
            <span>审核状态 {{ auditText(courseDetail.auditStatus) }}</span>
          </div>

          <div class="summary-card app-card">
            <h4>课程简介</h4>
            <p>{{ courseDetail.description || '暂无课程简介' }}</p>
            <p class="remark">历史审核意见：{{ courseDetail.auditRemark || '暂无' }}</p>
          </div>

          <div class="chapter-list">
            <section v-for="chapter in courseDetail.chapters || []" :key="chapter.id" class="soft-item chapter-card">
              <strong>{{ chapter.sort }}. {{ chapter.title }}</strong>
              <span>{{ chapter.description || '暂无章节说明' }}</span>
              <div class="lesson-list">
                <div v-for="lesson in chapter.lessons || []" :key="lesson.id" class="lesson-item">
                  <strong>{{ lesson.title }}</strong>
                  <span>{{ ['视频', '文档', '图文'][lesson.lessonType - 1] || '未知类型' }} · {{ Math.round(Number(lesson.durationSeconds || 0) / 60) }} 分钟</span>
                </div>
              </div>
            </section>
          </div>
        </template>
      </main>
    </section>

    <el-dialog v-model="auditDialog.visible" title="课程审核" width="560px">
      <el-form label-position="top">
        <el-form-item label="审核结果">
          <el-radio-group v-model="auditDialog.form.auditStatus">
            <el-radio :value="1">通过</el-radio>
            <el-radio :value="2">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见">
          <el-input v-model="auditDialog.form.auditRemark" type="textarea" :rows="4" placeholder="请填写审核结论或驳回原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="auditDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="auditDialog.saving" @click="submitAudit">提交审核</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.admin-course-audit-page { gap: 18px; }
.toolbar {
  display: grid;
  grid-template-columns: 1fr 180px 180px 100px;
  gap: 12px;
  padding: 16px;
}
.workspace { display: grid; grid-template-columns: 340px minmax(0, 1fr); gap: 18px; }
.block-head, .item-top, .detail-head, .top-actions { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.dense-list, .chapter-list, .lesson-list { display: grid; gap: 12px; }
.dense-list { margin-top: 14px; }
.course-item {
  text-align: left; border: 1px solid var(--line-soft); background: rgba(255,255,255,.62); border-radius: 16px; padding: 16px; cursor: pointer; display: grid; gap: 8px;
}
.course-item.active { border-color: rgba(161,100,47,.45); background: rgba(255,248,240,.95); }
.meta-row, .course-item span, .detail-head p, .summary-card p, .lesson-item span, .empty-state { color: var(--text-secondary); }
.meta-row { display: flex; flex-wrap: wrap; gap: 16px; margin: 12px 0 18px; }
.summary-card { padding: 18px; margin-bottom: 18px; }
.summary-card h4 { margin: 0 0 10px; }
.remark { margin-top: 10px; }
.chapter-card { gap: 10px; }
.lesson-item {
  padding: 12px 14px;
  border-radius: 12px;
  background: rgba(255,255,255,.7);
  border: 1px solid var(--line-soft);
}
@media (max-width: 1024px) { .workspace { grid-template-columns: 1fr; } }
@media (max-width: 760px) {
  .toolbar { grid-template-columns: 1fr; }
  .detail-head, .top-actions { flex-direction: column; align-items: flex-start; }
}
</style>
