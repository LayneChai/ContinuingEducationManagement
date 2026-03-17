<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'

import {
  enrollCourseApi,
  getLearningOverviewApi,
  getPublicCourseDetailApi,
  getPublicCoursesApi,
  getStudentCoursesApi,
  studyLessonApi,
} from '../../../api/course'

const loading = ref(false)
const tab = ref('discover')
const publicCourses = ref([])
const myCourses = ref([])
const detail = ref(null)
const overview = ref(null)
const activeCourseId = ref(null)
const detailLoading = ref(false)

const discoverMetrics = computed(() => [
  { label: '公开课程', value: publicCourses.value.length },
  { label: '我的课程', value: myCourses.value.length },
  { label: '学习中', value: myCourses.value.filter((item) => item.enrollmentStatus === 1).length },
  { label: '已完成', value: myCourses.value.filter((item) => item.enrollmentStatus === 2).length },
])

onMounted(async () => {
  await Promise.all([loadPublicCourses(), loadMyCourses()])
  const firstCourseId = publicCourses.value[0]?.id || myCourses.value[0]?.id
  if (firstCourseId) {
    await openCourse(firstCourseId)
  }
})

async function loadPublicCourses() {
  loading.value = true
  try {
    publicCourses.value = await getPublicCoursesApi()
  } finally {
    loading.value = false
  }
}

async function loadMyCourses() {
  myCourses.value = await getStudentCoursesApi()
}

async function openCourse(courseId) {
  activeCourseId.value = courseId
  detailLoading.value = true
  try {
    detail.value = await getPublicCourseDetailApi(courseId)
    if (detail.value.enrolled) {
      overview.value = await getLearningOverviewApi(courseId)
    } else {
      overview.value = null
    }
  } catch (error) {
    ElMessage.error(error.message || '加载课程失败')
  } finally {
    detailLoading.value = false
  }
}

async function enrollCurrentCourse() {
  if (!detail.value) return
  try {
    await enrollCourseApi(detail.value.id)
    ElMessage.success('报名成功，已加入我的课程')
    await Promise.all([loadPublicCourses(), loadMyCourses(), openCourse(detail.value.id)])
    tab.value = 'mine'
  } catch (error) {
    ElMessage.error(error.message || '报名失败')
  }
}

async function studyLesson(lesson) {
  if (!detail.value?.enrolled) {
    ElMessage.warning('请先报名课程')
    return
  }
  try {
    const nextProgress = Math.min(100, Number(lesson.learningProgress || 0) + 25)
    await studyLessonApi(detail.value.id, lesson.id, {
      studySeconds: Math.max(Number(lesson.durationSeconds || 0), 600),
      progressPercent: nextProgress,
      lastPosition: Math.max(Number(lesson.durationSeconds || 0), 600),
      completed: nextProgress >= 100 ? 1 : 0,
    })
    ElMessage.success(nextProgress >= 100 ? '课时已完成' : '学习进度已更新')
    await Promise.all([loadMyCourses(), openCourse(detail.value.id)])
  } catch (error) {
    ElMessage.error(error.message || '学习进度更新失败')
  }
}

function courseStatusText(status) {
  return ['已报名', '学习中', '已完成', '已取消'][status] || '未报名'
}

function lessonTypeText(type) {
  return ['视频', '文档', '图文'][type - 1] || '未知类型'
}
</script>

