<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'

import {
  createAdminAiModelApi,
  deleteAdminAiModelApi,
  disableAdminAiModelApi,
  enableAdminAiModelApi,
  getAdminAiModelsApi,
  testAdminAiModelApi,
  updateAdminAiModelApi,
} from '../../../api/admin'

const models = ref([])
const loading = ref(false)

const dialog = reactive({
  visible: false,
  isEdit: false,
  saving: false,
  configId: null,
  form: defaultForm(),
})

const metrics = computed(() => [
  { label: '模型配置数', value: models.value.length },
  { label: '当前启用', value: models.value.filter((item) => item.enabled === 1).length },
  { label: '供应商数量', value: new Set(models.value.map((item) => item.providerName)).size },
  { label: '最近更新', value: models.value[0]?.displayName || '--' },
])

onMounted(loadModels)

function defaultForm() {
  return {
    providerName: 'DeepSeek',
    displayName: '',
    baseUrl: 'https://api.deepseek.com',
    apiKey: '',
    modelName: 'deepseek-chat',
    remark: '',
  }
}

async function loadModels() {
  loading.value = true
  try {
    models.value = await getAdminAiModelsApi()
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  dialog.visible = true
  dialog.isEdit = false
  dialog.configId = null
  dialog.form = defaultForm()
}

function openEditDialog(model) {
  dialog.visible = true
  dialog.isEdit = true
  dialog.configId = model.id
  dialog.form = {
    providerName: model.providerName,
    displayName: model.displayName,
    baseUrl: model.baseUrl,
    apiKey: '',
    modelName: model.modelName,
    remark: model.remark || '',
  }
}

async function submitForm() {
  dialog.saving = true
  try {
    if (dialog.isEdit) {
      await updateAdminAiModelApi(dialog.configId, dialog.form)
      ElMessage.success('模型配置已更新')
    } else {
      await createAdminAiModelApi(dialog.form)
      ElMessage.success('模型配置已新增')
    }
    dialog.visible = false
    await loadModels()
  } catch (error) {
    ElMessage.error(error.message || '保存失败')
  } finally {
    dialog.saving = false
  }
}

async function enableModel(model) {
  try {
    await enableAdminAiModelApi(model.id)
    ElMessage.success(`已启用模型：${model.displayName}`)
    await loadModels()
  } catch (error) {
    ElMessage.error(error.message || '启用失败')
  }
}

async function disableModel(model) {
  try {
    await disableAdminAiModelApi(model.id)
    ElMessage.success(`已停用模型：${model.displayName}`)
    await loadModels()
  } catch (error) {
    ElMessage.error(error.message || '停用失败')
  }
}

async function deleteModel(model) {
  try {
    if (model.enabled === 1) {
      ElMessage.warning('请先停用当前模型，再执行删除')
      return
    }
    await ElMessageBox.confirm(`确定删除模型配置「${model.displayName}」吗？`, '删除确认', { type: 'warning' })
    await deleteAdminAiModelApi(model.id)
    ElMessage.success('模型配置已删除')
    await loadModels()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

async function testModel(model) {
  try {
    const result = await testAdminAiModelApi(model.id)
    if (result.success) {
      ElMessage.success(result.message || '连接测试成功')
    } else {
      ElMessage.error(result.message || '连接测试失败')
    }
  } catch (error) {
    ElMessage.error(error.message || '连接测试失败')
  }
}
</script>

<template>
  <div class="page-wrap admin-ai-model-page">
    <div class="page-heading">
      <div>
        <h1>模型配置</h1>
        <p>管理员可维护多个大模型配置，但系统同一时间只允许启用一个模型用于 AI 辅导。</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">新增模型</el-button>
    </div>

    <section class="stat-grid">
      <article v-for="item in metrics" :key="item.label" class="app-card stat-card">
        <div class="label">{{ item.label }}</div>
        <div class="value">{{ item.value }}</div>
      </article>
    </section>

    <section class="app-card panel-block">
      <el-alert
        title="启用规则：同一时间只能启用一个模型配置，启用新模型时系统会自动停用旧模型。"
        type="info"
        :closable="false"
        show-icon
      />
      <el-alert
        title="删除规则：已启用模型不可直接删除，请先停用后再删除。"
        type="warning"
        :closable="false"
        show-icon
        style="margin-top: 12px"
      />

      <el-table :data="models" v-loading="loading" stripe style="margin-top: 16px">
        <el-table-column prop="displayName" label="显示名称" min-width="160" />
        <el-table-column prop="providerName" label="服务商" min-width="120" />
        <el-table-column prop="modelName" label="模型名称" min-width="160" />
        <el-table-column prop="baseUrl" label="接口地址" min-width="220" />
        <el-table-column prop="apiKeyMasked" label="密钥" min-width="150" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.enabled === 1 ? 'success' : 'info'">
              {{ scope.row.enabled === 1 ? '已启用' : '未启用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" min-width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button text @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button text @click="testModel(scope.row)">测试</el-button>
            <el-button
              v-if="scope.row.enabled === 1"
              text
              type="warning"
              @click="disableModel(scope.row)"
            >停用</el-button>
            <el-button
              v-else
              text
              type="primary"
              @click="enableModel(scope.row)"
            >启用</el-button>
            <el-button
              text
              type="danger"
              :disabled="scope.row.enabled === 1"
              @click="deleteModel(scope.row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <el-dialog v-model="dialog.visible" :title="dialog.isEdit ? '编辑模型配置' : '新增模型配置'" width="720px">
      <el-form label-position="top">
        <div class="dialog-grid two-col">
          <el-form-item label="服务商名称"><el-input v-model="dialog.form.providerName" /></el-form-item>
          <el-form-item label="显示名称"><el-input v-model="dialog.form.displayName" placeholder="例如：DeepSeek 正式环境" /></el-form-item>
        </div>
        <div class="dialog-grid two-col">
          <el-form-item label="接口地址"><el-input v-model="dialog.form.baseUrl" /></el-form-item>
          <el-form-item label="模型名称"><el-input v-model="dialog.form.modelName" /></el-form-item>
        </div>
        <el-form-item label="API Key">
          <el-input v-model="dialog.form.apiKey" type="password" show-password :placeholder="dialog.isEdit ? '留空则保留原密钥' : '请输入模型密钥'" />
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="dialog.form.remark" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="dialog.saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.admin-ai-model-page { gap: 18px; }
.dialog-grid { display: grid; gap: 14px; }
.two-col { grid-template-columns: repeat(2, 1fr); }
@media (max-width: 760px) { .two-col { grid-template-columns: 1fr; } }
</style>
