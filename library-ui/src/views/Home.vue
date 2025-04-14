<template>
  <el-container class="layout-container">
    <!-- 头部 -->
    <el-header>
      <div class="header-left">
        <h2>图书管理系统</h2>
      </div>
      <div class="header-center">
        <el-menu mode="horizontal" router :default-active="$route.path">
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/books">图书管理</el-menu-item>
          <el-menu-item index="/profile">个人中心</el-menu-item>
          <el-menu-item v-if="userStore.isAdmin" index="/admin">管理后台</el-menu-item>
        </el-menu>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="el-dropdown-link">
            {{ userStore.userInfo?.username || '用户' }}
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人信息</el-dropdown-item>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    
    <!-- 主体内容 -->
    <el-main>
      <div class="home">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-card class="box-card">
              <template #header>
                <div class="card-header">
                  <span>图书总数</span>
                </div>
              </template>
              <div class="card-content">
                <el-statistic :value="statistics.totalBooks">
                  <template #title>
                    <div style="display: inline-flex; align-items: center">
                      图书
                      <el-icon style="margin-left: 4px">
                        <Collection />
                      </el-icon>
                    </div>
                  </template>
                </el-statistic>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="6">
            <el-card class="box-card">
              <template #header>
                <div class="card-header">
                  <span>借阅总数</span>
                </div>
              </template>
              <div class="card-content">
                <el-statistic :value="statistics.totalBorrows">
                  <template #title>
                    <div style="display: inline-flex; align-items: center">
                      借阅
                      <el-icon style="margin-left: 4px">
                        <Reading />
                      </el-icon>
                    </div>
                  </template>
                </el-statistic>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="6">
            <el-card class="box-card">
              <template #header>
                <div class="card-header">
                  <span>用户总数</span>
                </div>
              </template>
              <div class="card-content">
                <el-statistic :value="statistics.totalUsers">
                  <template #title>
                    <div style="display: inline-flex; align-items: center">
                      用户
                      <el-icon style="margin-left: 4px">
                        <User />
                      </el-icon>
                    </div>
                  </template>
                </el-statistic>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="6">
            <el-card class="box-card">
              <template #header>
                <div class="card-header">
                  <span>待处理请求</span>
                </div>
              </template>
              <div class="card-content">
                <el-statistic :value="statistics.pendingRequests">
                  <template #title>
                    <div style="display: inline-flex; align-items: center">
                      请求
                      <el-icon style="margin-left: 4px">
                        <Bell />
                      </el-icon>
                    </div>
                  </template>
                </el-statistic>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-row :gutter="20" style="margin-top: 20px">
          <el-col :span="12">
            <el-card class="box-card">
              <template #header>
                <div class="card-header">
                  <span>最近借阅</span>
                </div>
              </template>
              <el-table :data="recentBorrows" style="width: 100%" v-loading="loading">
                <el-table-column prop="bookTitle" label="图书" />
                <el-table-column prop="username" label="借阅人" />
                <el-table-column prop="borrowDate" label="借阅时间" />
                <el-table-column prop="status" label="状态">
                  <template #default="scope">
                    <el-tag :type="getBorrowStatusType(scope.row.status)">
                      {{ formatBorrowStatus(scope.row.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
              </el-table>
              <div v-if="recentBorrows.length === 0 && !loading" class="empty-data">
                暂无借阅记录
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="12">
            <el-card class="box-card">
              <template #header>
                <div class="card-header">
                  <span>热门图书</span>
                </div>
              </template>
              <el-table :data="popularBooks" style="width: 100%" v-loading="loading">
                <el-table-column prop="title" label="书名" />
                <el-table-column prop="author" label="作者" />
                <el-table-column prop="borrowCount" label="借阅次数" />
                <el-table-column prop="stock" label="库存" />
              </el-table>
              <div v-if="popularBooks.length === 0 && !loading" class="empty-data">
                暂无热门图书数据
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
    </el-main>
    
    <!-- 底部 -->
    <el-footer>
      <div class="footer-content">
        <p>© 2024 图书管理系统 - 版权所有</p>
      </div>
    </el-footer>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Collection, Reading, User, Bell } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'

const userStore = useUserStore()
const router = useRouter()

const statistics = ref({
  totalBooks: 0,
  totalBorrows: 0,
  totalUsers: 0,
  pendingRequests: 0
})

const recentBorrows = ref([])
const popularBooks = ref([])
const loading = ref(false)

const getBorrowStatusType = (status) => {
  const types = {
    'PENDING': 'warning',
    'BORROWED': 'primary',
    'RETURNED': 'success',
    'REJECTED': 'danger'
  }
  return types[status] || 'info'
}

const formatBorrowStatus = (status) => {
  const labels = {
    'PENDING': '待审核',
    'BORROWED': '借阅中',
    'RETURNED': '已归还',
    'REJECTED': '已拒绝'
  }
  return labels[status] || status
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleDateString('zh-CN')
}

const handleCommand = (command) => {
  if (command === 'logout') {
    handleLogout()
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

// 获取首页统计数据
const fetchStatistics = async () => {
  try {
    console.log("开始获取独立统计数据...");
    
    // 获取图书总数
    try {
      const booksResponse = await api.get('/api/books', { 
        params: { 
          page: 0, 
          size: 1 
        } 
      });
      console.log("图书API响应:", booksResponse);
      if (booksResponse && typeof booksResponse.totalElements === 'number') {
        statistics.value.totalBooks = booksResponse.totalElements;
        console.log("成功获取图书总数:", statistics.value.totalBooks);
      }
    } catch (error) {
      console.error("获取图书总数失败:", error);
    }
    
    // 获取借阅总数
    try {
      const borrowsResponse = await api.get('/api/borrows', { 
        params: { 
          page: 0, 
          size: 1 
        } 
      });
      console.log("借阅API响应:", borrowsResponse);
      if (borrowsResponse && typeof borrowsResponse.totalElements === 'number') {
        statistics.value.totalBorrows = borrowsResponse.totalElements;
        console.log("成功获取借阅总数:", statistics.value.totalBorrows);
      }
    } catch (error) {
      console.error("获取借阅总数失败:", error);
    }
    
    // 如果是管理员，尝试获取用户总数
    if (userStore.isAdmin) {
      try {
        const usersResponse = await api.get('/api/admin/users', { 
          params: { 
            page: 0, 
            size: 1 
          } 
        });
        console.log("用户API响应:", usersResponse);
        if (usersResponse && typeof usersResponse.totalElements === 'number') {
          statistics.value.totalUsers = usersResponse.totalElements;
          console.log("成功获取用户总数:", statistics.value.totalUsers);
        }
      } catch (error) {
        console.error("获取用户总数失败:", error);
      }
      
      // 尝试获取待处理请求数量
      try {
        const pendingResponse = await api.get('/api/borrows', { 
          params: { 
            page: 0, 
            size: 1,
            status: 'PENDING'
          } 
        });
        console.log("待处理借阅API响应:", pendingResponse);
        if (pendingResponse && typeof pendingResponse.totalElements === 'number') {
          statistics.value.pendingRequests = pendingResponse.totalElements;
          console.log("成功获取待处理请求数:", statistics.value.pendingRequests);
        }
      } catch (error) {
        console.error("获取待处理请求数失败:", error);
      }
    }
    
    console.log("所有统计数据获取完成:", statistics.value);
    
  } catch (error) {
    console.error("获取统计数据错误:", error);
    // 显示更友好的错误消息
    ElMessage.warning("统计数据加载失败，显示默认值");
  }
}

// 获取最近借阅记录
const fetchRecentBorrows = async () => {
  try {
    // 从借阅API获取最近的5条记录
    const response = await api.get('/api/borrows', {
      params: {
        page: 0,
        size: 5
      }
    })
    
    console.log("最近借阅记录API响应:", response)
    
    if (response && response.content) {
      // 处理每条记录的显示格式
      recentBorrows.value = response.content.map(item => ({
        bookTitle: item.book?.title || '-',
        username: item.user?.username || '-',
        borrowDate: formatDate(item.borrowDate),
        status: formatBorrowStatus(item.status)
      }))
    } else if (Array.isArray(response)) {
      // 如果直接返回数组
      recentBorrows.value = response.map(item => ({
        bookTitle: item.book?.title || '-',
        username: item.user?.username || '-',
        borrowDate: formatDate(item.borrowDate),
        status: formatBorrowStatus(item.status)
      }))
    } else {
      recentBorrows.value = []
    }
  } catch (error) {
    console.error("获取最近借阅记录错误:", error)
    ElMessage.error("获取最近借阅记录失败")
    recentBorrows.value = []
  }
}

// 获取热门图书
const fetchPopularBooks = async () => {
  try {
    // 获取按借阅次数排序的图书
    const response = await api.get('/api/books', {
      params: {
        page: 0,
        size: 5,
        sort: 'borrowCount,desc'
      }
    })
    
    console.log("热门图书API响应:", response)
    
    if (response && response.content) {
      popularBooks.value = response.content
    } else {
      popularBooks.value = []
    }
  } catch (error) {
    console.error("获取热门图书错误:", error)
    ElMessage.error("获取热门图书失败")
    popularBooks.value = []
  }
}

onMounted(async () => {
  loading.value = true
  try {
    console.log("Home页面加载，检查登录状态")
    
    if (!userStore.isLoggedIn) {
      console.log("未登录，重定向到登录页")
      router.push('/login')
      return
    }
    
    // 确保有用户信息
    if (!userStore.userInfo) {
      console.log("尝试获取用户信息")
      await userStore.fetchUserInfo()
    }
    
    console.log("当前用户:", userStore.userInfo)

    // 并行获取所有数据，但使用Promise.allSettled确保即使某个请求失败也不会影响其他请求
    const results = await Promise.allSettled([
      fetchStatistics(),
      fetchRecentBorrows(),
      fetchPopularBooks()
    ])
    
    // 检查各个请求的状态
    results.forEach((result, index) => {
      const apis = ['统计数据', '最近借阅', '热门图书']
      if (result.status === 'rejected') {
        console.error(`${apis[index]}请求失败:`, result.reason)
      } else {
        console.log(`${apis[index]}请求成功`)
      }
    })
    
    console.log("首页数据加载完成")
  } catch (error) {
    console.error("Home页面加载错误:", error)
    ElMessage.error("加载数据失败")
  } finally {
    loading.value = false
  }
})

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
}

.el-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
}

.header-left {
  width: 200px;
}

.header-center {
  flex-grow: 1;
}

.header-right {
  width: 150px;
  text-align: right;
}

.el-footer {
  text-align: center;
  background-color: #f5f7fa;
  padding: 20px 0;
}

.footer-content {
  color: #909399;
}

.home {
  padding: 20px;
}

.el-dropdown-link {
  cursor: pointer;
  display: flex;
  align-items: center;
}

.box-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-content {
  text-align: center;
  padding: 20px 0;
}

.el-row {
  margin-bottom: 20px;
}

.empty-data {
  text-align: center;
  color: #909399;
  padding: 20px 0;
  font-size: 14px;
}
</style> 