<template>
  <div class="page-wrap student-course-page">
    <div class="page-heading">
      <div>
        <h1>课程学习</h1>
        <p>浏览公开课程、加入我的学习计划，并记录每个课时的学习进度和学时完成情况。</p>
      </div>
    </div>

    <section class="stat-grid">
      <article v-for="item in discoverMetrics" :key="item.label" class="app-card stat-card">
        <div class="label">{{ item.label }}</div>
        <div class="value">{{ item.value }}</div>
      </article>
    </section>

    <section class="workspace">
      <aside class="app-card list-panel">
        <el-segmented v-model="tab" :options="[{ label: '公开课程', value: 'discover' }, { label: '我的课程', value: 'mine' }]" block />
        <div class="course-stack">
          <button
            v-for="course in (tab === 'discover' ? publicCourses : myCourses)"
            :key="course.id"
            class="course-card"
            :class="{ active: course.id === activeCourseId }"
            @click="openCourse(course.id)"
          >
            <strong>{{ course.title }}</strong>
            <span>{{ course.categoryName || '未分类' }} · {{ course.requiredHours }} 学时</span>
            <span v-if="course.enrolled">{{ courseStatusText(course.enrollmentStatus) }} · 进度 {{ course.learningProgress || 0 }}%</span>
            <span v-else>可报名学习</span>
          </button>
        </div>
      </aside>

      <main class="app-card detail-panel">
        <div v-if="!detail" class="empty-state">请选择一门课程开始查看。</div>
        <template v-else>
          <div class="detail-head">
            <div>
              <h2>{{ detail.title }}</h2>
              <p>{{ detail.subtitle || detail.description || '暂无课程介绍' }}</p>
            </div>
            <div class="detail-actions">
              <el-tag :type="detail.enrolled ? 'success' : 'warning'">
                {{ detail.enrolled ? courseStatusText(detail.enrollmentStatus) : '未报名' }}
              </el-tag>
              <el-button v-if="!detail.enrolled" type="primary" @click="enrollCurrentCourse">立即报名</el-button>
            </div>
          </div>

          <div class="meta-grid">
            <div><label>教师</label><span>{{ detail.teacherName || '未分配' }}</span></div>
            <div><label>要求学时</label><span>{{ detail.requiredHours }}</span></div>
            <div><label>已完成学时</label><span>{{ overview?.completedHours || detail.completedHours || 0 }}</span></div>
            <div><label>完成率</label><span>{{ overview?.completionRate || detail.learningProgress || 0 }}%</span></div>
          </div>

          <div v-if="detail.enrolled" class="overview-card app-card inner-card">
            <h3>学习总览</h3>
            <div class="overview-metrics">
              <div>
                <strong>{{ overview?.completedLessons || detail.completedLessons || 0 }}</strong>
                <span>已完成课时</span>
              </div>
              <div>
                <strong>{{ overview?.totalLessons || detail.totalLessons || 0 }}</strong>
                <span>总课时</span>
              </div>
              <div>
                <strong>{{ overview?.qualified === 1 || detail.qualified === 1 ? '已达标' : '未达标' }}</strong>
                <span>学时状态</span>
              </div>
            </div>
          </div>

          <div class="chapter-list">
            <section v-for="chapter in detail.chapters || []" :key="chapter.id" class="chapter-card app-card inner-card">
              <div class="chapter-top">
                <div>
                  <h4>{{ chapter.sort }}. {{ chapter.title }}</h4>
                  <p>{{ chapter.description || '暂无章节说明' }}</p>
                </div>
              </div>

              <div class="lesson-list">
                <article v-for="lesson in chapter.lessons || []" :key="lesson.id" class="lesson-item">
                  <div>
                    <strong>{{ lesson.title }}</strong>
                    <span>{{ lessonTypeText(lesson.lessonType) }} · {{ lesson.durationSeconds || 0 }} 秒</span>
                  </div>
                  <div class="lesson-side">
                    <el-progress :percentage="Number(lesson.learningProgress || 0)" :stroke-width="8" />
                    <el-button type="primary" plain @click="studyLesson(lesson)">
                      {{ lesson.completed ? '继续巩固' : '记录学习' }}
                    </el-button>
                  </div>
                </article>
              </div>
            </section>
          </div>
        </template>
      </main>
    </section>
  </div>
</template>

<style scoped lang="scss">
.student-course-page { gap: 18px; }
.workspace {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 18px;
}
.list-panel,
.detail-panel { padding: 20px; }
.course-stack {
  display: grid;
  gap: 10px;
  margin-top: 16px;
}
.course-card {
  text-align: left;
  display: grid;
  gap: 8px;
  padding: 16px;
  border-radius: 16px;
  border: 1px solid var(--line-soft);
  background: rgba(255,255,255,.6);
  cursor: pointer;
}
.course-card.active { border-color: rgba(161,100,47,.45); background: rgba(255,248,240,.95); }
.course-card span,
.detail-head p,
.chapter-top p,
.lesson-item span,
.meta-grid label,
.empty-state { color: var(--text-secondary); }
.detail-head,
.chapter-top,
.detail-actions,
.lesson-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}
.detail-head h2,
.chapter-top h4 { margin: 0; }
.meta-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin: 16px 0;
}
.meta-grid > div,
.inner-card {
  background: rgba(255,255,255,.58);
  border: 1px solid var(--line-soft);
  border-radius: 16px;
}
.meta-grid > div { padding: 14px; display: grid; gap: 6px; }
.inner-card { padding: 18px; }
.overview-metrics {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}
.overview-metrics > div {
  padding: 14px;
  border-radius: 14px;
  background: rgba(255,255,255,.68);
  border: 1px solid var(--line-soft);
}
.overview-metrics strong,
.overview-metrics span { display: block; }
.overview-metrics strong { font-size: 24px; font-family: var(--font-display); }
.chapter-list { display: grid; gap: 14px; }
.lesson-list { display: grid; gap: 10px; margin-top: 12px; }
.lesson-item {
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(255,255,255,.78);
  border: 1px solid var(--line-soft);
}
.lesson-side {
  min-width: 240px;
  display: grid;
  gap: 10px;
}

@media (max-width: 1100px) {
  .workspace { grid-template-columns: 1fr; }
  .meta-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 760px) {
  .meta-grid,
  .overview-metrics { grid-template-columns: 1fr; }
  .detail-head,
  .lesson-item { flex-direction: column; align-items: flex-start; }
  .lesson-side { min-width: 100%; width: 100%; }
}
</style>
