import http from './http'

export const getTeacherQuestionsApi = (courseId) => http.get('/teacher/questions', { params: { courseId } })
export const createTeacherQuestionApi = (data) => http.post('/teacher/questions', data)
export const updateTeacherQuestionApi = (questionId, data) => http.put(`/teacher/questions/${questionId}`, data)
export const deleteTeacherQuestionApi = (questionId) => http.delete(`/teacher/questions/${questionId}`)

export const getTeacherExamsApi = (params) => http.get('/teacher/exams', { params })
export const createTeacherExamApi = (data) => http.post('/teacher/exams', data)
export const updateTeacherExamApi = (examId, data) => http.put(`/teacher/exams/${examId}`, data)
export const getTeacherExamDetailApi = (examId) => http.get(`/teacher/exams/${examId}`)
export const publishTeacherExamApi = (examId) => http.post(`/teacher/exams/${examId}/publish`)

export const getStudentExamsApi = (params) => http.get('/student/exams', { params })
export const getStudentExamDetailApi = (examId) => http.get(`/student/exams/${examId}`)
export const submitStudentExamApi = (examId, data) => http.post(`/student/exams/${examId}/submit`, data)
