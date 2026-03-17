import http from './http'

export const getAdminUsersApi = (params) => http.get('/admin/users', { params })
export const createAdminUserApi = (data) => http.post('/admin/users', data)
export const getAdminRolesApi = () => http.get('/admin/roles')

export const getAdminCoursesApi = (params) => http.get('/admin/courses', { params })
export const getAdminCourseDetailApi = (courseId) => http.get(`/admin/courses/${courseId}`)
export const auditAdminCourseApi = (courseId, data) => http.post(`/admin/courses/${courseId}/audit`, data)

export const getAdminAiModelsApi = () => http.get('/admin/ai-models')
export const createAdminAiModelApi = (data) => http.post('/admin/ai-models', data)
export const updateAdminAiModelApi = (configId, data) => http.put(`/admin/ai-models/${configId}`, data)
export const enableAdminAiModelApi = (configId) => http.post(`/admin/ai-models/${configId}/enable`)
export const deleteAdminAiModelApi = (configId) => http.delete(`/admin/ai-models/${configId}`)
export const testAdminAiModelApi = (configId) => http.post(`/admin/ai-models/${configId}/test`)
