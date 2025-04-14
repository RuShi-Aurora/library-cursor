<template>
  <div class="user-container">
    <div class="header">
      <el-form :inline="true" :model="searchForm">
        <el-form-item>
          <el-input v-model="searchForm.keyword" placeholder="搜索用户名/邮箱" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </el-form-item>
      </el-form>
      
      <el-button type="primary" @click="handleAdd">
        新增用户
      </el-button>
    </div>

    <el-table :data="users" v-loading="loading">
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="email" label="邮箱" />
      <el-table-column prop="role" label="角色">
        <template #default="{ row }">
          <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'success'">
            {{ row.role === 'ADMIN' ? '管理员' : '读者' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="borrowCount" label="借阅记录">
        <template #default="{ row }">
          <el-tooltip v-if="row.borrowCount && row.borrowCount > 0" 
                      content="用户有借阅记录，请确保用户所有借书已归还后才能删除" 
                      placement="top">
            <el-tag type="warning">
              {{ row.borrowCount }} 条记录
              <el-icon class="ml-5"><InfoFilled /></el-icon>
            </el-tag>
          </el-tooltip>
          <span v-else>无记录</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button
            type="primary"
            size="small"
            @click="handleEdit(row)"
          >
            编辑
          </el-button>
          <el-button
            type="danger"
            size="small"
            @click="handleDelete(row)"
            :disabled="row.username === 'admin'"
          >
            删除
            <el-tooltip v-if="row.username === 'admin'" content="管理员账户不可删除" placement="top" effect="dark">
              <el-icon class="ml-5"><InfoFilled /></el-icon>
            </el-tooltip>
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      v-model:page-size="pageSize"
      :total="total"
      @current-change="handlePageChange"
    />

    <!-- 新增/编辑用户对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form
        :model="userForm"
        :rules="rules"
        ref="userFormRef"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="!!userForm.id" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!userForm.id">
          <el-input v-model="userForm.password" type="password" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="userForm.role">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="读者" value="USER" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import api from '../../api'

// 数据列表相关
const users = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索相关
const searchForm = reactive({
  keyword: ''
})

// 表单相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const userFormRef = ref(null)
const userForm = reactive({
  id: null,
  username: '',
  email: '',
  password: '',
  role: 'USER'
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

// 获取用户列表
const getUsers = async () => {
  loading.value = true
  try {
    const res = await api.get('/api/admin/users', {
      params: {
        page: page.value - 1,
        size: pageSize.value,
        keyword: searchForm.keyword
      }
    })
    users.value = res.content
    total.value = res.totalElements
  } catch (error) {
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 工具函数
const formatDate = (date) => {
  return new Date(date).toLocaleString()
}

// 事件处理
const handleSearch = () => {
  page.value = 1
  getUsers()
}

const handlePageChange = () => {
  getUsers()
}

const handleAdd = () => {
  dialogTitle.value = '新增用户'
  Object.assign(userForm, {
    id: null,
    username: '',
    email: '',
    password: '',
    role: 'USER'
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑用户'
  Object.assign(userForm, {
    id: row.id,
    username: row.username,
    email: row.email,
    role: row.role,
    password: ''
  })
  dialogVisible.value = true
}

const handleDelete = (row) => {
  // 不允许删除admin账户
  if (row.username === 'admin') {
    ElMessage.warning('不能删除管理员账户');
    return;
  }
  
  // 确认消息
  let confirmMsg = '确定要删除该用户吗？';
  if (row.borrowCount && row.borrowCount > 0) {
    confirmMsg = `此用户有${row.borrowCount}条借阅记录，系统将尝试删除已完成的记录。`;
    confirmMsg += '如果用户有未完成的借阅记录（待审核或已借出），则无法删除。';
    confirmMsg += '\n\n确定要继续吗？';
  }
  
  ElMessageBox.confirm(confirmMsg, '删除用户', {
    type: 'warning',
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    dangerouslyUseHTMLString: false
  }).then(async () => {
    try {
      loading.value = true;
      console.log(`开始删除用户 ${row.username}（ID: ${row.id}）`);
      
      const res = await api.delete(`/api/admin/users/${row.id}`);
      console.log('删除用户成功，响应:', res);
      
      ElMessage.success({
        message: res.message || '删除成功',
        duration: 3000
      });
      
      // 刷新用户列表
      getUsers();
    } catch (error) {
      console.error('删除用户失败:', error);
      console.error('错误详情:', error.response?.data);
      
      // 提取错误消息
      let errorMsg = '删除失败';
      if (error.response?.data?.message) {
        errorMsg = error.response.data.message;
      } else if (typeof error.response?.data === 'string') {
        errorMsg = error.response.data;
      }
      
      ElMessage.error({
        message: errorMsg,
        duration: 5000
      });
    } finally {
      loading.value = false;
    }
  }).catch(() => {
    console.log('用户取消删除操作');
  });
}

const handleSubmit = async () => {
  if (!userFormRef.value) return
  
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (userForm.id) {
          await api.put(`/api/admin/users/${userForm.id}`, userForm)
          ElMessage.success('更新成功')
        } else {
          await api.post('/api/admin/users', userForm)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        getUsers()
      } catch (error) {
        ElMessage.error(userForm.id ? '更新失败' : '添加失败')
      }
    }
  })
}

onMounted(() => {
  getUsers()
})
</script>

<style scoped>
.user-container {
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

.ml-5 {
  margin-left: 5px;
}
</style> 