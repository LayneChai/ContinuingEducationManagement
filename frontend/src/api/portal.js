import http from './http'

export const getMenusApi = () => http.get('/portal/menus')
export const getDashboardApi = () => http.get('/portal/dashboard')
