<template>
  <div class="borrow-container">
    <div class="header">
      <el-form :inline="true" :model="searchForm">
        <el-form-item>
          <el-select v-model="searchForm.status" placeholder="借阅状态" clearable @change="handleStatusChange">
            <el-option
              v-for="item in statusOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 管理员可以新增借阅 -->
      <el-button v-if="isAdmin" type="primary" @click="handleAddBorrow">
        新增借阅
      </el-button>
    </div>

    <el-table :data="borrows" v-loading="loading">
      <el-table-column label="书名">
        <template #default="{ row }">
          {{ row.book ? row.book.title : (row.bookTitle || '-') }}
        </template>
      </el-table-column>
      <el-table-column label="借阅人">
        <template #default="{ row }">
          {{ row.user ? row.user.username : (row.username || '-') }}
        </template>
      </el-table-column>
      <el-table-column prop="borrowDate" label="借阅时间">
        <template #default="{ row }">
          {{ formatDate(row.borrowDate) }}
        </template>
      </el-table-column>
      <el-table-column prop="dueDate" label="到期时间">
        <template #default="{ row }">
          {{ row.dueDate ? formatDate(row.dueDate) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="returnDate" label="归还时间">
        <template #default="{ row }">
          {{ row.returnDate ? formatDate(row.returnDate) : '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <template v-if="isAdmin">
            <el-button
              v-if="row.status === 'PENDING'"
              type="success"
              size="small"
              @click="handleApprove(row)"
            >
              通过
            </el-button>
            <el-button
              v-if="row.status === 'PENDING'"
              type="danger"
              size="small"
              @click="handleReject(row)"
            >
              拒绝
            </el-button>
            <el-button
              v-if="row.status === 'BORROWED'"
              type="primary"
              size="small"
              @click="handleReturn(row)"
            >
              归还
            </el-button>
          </template>
          <template v-else>
            <el-button
              v-if="row.status === 'BORROWED'"
              type="primary"
              size="small"
              @click="handleReturn(row)"
              :disabled="row.user && row.user.username !== userStore.userInfo.username"
            >
              归还
            </el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      v-model:page-size="pageSize"
      :total="total"
      @current-change="handlePageChange"
    />

    <!-- 调试信息区域 -->
    <div style="margin-top: 20px; text-align: right;">
      <el-button type="info" size="small" @click="showDebug = !showDebug">
        {{ showDebug ? '隐藏调试信息' : '显示调试信息' }}
      </el-button>
    </div>
    <el-collapse-transition>
      <div v-if="showDebug" class="debug-panel">
        <h3>API请求信息</h3>
        <pre>接口URL: /api/borrows</pre>
        <pre>请求参数: {{ JSON.stringify({
          page: page - 1,
          size: pageSize,
          status: searchForm.status || '全部(无筛选)'
        }, null, 2) }}</pre>
        <h3>状态筛选器</h3>
        <pre>当前选择的状态: {{ searchForm.status || '全部(无筛选)' }}</pre>
        <pre>可选状态列表: {{ JSON.stringify(statusOptions.map(o => o.value), null, 2) }}</pre>
        <h3>原始响应</h3>
        <pre>{{ JSON.stringify(apiResponse, null, 2) }}</pre>
        <h3>处理后的数据</h3>
        <pre>记录总数: {{ total }}</pre>
        <pre>当前显示记录: {{ borrows.length }}</pre>
        <pre>记录状态分布: {{ countStatuses() }}</pre>
      </div>
    </el-collapse-transition>

    <!-- 新增借阅对话框 -->
    <el-dialog
      title="新增借阅"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form
        :model="borrowForm"
        :rules="rules"
        ref="borrowFormRef"
        label-width="80px"
      >
        <el-form-item label="书籍" prop="bookId">
          <el-select v-model="borrowForm.bookId" filterable placeholder="选择书籍" remote :remote-method="searchBooks" :loading="booksLoading">
            <el-option
              v-for="item in books"
              :key="item.id"
              :label="item.title + ' - ' + item.author"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="借阅人" prop="username">
          <el-select v-model="borrowForm.username" filterable placeholder="选择用户" remote :remote-method="searchUsers" :loading="usersLoading">
            <el-option
              v-for="item in users"
              :key="item.id"
              :label="item.username"
              :value="item.username"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBorrow">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '../../stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.userInfo?.role === 'ADMIN')

// 数据列表相关
const borrows = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)
const apiResponse = ref(null) // 添加用于存储原始API响应的变量
const showDebug = ref(false) // 控制调试信息显示

// 搜索相关
const searchForm = reactive({
  status: ''
})

// 新增借阅相关
const dialogVisible = ref(false)
const borrowFormRef = ref(null)
const borrowForm = reactive({
  bookId: null,
  username: ''
})
const books = ref([])
const users = ref([])
const booksLoading = ref(false)
const usersLoading = ref(false)

// 规则
const rules = {
  bookId: [{ required: true, message: '请选择书籍', trigger: 'change' }],
  username: [{ required: true, message: '请选择借阅人', trigger: 'change' }]
}

// 状态选项
const statusOptions = [
  { label: '待审核', value: 'PENDING' },
  { label: '已借出', value: 'BORROWED' },
  { label: '已归还', value: 'RETURNED' },
  { label: '已拒绝', value: 'REJECTED' }
]

// 统计当前记录中各状态的数量
const countStatuses = () => {
  const statusCount = {
    PENDING: 0,
    BORROWED: 0,
    RETURNED: 0,
    REJECTED: 0,
    UNKNOWN: 0
  }
  
  borrows.value.forEach(record => {
    if (record.status && statusCount.hasOwnProperty(record.status)) {
      statusCount[record.status]++
    } else {
      statusCount.UNKNOWN++
    }
  })
  
  return statusCount
}

// 获取借阅列表
const getBorrows = async () => {
  loading.value = true
  try {
    // 构建更明确的请求参数
    const params = {
      page: page.value - 1,  // 注意：后端接收0-based页码
      size: pageSize.value
    }
    
    // 只有当status有值且不为空时才添加到请求参数中
    if (searchForm.status && searchForm.status.trim() !== '') {
      params.status = searchForm.status
    }
    
    console.log('正在请求借阅数据，参数:', params)
    
    // 记录精确的API请求URL
    const requestUrl = '/api/borrows'
    console.log(`完整请求URL: ${requestUrl}，参数:`, params)
    
    const res = await api.get(requestUrl, { params })
    
    // 输出API完整响应
    console.log('API原始响应对象:', res)
    console.log('响应数据类型:', typeof res)
    
    if (res) {
      console.log('响应数据结构:', Object.keys(res))
    }
    
    apiResponse.value = res // 保存原始响应
    
    // 处理不同的响应结构情况
    if (Array.isArray(res)) {
      console.log('API返回了数组格式数据', res)
      borrows.value = res
      total.value = res.length
    } else if (res && res.content && Array.isArray(res.content)) {
      console.log('API返回了分页对象格式数据', res)
      borrows.value = res.content
      total.value = res.totalElements || res.totalPages * res.size || res.content.length
      
      // 检查是否有状态不匹配的记录
      if (searchForm.status && searchForm.status.trim() !== '') {
        const mismatchedRecords = res.content.filter(r => r.status !== searchForm.status)
        if (mismatchedRecords.length > 0) {
          console.warn(`警告：有${mismatchedRecords.length}条记录的状态与筛选条件不匹配`, mismatchedRecords)
        }
      }
    } else if (res && typeof res === 'object') {
      console.log('API返回了其他对象格式数据，尝试解析', res)
      // 尝试查找任何可能的数组数据
      const potentialArrays = Object.values(res).filter(v => Array.isArray(v))
      if (potentialArrays.length > 0) {
        console.log('从响应中提取到数组数据', potentialArrays[0])
        borrows.value = potentialArrays[0]
        total.value = borrows.value.length
      } else {
        console.log('无法从响应中提取有效数据')
        borrows.value = []
        total.value = 0
      }
    } else {
      console.log('API返回了未知格式数据', res)
      borrows.value = []
      total.value = 0
    }
    
    console.log('处理后的借阅记录数据:', borrows.value)
    console.log('记录状态分布:', countStatuses())
    console.log('总记录数:', total.value)
  } catch (error) {
    console.error('获取借阅列表失败:', error)
    if (error.response) {
      console.error('错误状态码:', error.response.status)
      console.error('错误数据:', error.response.data)
      console.error('错误响应头:', error.response.headers)
    } else if (error.request) {
      console.error('请求已发送但未收到响应:', error.request)
    } else {
      console.error('请求配置错误:', error.message)
    }
    
    ElMessage.error('获取借阅列表失败: ' + (error.response?.data?.message || error.message || '未知错误'))
    borrows.value = []
    total.value = 0
    apiResponse.value = error.response?.data || error.message
  } finally {
    loading.value = false
  }
}

// 搜索书籍
const searchBooks = async (query) => {
  if (query === '') return
  booksLoading.value = true
  try {
    const res = await api.get('/api/books', {
      params: {
        keyword: query,
        page: 0,
        size: 20
      }
    })
    books.value = res.content
  } catch (error) {
    console.error('搜索书籍失败:', error)
  } finally {
    booksLoading.value = false
  }
}

// 搜索用户
const searchUsers = async (query) => {
  if (query === '') return
  usersLoading.value = true
  try {
    const res = await api.get('/api/admin/users', {
      params: {
        keyword: query,
        page: 0,
        size: 20
      }
    })
    users.value = res.content
  } catch (error) {
    console.error('搜索用户失败:', error)
  } finally {
    usersLoading.value = false
  }
}

// 工具函数
const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleString()
}

const getStatusType = (status) => {
  const map = {
    PENDING: 'warning',
    BORROWED: 'primary',
    RETURNED: 'success',
    REJECTED: 'danger'
  }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = {
    PENDING: '待审核',
    BORROWED: '借阅中',
    RETURNED: '已归还',
    REJECTED: '已拒绝'
  }
  return map[status] || status
}

// 事件处理
const handleStatusChange = (value) => {
  console.log('状态选择已改变:', value)
  // 状态变更时立即触发搜索
  handleSearch()
}

const handleSearch = () => {
  console.log('执行搜索，状态筛选值:', searchForm.status)
  
  // 重置为第一页
  page.value = 1
  
  // 调用获取数据方法
  getBorrows()
  
  // 更新调试面板显示
  if (showDebug.value) {
    console.log('搜索参数:', {
      page: page.value - 1,
      size: pageSize.value,
      status: searchForm.status || '全部'
    })
  }
}

const handlePageChange = () => {
  getBorrows()
}

const handleAddBorrow = () => {
  dialogVisible.value = true
  borrowForm.bookId = null
  borrowForm.username = ''
}

const submitBorrow = async () => {
  if (!borrowFormRef.value) return
  
  await borrowFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await api.post('/api/borrows/admin', borrowForm)
        ElMessage.success('借阅申请已提交')
        dialogVisible.value = false
        getBorrows()
      } catch (error) {
        console.error('新增借阅失败:', error)
        ElMessage.error(error.response?.data?.message || '新增借阅失败')
      }
    }
  })
}

