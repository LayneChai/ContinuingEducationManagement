import http from './http'

export const getTeacherAssignmentsApi = (params) => http.get('/teacher/assignments', { params })
export const createTeacherAssignmentApi = (data) => http.post('/teacher/assignments', data)
export const updateTeacherAssignmentApi = (assignmentId, data) => http.put(`/teacher/assignments/${assignmentId}`, data)
export const publishTeacherAssignmentApi = (assignmentId) => http.post(`/teacher/assignments/${assignmentId}/publish`)
export const getTeacherAssignmentDetailApi = (assignmentId) => http.get(`/teacher/assignments/${assignmentId}`)
export const getTeacherAssignmentSubmissionsApi = (assignmentId) => http.get(`/teacher/assignments/${assignmentId}/submissions`)
export const reviewTeacherSubmissionApi = (submissionId, data) => http.post(`/teacher/submissions/${submissionId}/review`, data)

export const getStudentAssignmentsApi = (params) => http.get('/student/assignments', { params })
export const getStudentAssignmentDetailApi = (assignmentId) => http.get(`/student/assignments/${assignmentId}`)
export const submitStudentAssignmentApi = (assignmentId, data) => http.post(`/student/assignments/${assignmentId}/submit`, data)
