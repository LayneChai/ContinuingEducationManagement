<script setup>
import { computed, nextTick, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import DOMPurify from 'dompurify'
import { marked } from 'marked'

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
const activeLessonId = ref(null)
const selectedLesson = ref(null)
const lessonViewerVisible = ref(false)
const detailLoading = ref(false)
const studyingLessonIds = ref([])
const autoRecordingLessonIds = ref([])
const lessonContentRef = ref(null)
const lessonVideoRef = ref(null)
const pdfScrollRef = ref(null)
const videoAutoProgressMap = ref({})
const contentAutoCompleteMap = ref({})
const pdfAutoProgressMap = ref({})
const videoLearnedSecondsMap = ref({})

marked.setOptions({ breaks: true, gfm: true })

const renderedLessonContent = computed(() => {
  if (!selectedLesson.value?.content) {
    return ''
  }
  return DOMPurify.sanitize(marked.parse(selectedLesson.value.content))
})

const discoverMetrics = computed(() => [
  { label: '公开课程', value: publicCourses.value.length },
  { label: '我的课程', value: myCourses.value.length },
  { label: '学习中', value: myCourses.value.filter((item) => item.enrollmentStatus === 1).length },
  { label: '已完成', value: myCourses.value.filter((item) => item.enrollmentStatus === 2).length },
])

const allLessons = computed(() => (detail.value?.chapters || []).flatMap((chapter) => chapter.lessons || []))

const selectedLessonIndex = computed(() => allLessons.value.findIndex((lesson) => lesson.id === selectedLesson.value?.id))

const previousLesson = computed(() => {
  if (selectedLessonIndex.value <= 0) {
    return null
  }
  return allLessons.value[selectedLessonIndex.value - 1] || null
})

const nextLesson = computed(() => {
  if (selectedLessonIndex.value < 0 || selectedLessonIndex.value >= allLessons.value.length - 1) {
    return null
  }
  return allLessons.value[selectedLessonIndex.value + 1] || null
})

const viewerProgress = computed(() => {
  const lesson = selectedLesson.value
  if (!lesson) {
    return 0
  }
  if (isVideoResource(lesson.resourceUrl)) {
    return Math.max(Number(lesson.learningProgress || 0), Number(videoAutoProgressMap.value[lesson.id] || 0))
  }
  if (isPdfResource(lesson.resourceUrl)) {
    return Math.max(Number(lesson.learningProgress || 0), Number(pdfAutoProgressMap.value[lesson.id] || 0))
  }
  return Number(lesson.learningProgress || 0)
})

const learnedTimeText = computed(() => {
  const lesson = selectedLesson.value
  if (!lesson) {
    return '已学习 0 分 0 秒'
  }
  const seconds = Math.max(
    Number(videoLearnedSecondsMap.value[lesson.id] || 0),
    Math.round((Number(lesson.learningProgress || 0) / 100) * Math.max(Number(lesson.durationSeconds || 0), 0)),
  )
  const minutes = Math.floor(seconds / 60)
  const remainSeconds = seconds % 60
  return `已学习 ${minutes} 分 ${remainSeconds} 秒`
})

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

async function openCourse(courseId, options = {}) {
  const { autoRecord = true } = options
  activeCourseId.value = courseId
  detailLoading.value = true
  try {
    const preferredLessonId = activeCourseId.value === courseId ? activeLessonId.value : null
    detail.value = await getPublicCourseDetailApi(courseId)
    const lessonList = (detail.value.chapters || []).flatMap((chapter) => chapter.lessons || [])
    const currentLesson = lessonList.find((lesson) => lesson.id === preferredLessonId) || lessonList[0] || null
    selectedLesson.value = currentLesson
    activeLessonId.value = currentLesson?.id || null
    await nextTick()
    if (currentLesson) {
      maybeTrackScrollableContent(currentLesson)
    }
    if (autoRecord && detail.value.enrolled && currentLesson) {
      autoRecordLesson(currentLesson)
    }
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
  await recordLessonProgress(lesson, { silent: false, increment: 25 })
}

async function recordLessonProgress(lesson, { silent, increment }) {
  if (!detail.value?.enrolled) {
    if (!silent) {
      ElMessage.warning('请先报名课程')
    }
    return
  }
  if (studyingLessonIds.value.includes(lesson.id)) {
    return
  }
  studyingLessonIds.value = [...studyingLessonIds.value, lesson.id]
  try {
    const baseSeconds = Math.max(Number(lesson.durationSeconds || 0), 600)
    const nextProgress = Math.min(100, Number(lesson.learningProgress || 0) + increment)
    await studyLessonApi(detail.value.id, lesson.id, {
      studySeconds: baseSeconds,
      progressPercent: nextProgress,
      lastPosition: baseSeconds,
      completed: nextProgress >= 100 ? 1 : 0,
    })
    if (!silent) {
      ElMessage.success(nextProgress >= 100 ? '课时已完成' : '学习进度已更新')
    }
    await Promise.all([loadMyCourses(), openCourse(detail.value.id, { autoRecord: false })])
  } catch (error) {
    if (!silent) {
      ElMessage.error(error.message || '学习进度更新失败')
    }
  } finally {
    studyingLessonIds.value = studyingLessonIds.value.filter((id) => id !== lesson.id)
  }
}

async function autoRecordLesson(lesson) {
  if (autoRecordingLessonIds.value.includes(lesson.id)) {
    return
  }
  autoRecordingLessonIds.value = [...autoRecordingLessonIds.value, lesson.id]
  try {
    await recordLessonProgress(lesson, { silent: true, increment: 10 })
  } finally {
    autoRecordingLessonIds.value = autoRecordingLessonIds.value.filter((id) => id !== lesson.id)
  }
}

function openLesson(lesson) {
  if (!detail.value?.enrolled && lesson.previewEnabled !== 1) {
    ElMessage.warning('请先报名课程后查看该课时内容')
    return
  }
  selectedLesson.value = lesson
  activeLessonId.value = lesson.id
  lessonViewerVisible.value = true
  nextTick(() => {
    maybeTrackScrollableContent(lesson)
    maybeTrackPdfScroll(lesson)
  })
  if (detail.value?.enrolled) {
    autoRecordLesson(lesson)
  }
}

function openAdjacentLesson(targetLesson) {
  if (!targetLesson) {
    return
  }
  openLesson(targetLesson)
}

function closeLessonViewer() {
  lessonViewerVisible.value = false
}

function courseStatusText(status) {
  return ['已报名', '学习中', '已完成', '已取消'][status] || '未报名'
}

function lessonTypeText(type) {
  return ['视频', '文档', '图文'][type - 1] || '未知类型'
}

function hasLessonMaterial(lesson) {
  return Boolean(lesson?.content || lesson?.resourceUrl)
}

function lessonActionText(lesson) {
  return activeLessonId.value === lesson.id ? '已打开' : '查看内容'
}

function normalizeResourceUrl(url) {
  return url ? String(url).trim() : ''
}

function resourceExtension(url) {
  const cleanUrl = normalizeResourceUrl(url).split('?')[0]
  const segments = cleanUrl.split('.')
  return segments.length > 1 ? segments.pop().toLowerCase() : ''
}

function isHttpUrl(url) {
  return /^https?:\/\//i.test(normalizeResourceUrl(url))
}

function isVideoResource(url) {
  return ['mp4', 'webm', 'ogg', 'mov', 'm4v'].includes(resourceExtension(url))
}

function isPdfResource(url) {
  return resourceExtension(url) === 'pdf'
}

function isImageResource(url) {
  return ['png', 'jpg', 'jpeg', 'gif', 'webp', 'svg'].includes(resourceExtension(url))
}

function isOfficeDocument(url) {
  return ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx'].includes(resourceExtension(url))
}

function bilibiliEmbedUrl(url) {
  const normalized = normalizeResourceUrl(url)
  const bvMatch = normalized.match(/bilibili\.com\/video\/(BV[0-9A-Za-z]+)/i)
  if (bvMatch) {
    const pageMatch = normalized.match(/[?&]p=(\d+)/i)
    const page = pageMatch ? pageMatch[1] : '1'
    return `https://player.bilibili.com/player.html?bvid=${bvMatch[1]}&page=${page}`
  }
  if (/player\.bilibili\.com\/player\.html/i.test(normalized)) {
    return normalized
  }
  return ''
}

function officePreviewUrl(url) {
  if (!isHttpUrl(url)) {
    return ''
  }
  return `https://view.officeapps.live.com/op/embed.aspx?src=${encodeURIComponent(normalizeResourceUrl(url))}`
}

function handleVideoTimeUpdate() {
  const lesson = selectedLesson.value
  const video = lessonVideoRef.value
  if (!lesson || !video || !detail.value?.enrolled) {
    return
  }
  const duration = Number(video.duration || lesson.durationSeconds || 0)
  if (!duration || !Number.isFinite(duration)) {
    return
  }
  videoLearnedSecondsMap.value = {
    ...videoLearnedSecondsMap.value,
    [lesson.id]: Math.max(Number(videoLearnedSecondsMap.value[lesson.id] || 0), Math.round(video.currentTime || 0)),
  }
  const progress = Math.min(100, Math.round((video.currentTime / duration) * 100))
  const currentSaved = Number(lesson.learningProgress || 0)
  const lastAuto = Number(videoAutoProgressMap.value[lesson.id] || 0)
  if (progress >= Math.max(currentSaved + 10, lastAuto + 10)) {
    videoAutoProgressMap.value = { ...videoAutoProgressMap.value, [lesson.id]: progress }
    recordLessonProgress(lesson, { silent: true, increment: Math.max(progress - currentSaved, 1) })
  }
}

function handleVideoEnded() {
  const lesson = selectedLesson.value
  if (!lesson || !detail.value?.enrolled) {
    return
  }
  videoLearnedSecondsMap.value = {
    ...videoLearnedSecondsMap.value,
    [lesson.id]: Math.max(Number(lesson.durationSeconds || 0), Number(videoLearnedSecondsMap.value[lesson.id] || 0)),
  }
  const currentSaved = Number(lesson.learningProgress || 0)
  if (currentSaved < 100) {
    recordLessonProgress(lesson, { silent: true, increment: Math.max(100 - currentSaved, 1) })
  }
}

function maybeTrackScrollableContent(lesson) {
  if (!lesson || !detail.value?.enrolled || !lessonContentRef.value) {
    return
  }
  const el = lessonContentRef.value
  if (el.scrollHeight <= el.clientHeight + 8) {
    handleLessonContentScroll()
  }
}

function handleLessonContentScroll() {
  const lesson = selectedLesson.value
  const el = lessonContentRef.value
  if (!lesson || !el || !detail.value?.enrolled || contentAutoCompleteMap.value[lesson.id]) {
    return
  }
  const reachedBottom = el.scrollTop + el.clientHeight >= el.scrollHeight - 16
  if (!reachedBottom) {
    return
  }
  contentAutoCompleteMap.value = { ...contentAutoCompleteMap.value, [lesson.id]: true }
  const currentSaved = Number(lesson.learningProgress || 0)
  recordLessonProgress(lesson, { silent: true, increment: Math.max(100 - currentSaved, 1) })
}

function maybeTrackPdfScroll(lesson) {
  if (!lesson || !detail.value?.enrolled || !isPdfResource(lesson.resourceUrl) || !pdfScrollRef.value) {
    return
  }
  handlePdfScroll()
}

function handlePdfScroll() {
  const lesson = selectedLesson.value
  const el = pdfScrollRef.value
  if (!lesson || !el || !detail.value?.enrolled || !isPdfResource(lesson.resourceUrl)) {
    return
  }
  const scrollable = Math.max(el.scrollHeight - el.clientHeight, 1)
  const progress = Math.min(100, Math.round((el.scrollTop / scrollable) * 100))
  const saved = Number(lesson.learningProgress || 0)
  const auto = Number(pdfAutoProgressMap.value[lesson.id] || 0)
  pdfAutoProgressMap.value = { ...pdfAutoProgressMap.value, [lesson.id]: progress }
  if (progress >= Math.max(saved + 20, auto + 20)) {
    recordLessonProgress(lesson, { silent: true, increment: Math.max(progress - saved, 1) })
  }
  if (progress >= 98 && !contentAutoCompleteMap.value[lesson.id]) {
    contentAutoCompleteMap.value = { ...contentAutoCompleteMap.value, [lesson.id]: true }
    recordLessonProgress(lesson, { silent: true, increment: Math.max(100 - saved, 1) })
  }
}
</script>

<template>
  <div class="page-wrap student-course-page">
    <div class="page-heading">
      <div>
        <h1>课程学习</h1>
        <p>浏览公开课程、加入我的学习计划，并记录每个课时的学习进度和时长完成情况。</p>
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
            <span>{{ course.categoryName || '未分类' }} · {{ course.requiredHours }} 小时</span>
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
            <div><label>要求时长</label><span>{{ detail.requiredHours }} 小时</span></div>
            <div><label>已完成时长</label><span>{{ overview?.completedHours || detail.completedHours || 0 }} 小时</span></div>
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
                <span>时长状态</span>
              </div>
            </div>
          </div>

          <div class="learning-layout">
            <div class="chapter-list">
              <section v-for="chapter in detail.chapters || []" :key="chapter.id" class="chapter-card app-card inner-card">
                <div class="chapter-top">
                  <div>
                    <h4>{{ chapter.sort }}. {{ chapter.title }}</h4>
                    <p>{{ chapter.description || '暂无章节说明' }}</p>
                  </div>
                </div>

                <div class="lesson-list">
                  <article v-for="lesson in chapter.lessons || []" :key="lesson.id" class="lesson-item" :class="{ active: activeLessonId === lesson.id }">
                    <div>
                      <strong>{{ lesson.title }}</strong>
                      <span>{{ lessonTypeText(lesson.lessonType) }} · {{ Math.round(Number(lesson.durationSeconds || 0) / 60) }} 分钟</span>
                    </div>
                    <div class="lesson-side">
                      <el-progress :percentage="Number(lesson.learningProgress || 0)" :stroke-width="8" />
                      <div class="lesson-actions">
                        <el-button plain :disabled="!hasLessonMaterial(lesson)" @click="openLesson(lesson)">
                          {{ hasLessonMaterial(lesson) ? lessonActionText(lesson) : '暂无内容' }}
                        </el-button>
                        <el-button
                          type="primary"
                          plain
                          :loading="studyingLessonIds.includes(lesson.id)"
                          :disabled="studyingLessonIds.includes(lesson.id)"
                          @click="studyLesson(lesson)"
                        >
                          {{ lesson.completed ? '继续巩固' : '记录学习' }}
                        </el-button>
                      </div>
                    </div>
                  </article>
                </div>
              </section>
            </div>
          </div>
        </template>
      </main>
    </section>

    <el-dialog v-model="lessonViewerVisible" width="min(1100px, 92vw)" top="4vh" destroy-on-close class="lesson-dialog" @closed="closeLessonViewer">
      <template #header>
        <div v-if="selectedLesson" class="lesson-view-head">
          <div>
            <h3>{{ selectedLesson.title }}</h3>
            <p>{{ lessonTypeText(selectedLesson.lessonType) }} · {{ Math.round(Number(selectedLesson.durationSeconds || 0) / 60) }} 分钟</p>
          </div>
          <el-tag :type="selectedLesson.previewEnabled === 1 ? 'success' : 'info'">
            {{ selectedLesson.previewEnabled === 1 ? '支持试看' : '需报名后学习' }}
          </el-tag>
        </div>
      </template>

      <div v-if="selectedLesson" class="lesson-viewer-shell">
        <div class="lesson-nav">
          <el-button :disabled="!previousLesson" @click="openAdjacentLesson(previousLesson)">上一课</el-button>
          <el-button type="primary" plain :disabled="!nextLesson" @click="openAdjacentLesson(nextLesson)">下一课</el-button>
        </div>

        <div class="viewer-progress-card">
          <div class="viewer-progress-head">
            <span>学习进度</span>
            <strong>{{ Math.round(viewerProgress) }}%</strong>
          </div>
          <el-progress :percentage="Math.round(viewerProgress)" :stroke-width="10" />
          <div v-if="isVideoResource(selectedLesson.resourceUrl)" class="viewer-progress-time">{{ learnedTimeText }}</div>
        </div>

        <a v-if="selectedLesson.resourceUrl" :href="selectedLesson.resourceUrl" target="_blank" rel="noreferrer" class="resource-link">
          打开课时资源
        </a>

        <video
          v-if="isVideoResource(selectedLesson.resourceUrl)"
          ref="lessonVideoRef"
          class="lesson-media"
          :src="normalizeResourceUrl(selectedLesson.resourceUrl)"
          controls
          playsinline
          @timeupdate="handleVideoTimeUpdate"
          @ended="handleVideoEnded"
        />

        <img v-else-if="isImageResource(selectedLesson.resourceUrl)" class="lesson-image" :src="normalizeResourceUrl(selectedLesson.resourceUrl)" :alt="selectedLesson.title" />

        <div v-else-if="isPdfResource(selectedLesson.resourceUrl)" ref="pdfScrollRef" class="pdf-scroll-box" @scroll="handlePdfScroll">
          <iframe class="lesson-frame pdf-frame" :src="normalizeResourceUrl(selectedLesson.resourceUrl)" title="PDF 预览" />
        </div>

        <iframe
          v-else-if="bilibiliEmbedUrl(selectedLesson.resourceUrl)"
          class="lesson-frame"
          :src="bilibiliEmbedUrl(selectedLesson.resourceUrl)"
          title="Bilibili 视频播放"
          allowfullscreen
        />

        <iframe
          v-else-if="isOfficeDocument(selectedLesson.resourceUrl) && officePreviewUrl(selectedLesson.resourceUrl)"
          class="lesson-frame"
          :src="officePreviewUrl(selectedLesson.resourceUrl)"
          title="Office 文档预览"
        />

        <iframe
          v-else-if="selectedLesson.resourceUrl && isHttpUrl(selectedLesson.resourceUrl) && !selectedLesson.content"
          class="lesson-frame"
          :src="normalizeResourceUrl(selectedLesson.resourceUrl)"
          title="课时资源预览"
        />

        <div
          v-if="selectedLesson.content"
          ref="lessonContentRef"
          class="lesson-content markdown-body"
          v-html="renderedLessonContent"
          @scroll="handleLessonContentScroll"
        ></div>
        <div v-else-if="selectedLesson.resourceUrl" class="empty-state">当前课时以外部资源为主，可在本窗口浏览或点击上方按钮打开。</div>
        <div v-else class="empty-state">当前课时暂未配置图文内容或资源地址。</div>
      </div>
    </el-dialog>
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
.detail-panel { overflow: hidden; }
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
.learning-layout {
  display: block;
}
.lesson-list { display: grid; gap: 10px; margin-top: 12px; }
.lesson-item {
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(255,255,255,.78);
  border: 1px solid var(--line-soft);
}
.lesson-item.active {
  border-color: rgba(161,100,47,.45);
  background: rgba(255,248,240,.95);
}
.lesson-side {
  min-width: 240px;
  display: grid;
  gap: 10px;
}
.lesson-actions {
  display: flex;
  gap: 10px;
}
.lesson-view-head h3 { margin: 0; }
.lesson-nav {
  display: flex;
  gap: 10px;
}
.lesson-viewer-shell {
  display: grid;
  gap: 14px;
}
.viewer-progress-card {
  padding: 14px 16px;
  border-radius: 14px;
  border: 1px solid var(--line-soft);
  background: rgba(255,255,255,.66);
}
.viewer-progress-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.viewer-progress-time {
  margin-top: 10px;
  color: var(--text-secondary);
}
.resource-link {
  display: inline-flex;
  align-items: center;
  width: fit-content;
  padding: 10px 14px;
  border-radius: 12px;
  color: #7a4a22;
  background: rgba(161,100,47,.12);
  border: 1px solid rgba(161,100,47,.2);
  text-decoration: none;
}
.lesson-media,
.lesson-image,
.lesson-frame {
  width: 100%;
  border: 1px solid var(--line-soft);
  border-radius: 14px;
  background: rgba(255,255,255,.72);
}
.lesson-media,
.lesson-image {
  max-height: 420px;
}
.lesson-image {
  object-fit: contain;
}
.lesson-frame {
  min-height: 420px;
}
.pdf-scroll-box {
  max-height: 70vh;
  overflow: auto;
  padding-right: 4px;
}
.pdf-frame {
  min-height: 1200px;
}
.lesson-content {
  max-height: 70vh;
  overflow: auto;
  line-height: 1.8;
  padding-right: 6px;
}
.markdown-body :deep(p) { margin: 0 0 12px; }
.markdown-body :deep(ul),
.markdown-body :deep(ol) { margin: 0 0 12px; padding-left: 20px; }
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
  background: rgba(161,100,47,.12);
  font-family: Consolas, 'Courier New', monospace;
}
.markdown-body :deep(pre code) { padding: 0; background: transparent; }

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
  .lesson-actions { width: 100%; flex-direction: column; }
  .lesson-nav { flex-direction: column; }
}
</style>
