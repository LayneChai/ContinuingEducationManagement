<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { getTeacherCertificatesApi, reviewTeacherCertificateApi } from '../../../api/certificate'

const statusFilter = ref(undefined)
const loading = ref(false)
const applications = ref([])

const reviewDialog = reactive({
  visible: false,
  saving: false,
  applyId: null,
  studentName: '',
  courseTitle: '',
  form: {
    status: 1,
    reviewRemark: '',
    certificateTitle: '',
    certificateUrl: '',
  },
})

const metrics = computed(() => [
  { label: '申请总量', value: applications.value.length },
  { label: '待审核', value: applications.value.filter((item) => item.status === 0).length },
  { label: '已通过', value: applications.value.filter((item) => item.status === 1).length },
  { label: '已驳回', value: applications.value.filter((item) => item.status === 2).length },
])

onMounted(loadApplications)

async function loadApplications() {
  loading.value = true
  try {
    applications.value = await getTeacherCertificatesApi(statusFilter.value !== undefined ? { status: statusFilter.value } : undefined)
  } finally {
    loading.value = false
  }
}

function openReviewDialog(item) {
  reviewDialog.visible = true
  reviewDialog.applyId = item.id
  reviewDialog.studentName = item.studentName
  reviewDialog.courseTitle = item.courseTitle
  reviewDialog.form = {
    status: item.status === 2 ? 1 : item.status,
    reviewRemark: item.reviewRemark || '',
    certificateTitle: item.record?.certificateTitle || `${item.courseTitle}结业证书`,
    certificateUrl: item.record?.certificateUrl || '',
  }
}

async function submitReview() {
  reviewDialog.saving = true
  try {
    await reviewTeacherCertificateApi(reviewDialog.applyId, reviewDialog.form)
    ElMessage.success('证书审核完成')
    reviewDialog.visible = false
    await loadApplications()
  } catch (error) {
    ElMessage.error(error.message || '审核失败')
  } finally {
    reviewDialog.saving = false
  }
}

function statusText(status) {
  return ['待审核', '已通过', '已驳回'][status] || '未知'
}
</script>

<template>
  <div class="page-wrap teacher-certificate-page">
    <div class="page-heading">
      <div>
        <h1>证书审核</h1>
        <p>仅查看并审核自己课程下的学员证书申请记录。</p>
      </div>
      <div class="top-actions">
        <el-select v-model="statusFilter" clearable placeholder="按状态筛选" style="width: 220px" @change="loadApplications">
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已驳回" :value="2" />
        </el-select>
        <el-button @click="loadApplications">刷新</el-button>
      </div>
    </div>

    <section class="stat-grid">
      <article v-for="item in metrics" :key="item.label" class="app-card stat-card">
        <div class="label">{{ item.label }}</div>
        <div class="value">{{ item.value }}</div>
      </article>
    </section>

    <section class="app-card panel-block">
      <div class="soft-list">
        <div v-for="item in applications" :key="item.id" class="soft-item application-item">
          <div>
            <strong>{{ item.courseTitle }}</strong>
            <span>{{ item.studentName }} · {{ statusText(item.status) }}</span>
            <span>申请说明：{{ item.applyReason || '暂无' }}</span>
            <span>审核意见：{{ item.reviewRemark || '暂无' }}</span>
          </div>
          <div class="application-side">
            <span v-if="item.record">证书编号：{{ item.record.certificateNo }}</span>
            <el-button type="primary" plain @click="openReviewDialog(item)">审核处理</el-button>
          </div>
        </div>
        <div v-if="!applications.length && !loading" class="soft-item">
          <strong>暂无证书申请</strong>
          <span>当前仅展示你本人课程下的证书申请数据。</span>
        </div>
      </div>
    </section>

    <el-dialog v-model="reviewDialog.visible" :title="`审核：${reviewDialog.studentName}`" width="680px">
      <el-form label-position="top">
        <el-form-item label="课程"><el-input :model-value="reviewDialog.courseTitle" disabled /></el-form-item>
        <el-form-item label="审核结果">
          <el-radio-group v-model="reviewDialog.form.status">
            <el-radio :value="1">通过</el-radio>
            <el-radio :value="2">驳回</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见"><el-input v-model="reviewDialog.form.reviewRemark" type="textarea" :rows="3" /></el-form-item>
        <template v-if="reviewDialog.form.status === 1">
          <el-form-item label="证书标题"><el-input v-model="reviewDialog.form.certificateTitle" /></el-form-item>
          <el-form-item label="证书地址"><el-input v-model="reviewDialog.form.certificateUrl" placeholder="可填写图片或 PDF 地址" /></el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="reviewDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="reviewDialog.saving" @click="submitReview">提交审核</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.teacher-certificate-page { gap: 18px; }
.top-actions, .application-item { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.application-side { display: grid; justify-items: end; gap: 8px; }
@media (max-width: 760px) {
  .top-actions, .application-item { align-items: flex-start; flex-direction: column; }
  .application-side { justify-items: start; }
}
</style>
