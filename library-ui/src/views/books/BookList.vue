<template>
  <div class="book-container">
    <div class="header">
      <el-form :inline="true" :model="searchForm">
        <el-form-item>
          <el-input v-model="searchForm.keyword" placeholder="搜索书名" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="searchForm.category" placeholder="选择分类" clearable>
            <el-option
              v-for="item in categories"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </el-form-item>
      </el-form>
      
      <el-button type="primary" @click="handleAdd" v-if="isAdmin">
        新增图书
      </el-button>
    </div>

    <el-table :data="books" v-loading="loading">
      <el-table-column prop="title" label="书名" />
      <el-table-column prop="author" label="作者" />
      <el-table-column prop="isbn" label="ISBN" />
      <el-table-column prop="category" label="分类" />
      <el-table-column prop="stock" label="库存" />
      <el-table-column label="操作" width="250">
        <template #default="{ row }">
          <el-button
            v-if="!isAdmin"
            type="primary"
            size="small"
            @click="handleBorrow(row)"
            :disabled="row.stock <= 0"
          >
            借阅
          </el-button>
          <template v-else>
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
            >
              删除
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

    <!-- 新增/编辑图书对话框 -->
    <el-dialog
      :title="dialogTitle"
      v-model="dialogVisible"
      width="500px"
    >
      <el-form
        :model="bookForm"
        :rules="rules"
        ref="bookFormRef"
        label-width="80px"
      >
        <el-form-item label="书名" prop="title">
          <el-input v-model="bookForm.title" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="bookForm.author" />
        </el-form-item>
        <el-form-item label="ISBN" prop="isbn">
          <el-input v-model="bookForm.isbn" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="bookForm.category" placeholder="选择分类">
            <el-option
              v-for="item in categories"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="bookForm.stock" :min="0" />
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '../../store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '../../api'
import { useRouter } from 'vue-router'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.userInfo?.role === 'ADMIN')
const router = useRouter()

// 数据列表相关
const books = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 搜索相关
const searchForm = reactive({
  keyword: '',
  category: ''
})

// 分类列表
const categories = ['文学', '科技', '历史', '艺术', '其他']

// 获取图书列表
const getBooks = async () => {
  loading.value = true
  try {
    const res = await api.get('/api/books', {
      params: {
        page: page.value - 1,
        size: pageSize.value,
        keyword: searchForm.keyword,
        category: searchForm.category
      }
    })
    books.value = res.content
    total.value = res.totalElements
  } catch (error) {
    ElMessage.error('获取图书列表失败')
  } finally {
    loading.value = false
  }
}

// 表单相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const bookFormRef = ref(null)
const bookForm = reactive({
  id: null,
  title: '',
  author: '',
  isbn: '',
  category: '',
  stock: 0
})

const rules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
  isbn: [{ required: true, message: '请输入ISBN', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

// 事件处理
const handleSearch = () => {
  page.value = 1
  getBooks()
}

const handlePageChange = () => {
  getBooks()
}

const handleAdd = () => {
  dialogTitle.value = '新增图书'
  Object.assign(bookForm, {
    id: null,
    title: '',
    author: '',
    isbn: '',
    category: '',
    stock: 0
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑图书'
  Object.assign(bookForm, row)
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除《${row.title}》吗？删除后无法恢复。`, '删除图书', {
    type: 'warning',
    confirmButtonText: '确定删除',
    cancelButtonText: '取消'
  }).then(async () => {
    try {
      console.log(`开始删除图书: ${row.title}(ID: ${row.id})`);
      loading.value = true;
      
      const response = await api.delete(`/api/books/${row.id}`);
      console.log('删除图书成功:', response);
      
      ElMessage.success(response?.message || '删除成功');
      getBooks();
    } catch (error) {
      console.error('删除图书失败:', error);
      
      // 提取后端返回的错误信息
      let errorMsg = '删除失败';
      if (error.response?.data?.message) {
        errorMsg = error.response.data.message;
      } else if (typeof error.response?.data === 'string') {
        errorMsg = error.response.data;
      }
      
      // 如果是外键约束错误，添加维护功能引导
      if (errorMsg.includes('外键约束') || errorMsg.includes('关联的借阅记录')) {
        ElMessageBox.alert(
          `${errorMsg}<br><br>作为管理员，您可以通过<strong>系统维护</strong>功能修复外键约束。<br>
          <ol>
            <li>前往【管理后台】</li>
            <li>选择【系统维护】选项卡</li>
            <li>点击【修复数据库外键约束】按钮</li>
            <li>修复完成后再次尝试删除此图书</li>
          </ol>`,
          '删除失败 - 需要系统维护',
          {
            confirmButtonText: '知道了',
            dangerouslyUseHTMLString: true,
            type: 'warning'
          }
        ).then(() => {
          if (isAdmin.value) {
            ElMessageBox.confirm('是否立即前往系统维护页面？', '前往维护', {
              confirmButtonText: '是',
              cancelButtonText: '否',
              type: 'info'
            }).then(() => {
              router.push('/admin/maintenance');
            }).catch(() => {});
          }
        });
      } else {
        ElMessage.error({
          message: errorMsg,
          duration: 5000
        });
      }
    } finally {
      loading.value = false;
    }
  }).catch(() => {
    console.log('用户取消删除操作');
  });
}

const handleSubmit = async () => {
  if (!bookFormRef.value) return
  
  await bookFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (bookForm.id) {
          await api.put(`/api/books/${bookForm.id}`, bookForm)
          ElMessage.success('更新成功')
        } else {
          await api.post('/api/books', bookForm)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        getBooks()
      } catch (error) {
        ElMessage.error(bookForm.id ? '更新失败' : '添加失败')
      }
    }
  })
}

const handleBorrow = async (book) => {
  try {
    await api.post(`/api/borrows`, {
      bookId: book.id
    })
    ElMessage.success('借阅申请已提交')
    getBooks()
  } catch (error) {
    ElMessage.error('借阅失败')
  }
}

onMounted(() => {
  getBooks()
})
</script>

<style scoped>
.book-container {
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
</style> 