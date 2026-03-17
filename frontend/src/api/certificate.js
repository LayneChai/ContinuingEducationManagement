import http from './http'

export const getStudentCertificatesApi = () => http.get('/student/certificates')
export const applyStudentCertificateApi = (courseId, data) => http.post(`/student/certificates/courses/${courseId}/apply`, data)

export const getAdminCertificatesApi = (params) => http.get('/admin/certificates', { params })
export const reviewAdminCertificateApi = (applyId, data) => http.post(`/admin/certificates/${applyId}/review`, data)

export const getTeacherCertificatesApi = (params) => http.get('/teacher/certificates', { params })
export const reviewTeacherCertificateApi = (applyId, data) => http.post(`/teacher/certificates/${applyId}/review`, data)
