import http from './http'

export const loginApi = (data) => http.post('/public/auth/login', data)
export const logoutApi = () => http.post('/public/auth/logout')
export const getProfileApi = () => http.get('/public/auth/me')
