<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { getStudentCoursesApi } from '../../../api/course'
import { applyStudentCertificateApi, getStudentCertificatesApi } from '../../../api/certificate'

const courses = ref([])
const certificates = ref([])
const selectedCourseId = ref(null)
const applying = ref(false)
const applyForm = reactive({ applyReason: '' })

const metrics = computed(() => [
  { label: '申请总数', value: certificates.value.length },
  { label: '待审核', value: certificates.value.filter((item) => item.status === 0).length },
  { label: '已通过', value: certificates.value.filter((item) => item.status === 1).length },
  { label: '已驳回', value: certificates.value.filter((item) => item.status === 2).length },
])

onMounted(async () => {
  await Promise.all([loadCourses(), loadCertificates()])
})

async function loadCourses() {
  courses.value = await getStudentCoursesApi({ status: 2 })
  selectedCourseId.value = courses.value[0]?.id || null
}

async function loadCertificates() {
  certificates.value = await getStudentCertificatesApi()
}

async function applyCertificate() {
  if (!selectedCourseId.value) {
    ElMessage.warning('请先选择已完成课程')
    return
  }
  applying.value = true
  try {
    await applyStudentCertificateApi(selectedCourseId.value, applyForm)
    ElMessage.success('证书申请已提交')
    applyForm.applyReason = ''
    await loadCertificates()
  } catch (error) {
    ElMessage.error(error.message || '申请失败')
  } finally {
    applying.value = false
  }
}

function statusText(status) {
  return ['待审核', '已通过', '已驳回'][status] || '未知'
}
</script>

<template>
  <div class="page-wrap certificate-page">
    <div class="page-heading">
      <div>
        <h1>证书中心</h1>
        <p>针对已完成并达标的课程发起证书申请，并跟踪审核和发证结果。</p>
      </div>
    </div>

    <section class="stat-grid">
      <article v-for="item in metrics" :key="item.label" class="app-card stat-card">
        <div class="label">{{ item.label }}</div>
        <div class="value">{{ item.value }}</div>
      </article>
    </section>

    <section class="content-grid">
      <article class="app-card panel-block">
        <h3>申请证书</h3>
        <el-form label-position="top">
          <el-form-item label="已完成课程">
            <el-select v-model="selectedCourseId" placeholder="选择课程">
              <el-option v-for="course in courses" :key="course.id" :label="course.title" :value="course.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="申请说明">
            <el-input v-model="applyForm.applyReason" type="textarea" :rows="4" placeholder="可填写补充说明" />
          </el-form-item>
          <el-button type="primary" :loading="applying" @click="applyCertificate">提交申请</el-button>
        </el-form>
      </article>

      <article class="app-card panel-block">
        <h3>申请记录</h3>
        <div class="soft-list">
          <div v-for="item in certificates" :key="item.id" class="soft-item">
            <strong>{{ item.courseTitle }}</strong>
            <span>{{ statusText(item.status) }} · 申请于 {{ item.applyTime }}</span>
            <span>审核意见：{{ item.reviewRemark || '暂无' }}</span>
            <span v-if="item.record">证书编号：{{ item.record.certificateNo }}</span>
          </div>
          <div v-if="!certificates.length" class="soft-item">
            <strong>暂无申请记录</strong>
            <span>完成课程并达标后，可在这里提交证书申请。</span>
          </div>
        </div>
      </article>
    </section>
  </div>
</template>
