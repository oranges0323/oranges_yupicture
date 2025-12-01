import type { AxiosInstance } from 'axios'
import axios from 'axios'
import { message } from 'ant-design-vue'
// 区分开发和生产环境
const DEV_BASE_URL = "http://localhost:8123";
const PROD_BASE_URL = "http://111.229.125.52";
// 创建 Axios 实例
const myAxios = axios.create({
  // 这里打包上传到云需要修改
  // baseURL: PROD_BASE_URL,
  baseURL: DEV_BASE_URL,
  timeout: 10000,
  withCredentials: true,
});

// 全局请求拦截器
myAxios.interceptors.request.use(
  function (config) {
    // 请求日志
    console.log('发送请求：', {
      url: config.baseURL + config.url,
      method: config.method,
      data: config.data,
      params: config.params
    })
    return config
  },
  function (error) {
    console.error('请求错误：', error)
    return Promise.reject(error)
  },
)

// 全局响应拦截器
myAxios.interceptors.response.use(
  function (response) {
    const { data } = response
    // 响应日志
    console.log('收到响应：', {
      url: response.config.url,
      status: response.status,
      data: data
    })
    // 未登录
    if (data.code === 40100) {
      // 不是获取用户信息的请求，并且用户目前不是已经在用户登录页面，则跳转到登录页面
      if (
        !response.request.responseURL.includes('user/get/login') &&
        !window.location.pathname.includes('/user/login')
      ) {
        message.warning('请先登录')
        window.location.href = `/user/login?redirect=${window.location.href}`
      }
    }
    return response
  },
  function (error) {
    // 响应错误日志
    console.error('响应错误：', {
      message: error.message,
      status: error.response?.status,
      data: error.response?.data
    })
    // Any status codes that falls outside the range of 2xx cause this function to trigger
    // Do something with response error
    return Promise.reject(error)
  },
)

export default myAxios
