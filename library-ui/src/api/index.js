import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:8080'
})

// 添加请求拦截器
api.interceptors.request.use(
  config => {
    // 获取token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    
    // 增强调试信息
    let paramsInfo = '';
    if (config.params) {
      paramsInfo = `参数: ${JSON.stringify(config.params)}`;
    } else if (config.data) {
      paramsInfo = `数据: ${JSON.stringify(config.data)}`;
    }
    
    console.log(`发送 ${config.method.toUpperCase()} 请求到: ${config.url} ${paramsInfo}`, config)
    
    return config
  },
  error => {
    console.error('请求发送错误:', error)
    return Promise.reject(error)
  }
)

// 添加响应拦截器，记录响应信息
api.interceptors.response.use(
  response => {
    console.log(`收到来自 ${response.config.url} 的响应:`, response.data)
    
    // 增强调试信息
    if (response.config.url.includes('/api/borrows')) {
      console.log('借阅API响应详情:', {
        url: response.config.url,
        method: response.config.method,
        params: response.config.params,
        data: response.data,
        headers: response.headers,
        status: response.status
      });
      
      // 分析数据结构
      if (response.data) {
        console.log('响应数据类型:', typeof response.data);
        if (typeof response.data === 'object') {
          console.log('响应对象结构:', Object.keys(response.data));
          
          // 如果有content属性，尝试分析第一条记录
          if (response.data.content && response.data.content.length > 0) {
            console.log('第一条记录示例:', response.data.content[0]);
          }
        }
      }
    }
    
    return response.data
  },
  error => {
    console.error('响应错误:', error)
    if (error.response) {
      console.error('响应状态:', error.response.status)
      console.error('响应数据:', error.response.data)
      
      // 处理401未授权错误
      if (error.response.status === 401) {
        console.log('用户未授权，即将跳转到登录页面')
        // 清除token
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default api 