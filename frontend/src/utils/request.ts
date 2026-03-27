import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

request.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const msg = error.response?.data?.message || '请求失败'
    return Promise.reject(new Error(msg))
  },
)

export default request
