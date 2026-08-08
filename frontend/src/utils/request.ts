import axios from 'axios'

// 统一的 axios 实例：所有接口请求都走这里
const request = axios.create({
  baseURL: '/api', // 开发环境由 vite 代理转发到后端 8080
  timeout: 10000,
})

// 请求拦截器：发请求前统一带上 token（后端从 Authorization 头取）
request.interceptors.request.use((config) => {
  const token = localStorage.getItem('community_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：收到响应后统一处理
request.interceptors.response.use(
  (response) => {
    // 约定后端统一返回 { code, message, data }，code === 0 表示成功
    const res = response.data
    if (res.code !== 0) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data
  },
  (error) => {
    // 未登录或登录过期：后端返回 HTTP 401，清掉本地登录信息，跳回登录页
    if (error.response?.status === 401) {
      localStorage.removeItem('community_token')
      localStorage.removeItem('community_user')
      if (!window.location.pathname.startsWith('/login')) {
        ElMessage.warning('请先登录')
        window.location.href = '/login'
      }
    } else {
      // 其他错误：网络不通、404、500 等
      ElMessage.error(error.response?.data?.message || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  },
)

export default request
