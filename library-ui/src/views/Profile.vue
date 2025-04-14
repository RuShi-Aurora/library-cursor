<template>
  <div class="profile-container">
    <div class="profile-header">
      <h2>个人中心</h2>
    </div>

    <el-form
      ref="profileFormRef"
      :model="profileForm"
      :rules="rules"
      label-width="100px"
      class="profile-form"
      v-loading="loading"
    >
      <el-form-item label="用户名">
        <el-input v-model="profileForm.username" disabled />
      </el-form-item>

      <el-form-item label="邮箱" prop="email">
        <el-input v-model="profileForm.email" :disabled="!isEdit" />
      </el-form-item>

      <el-form-item label="新密码" prop="newPassword" v-if="isEdit">
        <el-input
          v-model="profileForm.newPassword"
          type="password"
          show-password
          placeholder="不修改请留空"
        />
      </el-form-item>

      <el-form-item label="确认密码" prop="confirmPassword" v-if="isEdit">
        <el-input
          v-model="profileForm.confirmPassword"
          type="password"
          show-password
          placeholder="不修改请留空"
        />
      </el-form-item>

      <el-form-item>
        <el-button 
          v-if="!isEdit" 
          type="primary" 
          @click="handleEdit"
        >
          编辑
        </el-button>
        <template v-else>
          <el-button 
            type="primary" 
            @click="handleSubmit" 
            :loading="submitting"
          >
            保存
          </el-button>
          <el-button @click="handleCancel">取消</el-button>
        </template>
      </el-form-item>
    </el-form>

    <!-- 借阅历史 -->
    <div class="borrow-history">
      <h3>借阅历史</h3>
      <el-table
        :data="borrowRecords"
        style="width: 100%"
        v-loading="loadingRecords"
      >
        <el-table-column prop="bookTitle" label="图书名称" />
        <el-table-column prop="borrowDate" label="借阅时间">
          <template #default="{ row }">
            {{ formatDate(row.borrowDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="dueDate" label="到期时间">
          <template #default="{ row }">
            {{ formatDate(row.dueDate) }}
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
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'BORROWED'"
              type="primary"
              size="small"
              @click="handleReturn(row)"
            >
              归还
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import api from '../api'

const userStore = useUserStore()
const profileFormRef = ref(null)
const loading = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const loadingRecords = ref(false)

// 借阅记录分页
const borrowRecords = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 表单数据
const profileForm = reactive({
  username: '',
  email: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const rules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  newPassword: [
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    {
      validator: (rule, value, callback) => {
        if (profileForm.newPassword && value !== profileForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 获取用户信息
const fetchUserInfo = async () => {
  loading.value = true
  try {
    const response = await api.get('/api/users/profile')
    profileForm.username = response.username
    profileForm.email = response.email
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
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

// 获取借阅记录
const fetchBorrowRecords = async () => {
  loadingRecords.value = true
  try {
    const response = await api.get('/api/users/borrow-records', {
      params: {
        page: currentPage.value - 1,
        size: pageSize.value
      }
    })
    borrowRecords.value = response.content
    total.value = response.totalElements
  } catch (error) {
    console.error('获取借阅记录失败:', error)
    ElMessage.error('获取借阅记录失败')
  } finally {
    loadingRecords.value = false
  }
}

// 归还图书
const handleReturn = async (row) => {
  try {
    await api.put(`/api/borrows/${row.id}/return`)
    ElMessage.success('归还成功')
    fetchBorrowRecords()
  } catch (error) {
    console.error('归还失败:', error)
    ElMessage.error('归还失败: ' + (error.response?.data?.message || '未知错误'))
  }
}

// 编辑
const handleEdit = () => {
  isEdit.value = true
}

// 提交
const handleSubmit = async () => {
  if (!profileFormRef.value) return
  
  await profileFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        const updateData = {
          email: profileForm.email
        }
        
        if (profileForm.newPassword) {
          await api.put('/api/users/profile/password', {
            currentPassword: '',
            newPassword: profileForm.newPassword
          })
        }
        
        await api.put('/api/users/profile', updateData)
        
        ElMessage.success('保存成功')
        isEdit.value = false
        profileForm.newPassword = ''
        profileForm.confirmPassword = ''
        await fetchUserInfo()
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error(error.response?.data?.message || '保存失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 取消
const handleCancel = () => {
  isEdit.value = false
  profileForm.newPassword = ''
  profileForm.confirmPassword = ''
  fetchUserInfo()
}

// 分页处理
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchBorrowRecords()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchBorrowRecords()
}

// 初始化
onMounted(() => {
  fetchUserInfo()
  fetchBorrowRecords()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.profile-header {
  margin-bottom: 20px;
}

.profile-form {
  max-width: 500px;
  margin-bottom: 40px;
}

.borrow-history {
  margin-top: 30px;
}

.borrow-history h3 {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 