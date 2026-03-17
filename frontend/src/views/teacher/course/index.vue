<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import {
  createChapterApi,
  createLessonApi,
  createTeacherCourseApi,
  deleteChapterApi,
  deleteLessonApi,
  getCourseCategoriesApi,
  getTeacherCourseDetailApi,
  getTeacherCoursesApi,
  submitTeacherCourseAuditApi,
  updateChapterApi,
  updateLessonApi,
  updateTeacherCourseApi,
} from '../../../api/course'

const loading = ref(false)
const categories = ref([])
const courses = ref([])
const activeCourseId = ref(null)
const courseDetail = ref(null)
const detailLoading = ref(false)

const filters = reactive({
  keyword: '',
  auditStatus: undefined,
  status: undefined,
})

const courseDialog = reactive({
  visible: false,
  isEdit: false,
  saving: false,
  form: defaultCourseForm(),
})

const chapterDialog = reactive({
  visible: false,
  isEdit: false,
  saving: false,
  chapterId: null,
  form: { title: '', sort: 0, description: '' },
})

const lessonDialog = reactive({
  visible: false,
  isEdit: false,
  saving: false,
  lessonId: null,
  form: defaultLessonForm(),
})

const metrics = computed(() => {
  const list = courses.value
  return [
    { label: '课程总数', value: list.length },
    { label: '待审课程', value: list.filter((item) => item.auditStatus === 0).length },
    { label: '已上架', value: list.filter((item) => item.status === 1).length },
    { label: '课时总量', value: list.reduce((sum, item) => sum + (item.totalLessons || 0), 0) },
  ]
})

const chapterOptions = computed(() => (courseDetail.value?.chapters || []).map((item) => ({
  label: item.title,
  value: item.id,
})))

onMounted(async () => {
  await Promise.all([loadCategories(), loadCourses()])
})

function defaultCourseForm() {
  return {
    title: '',
    subtitle: '',
    coverUrl: '',
    categoryId: null,
    description: '',
    targetUser: '',
    requiredHours: 12,
    examRequired: 0,
    assignmentRequired: 0,
    certificateEnabled: 1,
  }
}

function defaultLessonForm() {
  return {
    chapterId: null,
    title: '',
    lessonType: 1,
    resourceUrl: '',
    content: '',
    durationSeconds: 900,
    previewEnabled: 0,
    sort: 0,
    status: 1,
  }
}

async function loadCategories() {
  categories.value = await getCourseCategoriesApi()
}

async function loadCourses(selectId) {
  loading.value = true
  try {
    courses.value = await getTeacherCoursesApi(filters)
    const targetId = selectId || activeCourseId.value || courses.value[0]?.id
    if (targetId) {
      await loadCourseDetail(targetId)
    } else {
      activeCourseId.value = null
      courseDetail.value = null
    }
  } finally {
    loading.value = false
  }
}

async function loadCourseDetail(courseId) {
  activeCourseId.value = courseId
  detailLoading.value = true
  try {
    courseDetail.value = await getTeacherCourseDetailApi(courseId)
  } finally {
    detailLoading.value = false
  }
}

function openCreateCourse() {
  courseDialog.visible = true
  courseDialog.isEdit = false
  courseDialog.form = defaultCourseForm()
}

function openEditCourse() {
  if (!courseDetail.value) return
  courseDialog.visible = true
  courseDialog.isEdit = true
  courseDialog.form = {
    title: courseDetail.value.title,
    subtitle: courseDetail.value.subtitle,
    coverUrl: courseDetail.value.coverUrl,
    categoryId: courseDetail.value.categoryId,
    description: courseDetail.value.description,
    targetUser: courseDetail.value.targetUser,
    requiredHours: Number(courseDetail.value.requiredHours || 0),
    examRequired: courseDetail.value.examRequired,
    assignmentRequired: courseDetail.value.assignmentRequired,
    certificateEnabled: courseDetail.value.certificateEnabled,
  }
}

async function submitCourse() {
  courseDialog.saving = true
  try {
    if (courseDialog.isEdit && activeCourseId.value) {
      await updateTeacherCourseApi(activeCourseId.value, courseDialog.form)
      ElMessage.success('课程已更新')
      await loadCourses(activeCourseId.value)
    } else {
      const result = await createTeacherCourseApi(courseDialog.form)
      ElMessage.success('课程已创建')
      await loadCourses(result.courseId)
    }
    courseDialog.visible = false
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    courseDialog.saving = false
  }
}

async function submitAudit() {
  if (!activeCourseId.value) return
  try {
    await submitTeacherCourseAuditApi(activeCourseId.value)
    ElMessage.success('已提交审核')
    await Promise.all([loadCourses(activeCourseId.value), loadCourseDetail(activeCourseId.value)])
  } catch (error) {
    ElMessage.error(error.message || '提交失败')
  }
}

