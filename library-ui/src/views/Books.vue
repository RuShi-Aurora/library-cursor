<template>
  <div class="books-container">
    <div class="books-header">
      <h2>图书管理</h2>
      <el-button type="primary" @click="handleAdd">添加图书</el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchQuery"
        placeholder="搜索图书名称或作者"
        clearable
        @clear="handleSearch"
        style="width: 300px"
      >
        <template #append>
          <el-button @click="handleSearch">
            <el-icon><Search /></el-icon>
          </el-button>
        </template>
      </el-input>
    </div>

    <!-- 图书列表 -->
    <el-table
      :data="books"
      style="width: 100%"
      v-loading="loading"
    >
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="书名" />
      <el-table-column prop="author" label="作者" />
      <el-table-column prop="isbn" label="ISBN" width="120" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'AVAILABLE' ? 'success' : 'danger'">
            {{ row.status === 'AVAILABLE' ? '可借' : '已借出' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button-group>
            <el-button size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button 
              size="small" 
              type="primary" 
              :disabled="row.status !== 'AVAILABLE'"
              @click="handleBorrow(row)"
            >
              借阅
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </el-button-group>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import api from '../api'

const router = useRouter()

// 数据
const books = ref([])
const loading = ref(false)
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 获取图书列表
const fetchBooks = async () => {
  loading.value = true
  try {
    const response = await api.get('/api/books', {
      params: {
        page: currentPage.value - 1,
        size: pageSize.value,
        keyword: searchQuery.value
      }
    })
    books.value = response.content
    total.value = response.totalElements
  } catch (error) {
    console.error('获取图书列表失败:', error)
    ElMessage.error('获取图书列表失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchBooks()
}

// 分页
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchBooks()
}

const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchBooks()
}

// 添加图书
const handleAdd = () => {
  router.push('/books/add')
}

// 编辑图书
const handleEdit = (row) => {
  router.push(`/books/${row.id}`)
}

// 借阅图书
const handleBorrow = async (row) => {
  try {
    await api.post(`/api/books/${row.id}/borrow`)
    ElMessage.success('借阅申请已提交')
    fetchBooks()
  } catch (error) {
    console.error('借阅失败:', error)
    ElMessage.error(error.response?.data?.message || '借阅失败')
  }
}

// 删除图书
const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确定要删除这本图书吗？',
    '警告',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await api.delete(`/api/books/${row.id}`)
      ElMessage.success('删除成功')
      fetchBooks()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 初始化
onMounted(() => {
  fetchBooks()
})
</script>

<style scoped>
.books-container {
  padding: 20px;
}

.books-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-bar {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 