const handleApprove = async (row) => {
  try {
    await api.put(`/api/borrows/${row.id}/approve`)
    ElMessage.success('审核通过')
    getBorrows()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败: ' + (error.response?.data?.message || '未知错误'))
  }
}

const handleReject = async (row) => {
  try {
    await api.put(`/api/borrows/${row.id}/reject`)
    ElMessage.success('已拒绝')
    getBorrows()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败: ' + (error.response?.data?.message || '未知错误'))
  }
}

const handleReturn = async (row) => {
  try {
    console.log('开始执行归还操作，记录详情:', row);
    console.log('当前用户角色:', isAdmin.value ? '管理员' : '普通用户');
    console.log('当前用户信息:', userStore.userInfo);
    
    if (!row.id) {
      console.error('归还失败: 记录ID不存在');
      ElMessage.error('归还失败: 借阅记录ID不存在');
      return;
    }
    
    // 根据用户角色选择不同的API路径
    let apiUrl = `/api/borrows/${row.id}/return`;
    
    // 修复管理员API路径格式
    if (isAdmin.value) {
      // 注意：正确的格式应该是 /api/borrows/admin/{id}/return
      apiUrl = `/api/borrows/admin/${row.id}/return`;
      console.log('使用管理员专用API:', apiUrl);
    }
    
    // 管理员操作需要确认
    if (isAdmin.value) {
      try {
        await ElMessageBox.confirm(
          `确定要代替用户 ${row.user?.username || '未知用户'} 归还《${row.book?.title || '此图书'}》吗？`, 
          '管理员操作确认', 
          {
            confirmButtonText: '确定归还',
            cancelButtonText: '取消',
            type: 'warning'
          }
        );
        console.log('管理员确认执行归还操作');
      } catch (e) {
        console.log('管理员取消了归还操作');
        return;
      }
    }
    
    console.log(`发送请求: PUT ${apiUrl}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      },
      user: userStore.userInfo
    });
    
    const res = await api.put(apiUrl);
    console.log('归还操作成功，服务器响应:', res);
    
    ElMessage.success('归还成功');
    getBorrows();
  } catch (error) {
    console.error('归还失败:', error);
    console.error('请求配置:', error.config);
    if (error.response) {
      console.error('错误状态码:', error.response.status);
      console.error('错误数据:', error.response.data);
      console.error('响应头:', error.response.headers);
    } else if (error.request) {
      console.error('未收到响应的请求:', error.request);
    }
    
    // 显示具体错误信息
    let errorMsg = '未知错误';
    if (error.response?.data?.message) {
      errorMsg = error.response.data.message;
    } else if (typeof error.response?.data === 'string') {
      errorMsg = error.response.data;
    } else if (error.message) {
      errorMsg = error.message;
    }
    
    ElMessage.error('归还失败: ' + errorMsg);
  }
}

onMounted(() => {
  getBorrows()
})
</script>

<style scoped>
.borrow-container {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.el-pagination {
  margin-top: 20px;
  justify-content: flex-end;
}

.debug-panel {
  margin-top: 10px;
  padding: 15px;
  background-color: #f8f8f8;
  border: 1px solid #ddd;
  border-radius: 4px;
  text-align: left;
}

.debug-panel pre {
  background-color: #fff;
  padding: 8px;
  border-radius: 4px;
  margin: 5px 0;
  overflow-x: auto;
  max-height: 200px;
}

.debug-panel h3 {
  margin-top: 15px;
  margin-bottom: 5px;
  font-size: 14px;
  color: #333;
}
</style> 