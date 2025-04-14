import { defineStore } from 'pinia'
import api from '../api'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo')) || null
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    isAdmin: (state) => state.userInfo?.role === 'ADMIN'
  },

  actions: {
    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`
    },

    setUserInfo(userInfo) {
      this.userInfo = userInfo
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },

    async login(username, password) {
      try {
        console.log("准备登录请求:", { username, password });
        const response = await api.post('/api/auth/login', { 
          username, 
          password 
        });
        
        console.log("登录响应:", response);
        
        if (response && response.token) {
          this.setToken(response.token);
          await this.fetchUserInfo();
          return true;
        } else {
          console.error("登录响应格式不正确:", response);
          ElMessage.error("服务器响应格式不正确");
          return false;
        }
      } catch (error) {
        console.error('登录失败:', error);
        throw error;
      }
    },

    async register(userData) {
      try {
        const response = await api.post('/api/auth/register', userData);
        console.log("注册响应:", response);
        return true;
      } catch (error) {
        console.error('注册失败:', error);
        throw error;
      }
    },

    async fetchUserInfo() {
      try {
        if (!this.token) {
          console.warn("尝试获取用户信息但没有token");
          return false;
        }
        
        console.log("获取用户信息，当前token:", this.token);
        const response = await api.get('/api/auth/user-info');
        console.log("用户信息响应:", response);
        
        if (response) {
          this.setUserInfo(response);
          return true;
        }
        return false;
      } catch (error) {
        console.error('获取用户信息失败:', error);
        // 如果是401错误，则登出
        if (error.response && error.response.status === 401) {
          this.logout();
        }
        throw error;
      }
    },

    logout() {
      this.token = '';
      this.userInfo = null;
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
      api.defaults.headers.common['Authorization'] = '';
    }
  }
}) 