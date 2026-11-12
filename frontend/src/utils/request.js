import axios from 'axios'
import { ElMessage } from 'element-plus'
const request = axios.create({ baseURL: '/api', timeout: 15000 })
request.interceptors.request.use(cfg => {
  const token = localStorage.getItem('cem_token')
  if (token) cfg.headers.Authorization = 'Bearer ' + token
  return cfg
})
request.interceptors.response.use(
  res => {
    const data = res.data
    if (data && typeof data.code === 'number' && data.code !== 200) {
      ElMessage.error(data.msg || '请求失败')
      return Promise.reject(new Error(data.msg || '请求失败'))
    }
    return data
  },
  err => {
    if (err.response && err.response.status === 403) ElMessage.error('无权访问')
    else if (err.response && err.response.status === 401) {
      let wasStaff = false
      try { const u = JSON.parse(localStorage.getItem('cem_user') || 'null'); wasStaff = !!u && u.role !== 'FAMILY' } catch (e) {}
      localStorage.removeItem('cem_token'); localStorage.removeItem('cem_user')
      const path = (location.hash || '').replace(/^#/, '').split('?')[0]
      const guestPaths = ['', '/', '/home', '/memorial-wall', '/front-services', '/login', '/register', '/admin/login']
      if (!guestPaths.includes(path)) location.hash = wasStaff ? '#/admin/login' : '#/login?redirect=' + encodeURIComponent(path)
    } else ElMessage.error(err.message || '网络错误')
    return Promise.reject(err)
  }
)
export default request
