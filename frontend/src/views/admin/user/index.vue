<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import { createAdminUserApi, getAdminRolesApi, getAdminUsersApi, updateAdminUserApi } from '../../../api/admin'

const filters = reactive({
  keyword: '',
  status: undefined,
})

const roles = ref([])
const users = ref([])
const loading = ref(false)

const createDialog = reactive({
  visible: false,
  saving: false,
  form: defaultUserForm(),
})

const editDialog = reactive({
  visible: false,
  saving: false,
  userId: null,
  form: defaultEditForm(),
})

const metrics = computed(() => [
  { label: '学员总数', value: users.value.length },
  { label: '启用账号', value: users.value.filter((item) => item.status === 1).length },
  { label: '禁用账号', value: users.value.filter((item) => item.status === 0).length },
  { label: '带证书编号', value: users.value.filter((item) => item.studentNo).length },
])

onMounted(async () => {
  await Promise.all([loadRoles(), loadUsers()])
})

function defaultUserForm() {
  return {
    username: '',
    password: 'Student@123456',
    realName: '',
    phone: '',
    email: '',
    studentNo: '',
    className: '',
    title: '',
    bio: '',
    status: 1,
    roleCodes: ['STUDENT'],
  }
}

function defaultEditForm() {
  return {
    username: '',
    realName: '',
    phone: '',
    email: '',
    studentNo: '',
    className: '',
    bio: '',
    password: '',
    status: 1,
  }
}

async function loadRoles() {
  roles.value = await getAdminRolesApi()
}

async function loadUsers() {
  loading.value = true
  try {
    users.value = await getAdminUsersApi({ ...filters, roleCode: 'STUDENT' })
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  createDialog.visible = true
  createDialog.form = defaultUserForm()
}

function openEditDialog(user) {
  editDialog.visible = true
  editDialog.userId = user.id
  editDialog.form = {
    username: user.username,
    realName: user.realName,
    phone: user.phone || '',
    email: user.email || '',
    studentNo: user.studentNo || '',
    className: user.className || '',
    bio: user.bio || '',
    password: '',
    status: user.status,
  }
}

async function submitCreate() {
  createDialog.saving = true
  try {
    await createAdminUserApi(createDialog.form)
    ElMessage.success('学员账号已创建')
    createDialog.visible = false
    await loadUsers()
  } catch (error) {
    ElMessage.error(error.message || '创建失败')
  } finally {
    createDialog.saving = false
  }
}

async function submitEdit() {
  editDialog.saving = true
  try {
    await updateAdminUserApi(editDialog.userId, editDialog.form)
    ElMessage.success('学员信息已更新')
    editDialog.visible = false
    await loadUsers()
  } catch (error) {
    ElMessage.error(error.message || '更新失败')
  } finally {
    editDialog.saving = false
  }
}

function statusText(status) {
  return status === 1 ? '启用' : '禁用'
}
</script>

<template>
  <div class="page-wrap admin-user-page">
    <div class="page-heading">
      <div>
        <h1>学员管理</h1>
        <p>查看继续教育学员账号状态，并快速新增学员账号用于课程报名与学习。</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">新增学员</el-button>
    </div>

    <section class="stat-grid">
      <article v-for="item in metrics" :key="item.label" class="app-card stat-card">
        <div class="label">{{ item.label }}</div>
        <div class="value">{{ item.value }}</div>
      </article>
    </section>

    <section class="toolbar app-card">
      <el-input v-model="filters.keyword" placeholder="搜索账号或姓名" clearable @keyup.enter="loadUsers" />
      <el-select v-model="filters.status" placeholder="账号状态" clearable>
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button @click="loadUsers">查询</el-button>
    </section>

    <section class="app-card panel-block">
      <el-table :data="users" v-loading="loading" stripe>
        <el-table-column prop="realName" label="姓名" min-width="120" />
        <el-table-column prop="username" label="账号" min-width="140" />
        <el-table-column prop="studentNo" label="学员编号" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="className" label="班级" min-width="140" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">{{ statusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="openEditDialog(scope.row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="createDialog.visible" title="新增学员" width="720px">
      <el-form label-position="top">
        <div class="dialog-grid two-col">
          <el-form-item label="姓名"><el-input v-model="createDialog.form.realName" /></el-form-item>
          <el-form-item label="登录账号"><el-input v-model="createDialog.form.username" /></el-form-item>
        </div>
        <div class="dialog-grid two-col">
          <el-form-item label="初始密码"><el-input v-model="createDialog.form.password" /></el-form-item>
          <el-form-item label="学员编号"><el-input v-model="createDialog.form.studentNo" /></el-form-item>
        </div>
        <div class="dialog-grid two-col">
          <el-form-item label="班级"><el-input v-model="createDialog.form.className" /></el-form-item>
          <el-form-item label="手机号"><el-input v-model="createDialog.form.phone" /></el-form-item>
        </div>
        <div class="dialog-grid two-col">
          <el-form-item label="邮箱"><el-input v-model="createDialog.form.email" /></el-form-item>
          <el-form-item label="状态"><el-switch v-model="createDialog.form.status" :active-value="1" :inactive-value="0" disabled /></el-form-item>
        </div>
        <el-form-item label="备注 / 简介"><el-input v-model="createDialog.form.bio" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="createDialog.saving" @click="submitCreate">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialog.visible" title="编辑学员" width="720px">
      <el-form label-position="top">
        <div class="dialog-grid two-col">
          <el-form-item label="姓名"><el-input v-model="editDialog.form.realName" /></el-form-item>
          <el-form-item label="登录账号"><el-input v-model="editDialog.form.username" disabled /></el-form-item>
        </div>
        <div class="dialog-grid two-col">
          <el-form-item label="新密码"><el-input v-model="editDialog.form.password" placeholder="不修改请留空" show-password /></el-form-item>
          <el-form-item label="学员编号"><el-input v-model="editDialog.form.studentNo" /></el-form-item>
        </div>
        <div class="dialog-grid two-col">
          <el-form-item label="班级"><el-input v-model="editDialog.form.className" /></el-form-item>
          <el-form-item label="账号状态"><el-switch v-model="editDialog.form.status" :active-value="1" :inactive-value="0" inline-prompt active-text="启用" inactive-text="停用" /></el-form-item>
        </div>
        <div class="dialog-grid two-col">
          <el-form-item label="手机号"><el-input v-model="editDialog.form.phone" /></el-form-item>
          <el-form-item label="邮箱"><el-input v-model="editDialog.form.email" /></el-form-item>
        </div>
        <el-form-item label="备注 / 简介"><el-input v-model="editDialog.form.bio" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="editDialog.saving" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.admin-user-page { gap: 18px; }
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
