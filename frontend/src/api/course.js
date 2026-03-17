import http from './http'

export const getCourseCategoriesApi = () => http.get('/public/course/categories')

export const getTeacherCoursesApi = (params) => http.get('/teacher/courses', { params })
export const getTeacherCourseDetailApi = (courseId) => http.get(`/teacher/courses/${courseId}`)
export const createTeacherCourseApi = (data) => http.post('/teacher/courses', data)
export const updateTeacherCourseApi = (courseId, data) => http.put(`/teacher/courses/${courseId}`, data)
export const submitTeacherCourseAuditApi = (courseId) => http.post(`/teacher/courses/${courseId}/submit-audit`)
export const createChapterApi = (courseId, data) => http.post(`/teacher/courses/${courseId}/chapters`, data)
export const updateChapterApi = (courseId, chapterId, data) => http.put(`/teacher/courses/${courseId}/chapters/${chapterId}`, data)
export const deleteChapterApi = (courseId, chapterId) => http.delete(`/teacher/courses/${courseId}/chapters/${chapterId}`)
export const createLessonApi = (courseId, data) => http.post(`/teacher/courses/${courseId}/lessons`, data)
export const updateLessonApi = (courseId, lessonId, data) => http.put(`/teacher/courses/${courseId}/lessons/${lessonId}`, data)
export const deleteLessonApi = (courseId, lessonId) => http.delete(`/teacher/courses/${courseId}/lessons/${lessonId}`)

export const getPublicCoursesApi = (params) => http.get('/public/courses', { params })
export const getPublicCourseDetailApi = (courseId) => http.get(`/public/courses/${courseId}`)
export const enrollCourseApi = (courseId) => http.post(`/student/courses/${courseId}/enroll`)
export const getStudentCoursesApi = (params) => http.get('/student/courses', { params })
export const studyLessonApi = (courseId, lessonId, data) => http.post(`/student/courses/${courseId}/lessons/${lessonId}/study`, data)
export const getLearningOverviewApi = (courseId) => http.get(`/student/courses/${courseId}/learning-overview`)
