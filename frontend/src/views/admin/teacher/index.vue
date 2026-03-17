<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { createAdminUserApi, getAdminUsersApi } from '../../../api/admin'

const filters = reactive({
  keyword: '',
  status: undefined,
})

const teachers = ref([])
const loading = ref(false)

const createDialog = reactive({
  visible: false,
  saving: false,
  form: defaultTeacherForm(),
})

const metrics = computed(() => [
  { label: '教师总数', value: teachers.value.length },
  { label: '启用教师', value: teachers.value.filter((item) => item.status === 1).length },
  { label: '带职称信息', value: teachers.value.filter((item) => item.title).length },
  { label: '已配置邮箱', value: teachers.value.filter((item) => item.email).length },
])

onMounted(loadTeachers)

function defaultTeacherForm() {
  return {
    username: '',
    password: 'Teacher@123456',
    realName: '',
    phone: '',
    email: '',
    studentNo: '',
    title: '',
    bio: '',
    roleCodes: ['TEACHER'],
  }
}

async function loadTeachers() {
  loading.value = true
  try {
    teachers.value = await getAdminUsersApi({ ...filters, roleCode: 'TEACHER' })
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  createDialog.visible = true
  createDialog.form = defaultTeacherForm()
}

async function submitCreate() {
  createDialog.saving = true
  try {
    await createAdminUserApi(createDialog.form)
    ElMessage.success('教师账号已创建')
    createDialog.visible = false
    await loadTeachers()
  } catch (error) {
    ElMessage.error(error.message || '创建失败')
  } finally {
    createDialog.saving = false
  }
}

function statusText(status) {
  return status === 1 ? '启用' : '禁用'
}
</script>

<template>
  <div class="page-wrap admin-teacher-page">
    <div class="page-heading">
      <div>
        <h1>教师管理</h1>
        <p>维护授课教师账号、职称和联系方式，为课程、考试、作业模块提供师资基础数据。</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">新增教师</el-button>
    </div>

    <section class="stat-grid">
      <article v-for="item in metrics" :key="item.label" class="app-card stat-card">
        <div class="label">{{ item.label }}</div>
        <div class="value">{{ item.value }}</div>
      </article>
    </section>

    <section class="toolbar app-card">
      <el-input v-model="filters.keyword" placeholder="搜索教师姓名或账号" clearable @keyup.enter="loadTeachers" />
      <el-select v-model="filters.status" placeholder="账号状态" clearable>
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button @click="loadTeachers">查询</el-button>
    </section>

    <section class="app-card panel-block">
      <el-table :data="teachers" v-loading="loading" stripe>
        <el-table-column prop="realName" label="姓名" min-width="120" />
        <el-table-column prop="username" label="账号" min-width="140" />
        <el-table-column prop="title" label="职称/岗位" min-width="140" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">{{ statusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
      </el-table>
    </section>

    <el-dialog v-model="createDialog.visible" title="新增教师" width="720px">
      <el-form label-position="top">
        <div class="dialog-grid two-col">
          <el-form-item label="姓名"><el-input v-model="createDialog.form.realName" /></el-form-item>
          <el-form-item label="登录账号"><el-input v-model="createDialog.form.username" /></el-form-item>
        </div>
        <div class="dialog-grid two-col">
          <el-form-item label="初始密码"><el-input v-model="createDialog.form.password" /></el-form-item>
          <el-form-item label="职称 / 岗位"><el-input v-model="createDialog.form.title" /></el-form-item>
        </div>
        <div class="dialog-grid two-col">
          <el-form-item label="手机号"><el-input v-model="createDialog.form.phone" /></el-form-item>
          <el-form-item label="邮箱"><el-input v-model="createDialog.form.email" /></el-form-item>
        </div>
        <el-form-item label="教师简介"><el-input v-model="createDialog.form.bio" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="createDialog.saving" @click="submitCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.admin-teacher-page { gap: 18px; }
.toolbar {
  display: grid;
  grid-template-columns: 1fr 180px 100px;
  gap: 12px;
  padding: 16px;
}
.dialog-grid { display: grid; gap: 14px; }
.two-col { grid-template-columns: repeat(2, 1fr); }
@media (max-width: 760px) {
  .toolbar, .two-col { grid-template-columns: 1fr; }
}
</style>