function openCreateChapter() {
  chapterDialog.visible = true
  chapterDialog.isEdit = false
  chapterDialog.chapterId = null
  chapterDialog.form = { title: '', sort: (courseDetail.value?.chapters?.length || 0) + 1, description: '' }
}

function openEditChapter(chapter) {
  chapterDialog.visible = true
  chapterDialog.isEdit = true
  chapterDialog.chapterId = chapter.id
  chapterDialog.form = { title: chapter.title, sort: chapter.sort, description: chapter.description }
}

async function submitChapter() {
  chapterDialog.saving = true
  try {
    if (chapterDialog.isEdit) {
      await updateChapterApi(activeCourseId.value, chapterDialog.chapterId, chapterDialog.form)
      ElMessage.success('章节已更新')
    } else {
      await createChapterApi(activeCourseId.value, chapterDialog.form)
      ElMessage.success('章节已新增')
    }
    chapterDialog.visible = false
    await Promise.all([loadCourseDetail(activeCourseId.value), loadCourses(activeCourseId.value)])
  } catch (error) {
    ElMessage.error(error.message || '保存章节失败')
  } finally {
    chapterDialog.saving = false
  }
}

async function removeChapter(chapter) {
  try {
    await ElMessageBox.confirm(`确定删除章节「${chapter.title}」及其课时吗？`, '删除确认', { type: 'warning' })
    await deleteChapterApi(activeCourseId.value, chapter.id)
    ElMessage.success('章节已删除')
    await Promise.all([loadCourseDetail(activeCourseId.value), loadCourses(activeCourseId.value)])
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

function openCreateLesson(chapterId = null) {
  lessonDialog.visible = true
  lessonDialog.isEdit = false
  lessonDialog.lessonId = null
  lessonDialog.form = { ...defaultLessonForm(), chapterId: chapterId || chapterOptions.value[0]?.value || null }
}

function openEditLesson(lesson) {
  lessonDialog.visible = true
  lessonDialog.isEdit = true
  lessonDialog.lessonId = lesson.id
  lessonDialog.form = {
    chapterId: lesson.chapterId,
    title: lesson.title,
    lessonType: lesson.lessonType,
    resourceUrl: lesson.resourceUrl,
    content: lesson.content,
    durationSeconds: lesson.durationSeconds,
    previewEnabled: lesson.previewEnabled,
    sort: lesson.sort,
    status: lesson.status,
  }
}

async function submitLesson() {
  lessonDialog.saving = true
  try {
    if (lessonDialog.isEdit) {
      await updateLessonApi(activeCourseId.value, lessonDialog.lessonId, lessonDialog.form)
      ElMessage.success('课时已更新')
    } else {
      await createLessonApi(activeCourseId.value, lessonDialog.form)
      ElMessage.success('课时已新增')
    }
    lessonDialog.visible = false
    await Promise.all([loadCourseDetail(activeCourseId.value), loadCourses(activeCourseId.value)])
  } catch (error) {
    ElMessage.error(error.message || '保存课时失败')
  } finally {
    lessonDialog.saving = false
  }
}

async function removeLesson(lesson) {
  try {
    await ElMessageBox.confirm(`确定删除课时「${lesson.title}」吗？`, '删除确认', { type: 'warning' })
    await deleteLessonApi(activeCourseId.value, lesson.id)
    ElMessage.success('课时已删除')
    await Promise.all([loadCourseDetail(activeCourseId.value), loadCourses(activeCourseId.value)])
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
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
  <div class="page-wrap teacher-course-page">
    <div class="page-heading">
      <div>
        <h1>课程管理</h1>
        <p>围绕教师课程、章节和课时进行维护，并直接提交管理员审核。</p>
      </div>
      <el-button type="primary" @click="openCreateCourse">新建课程</el-button>
    </div>

    <section class="stat-grid">
      <article v-for="item in metrics" :key="item.label" class="app-card stat-card">
        <div class="label">{{ item.label }}</div>
        <div class="value">{{ item.value }}</div>
      </article>
    </section>

    <section class="toolbar app-card">
      <el-input v-model="filters.keyword" placeholder="搜索课程标题或编码" clearable @keyup.enter="loadCourses()" />
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
      <el-button @click="loadCourses()">查询</el-button>
    </section>

    <section class="workspace">
      <aside class="app-card course-list-panel">
        <div class="panel-head">
          <h3>课程列表</h3>
          <span>{{ courses.length }} 门</span>
        </div>
        <el-skeleton v-if="loading" :rows="6" animated />
        <div v-else class="course-list">
          <button
            v-for="course in courses"
            :key="course.id"
            class="course-item"
            :class="{ active: course.id === activeCourseId }"
            @click="loadCourseDetail(course.id)"
          >
            <div class="course-item-top">
              <strong>{{ course.title }}</strong>
              <el-tag size="small" :type="course.auditStatus === 1 ? 'success' : course.auditStatus === 2 ? 'danger' : 'warning'">
                {{ auditText(course.auditStatus) }}
              </el-tag>
            </div>
            <span>{{ course.categoryName || '未分类' }} · {{ statusText(course.status) }}</span>
            <span>要求学时 {{ course.requiredHours }} / 课时 {{ course.totalLessons }}</span>
          </button>
        </div>
      </aside>

      <main class="app-card detail-panel">
        <div v-if="!courseDetail" class="empty-state">请先从左侧选择课程，或新建一门课程。</div>
        <template v-else>
          <div class="detail-head">
            <div>
              <h2>{{ courseDetail.title }}</h2>
              <p>{{ courseDetail.subtitle || '暂无副标题' }}</p>
            </div>
            <div class="detail-actions">
              <el-button @click="openEditCourse">编辑课程</el-button>
              <el-button type="primary" plain @click="submitAudit">提交审核</el-button>
            </div>
          </div>

          <div class="meta-grid">
            <div><label>审核状态</label><span>{{ auditText(courseDetail.auditStatus) }}</span></div>
            <div><label>课程状态</label><span>{{ statusText(courseDetail.status) }}</span></div>
            <div><label>适用对象</label><span>{{ courseDetail.targetUser || '未填写' }}</span></div>
            <div><label>要求学时</label><span>{{ courseDetail.requiredHours }}</span></div>
          </div>

          <div class="summary-card app-card inner-card">
            <h3>课程简介</h3>
            <p>{{ courseDetail.description || '暂无课程简介' }}</p>
            <p v-if="courseDetail.auditRemark" class="audit-remark">审核意见：{{ courseDetail.auditRemark }}</p>
          </div>

          <div class="chapter-head">
            <h3>章节与课时</h3>
            <div class="detail-actions">
              <el-button @click="openCreateChapter">新增章节</el-button>
              <el-button type="primary" @click="openCreateLesson()">新增课时</el-button>
            </div>
          </div>

          <el-skeleton v-if="detailLoading" :rows="8" animated />
          <div v-else class="chapter-list">
            <section v-for="chapter in courseDetail.chapters || []" :key="chapter.id" class="chapter-card app-card inner-card">
              <div class="chapter-top">
                <div>
                  <h4>{{ chapter.sort }}. {{ chapter.title }}</h4>
                  <p>{{ chapter.description || '暂无章节说明' }}</p>
                </div>
                <div class="detail-actions">
                  <el-button text @click="openCreateLesson(chapter.id)">加课时</el-button>
                  <el-button text @click="openEditChapter(chapter)">编辑</el-button>
                  <el-button text type="danger" @click="removeChapter(chapter)">删除</el-button>
                </div>
              </div>

              <div class="lesson-list">
                <article v-for="lesson in chapter.lessons || []" :key="lesson.id" class="lesson-item">
                  <div>
                    <strong>{{ lesson.title }}</strong>
                    <span>
                      {{ ['视频', '文档', '图文'][lesson.lessonType - 1] || '未知类型' }} ·
                      {{ lesson.durationSeconds || 0 }} 秒
                    </span>
                  </div>
                  <div class="detail-actions">
                    <el-button text @click="openEditLesson(lesson)">编辑</el-button>
                    <el-button text type="danger" @click="removeLesson(lesson)">删除</el-button>
                  </div>
                </article>
                <div v-if="!(chapter.lessons || []).length" class="lesson-empty">当前章节还没有课时</div>
              </div>
            </section>
          </div>
        </template>
      </main>
    </section>

    <el-dialog v-model="courseDialog.visible" :title="courseDialog.isEdit ? '编辑课程' : '新建课程'" width="720px">
      <el-form label-position="top">
        <div class="dialog-grid two-col">
          <el-form-item label="课程标题"><el-input v-model="courseDialog.form.title" /></el-form-item>
          <el-form-item label="课程分类">
            <el-select v-model="courseDialog.form.categoryId" clearable>
              <el-option v-for="item in categories" :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
        </div>
        <div class="dialog-grid two-col">
          <el-form-item label="课程副标题"><el-input v-model="courseDialog.form.subtitle" /></el-form-item>
          <el-form-item label="封面地址"><el-input v-model="courseDialog.form.coverUrl" /></el-form-item>
        </div>
        <div class="dialog-grid three-col">
          <el-form-item label="要求学时"><el-input-number v-model="courseDialog.form.requiredHours" :min="1" /></el-form-item>
          <el-form-item label="考试要求"><el-switch v-model="courseDialog.form.examRequired" :active-value="1" :inactive-value="0" /></el-form-item>
          <el-form-item label="作业要求"><el-switch v-model="courseDialog.form.assignmentRequired" :active-value="1" :inactive-value="0" /></el-form-item>
        </div>
        <el-form-item label="适用对象"><el-input v-model="courseDialog.form.targetUser" /></el-form-item>
        <el-form-item label="课程简介"><el-input v-model="courseDialog.form.description" type="textarea" :rows="5" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="courseDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="courseDialog.saving" @click="submitCourse">保存课程</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="chapterDialog.visible" :title="chapterDialog.isEdit ? '编辑章节' : '新增章节'" width="520px">
      <el-form label-position="top">
        <el-form-item label="章节标题"><el-input v-model="chapterDialog.form.title" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="chapterDialog.form.sort" :min="0" /></el-form-item>
        <el-form-item label="章节说明"><el-input v-model="chapterDialog.form.description" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="chapterDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="chapterDialog.saving" @click="submitChapter">保存章节</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="lessonDialog.visible" :title="lessonDialog.isEdit ? '编辑课时' : '新增课时'" width="620px">
      <el-form label-position="top">
        <div class="dialog-grid two-col">
          <el-form-item label="所属章节">
            <el-select v-model="lessonDialog.form.chapterId">
              <el-option v-for="item in chapterOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="课时类型">
            <el-select v-model="lessonDialog.form.lessonType">
              <el-option label="视频" :value="1" />
              <el-option label="文档" :value="2" />
              <el-option label="图文" :value="3" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item label="课时标题"><el-input v-model="lessonDialog.form.title" /></el-form-item>
        <el-form-item label="资源地址"><el-input v-model="lessonDialog.form.resourceUrl" /></el-form-item>
        <el-form-item label="图文内容"><el-input v-model="lessonDialog.form.content" type="textarea" :rows="4" /></el-form-item>
        <div class="dialog-grid three-col">
          <el-form-item label="时长(秒)"><el-input-number v-model="lessonDialog.form.durationSeconds" :min="0" /></el-form-item>
          <el-form-item label="排序"><el-input-number v-model="lessonDialog.form.sort" :min="0" /></el-form-item>
          <el-form-item label="状态"><el-switch v-model="lessonDialog.form.status" :active-value="1" :inactive-value="0" /></el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="lessonDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="lessonDialog.saving" @click="submitLesson">保存课时</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.teacher-course-page { gap: 18px; }
.toolbar {
  display: grid;
  grid-template-columns: 1.2fr repeat(2, 160px) 100px;
  gap: 12px;
  padding: 16px;
}
.workspace {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 18px;
}
.course-list-panel,
.detail-panel { padding: 20px; }
.panel-head,
.detail-head,
.chapter-head,
.course-item-top,
.chapter-top,
.detail-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}
.panel-head h3,
.chapter-head h3,
.detail-head h2,
.chapter-top h4 { margin: 0; }
.course-list { display: grid; gap: 10px; margin-top: 14px; }
.course-item {
  text-align: left;
  border: 1px solid var(--line-soft);
  background: rgba(255,255,255,.6);
  border-radius: 16px;
  padding: 16px;
  cursor: pointer;
  display: grid;
  gap: 8px;
}
.course-item.active { border-color: rgba(161,100,47,.45); background: rgba(255,248,240,.95); }
.course-item span,
.detail-head p,
.chapter-top p,
.lesson-item span,
.audit-remark,
.lesson-empty,
.meta-grid label { color: var(--text-secondary); }
.detail-head { margin-bottom: 16px; }
.meta-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}
.meta-grid > div,
.inner-card {
  background: rgba(255,255,255,.56);
  border: 1px solid var(--line-soft);
  border-radius: 16px;
}
.meta-grid > div { padding: 14px; display: grid; gap: 6px; }
.inner-card { padding: 18px; }
.chapter-list { display: grid; gap: 14px; }
.lesson-list { display: grid; gap: 10px; margin-top: 12px; }
.lesson-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(255,255,255,.75);
  border: 1px solid var(--line-soft);
}
.empty-state { color: var(--text-secondary); padding: 32px 12px; }
.dialog-grid { display: grid; gap: 14px; }
.two-col { grid-template-columns: repeat(2, 1fr); }
.three-col { grid-template-columns: repeat(3, 1fr); }

@media (max-width: 1100px) {
  .workspace { grid-template-columns: 1fr; }
  .meta-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 760px) {
  .toolbar, .two-col, .three-col, .meta-grid { grid-template-columns: 1fr; }
  .lesson-item, .detail-head, .chapter-head, .chapter-top { align-items: flex-start; flex-direction: column; }
}
</style>
