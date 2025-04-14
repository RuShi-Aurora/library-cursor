<template>
  <div class="book-detail-container">
    <div class="form-header">
      <h2>{{ isEdit ? '编辑图书' : '图书详情' }}</h2>
    </div>

    <el-form
      ref="bookFormRef"
      :model="bookForm"
      :rules="rules"
      label-width="100px"
      class="book-form"
      v-loading="loading"
    >
      <el-form-item label="书名" prop="title">
        <el-input v-model="bookForm.title" :disabled="!isEdit" />
      </el-form-item>

      <el-form-item label="作者" prop="author">
        <el-input v-model="bookForm.author" :disabled="!isEdit" />
      </el-form-item>

      <el-form-item label="ISBN" prop="isbn">
        <el-input v-model="bookForm.isbn" :disabled="!isEdit" />
      </el-form-item>

      <el-form-item label="出版社" prop="publisher">
        <el-input v-model="bookForm.publisher" :disabled="!isEdit" />
      </el-form-item>

      <el-form-item label="出版日期" prop="publishDate">
        <el-date-picker
          v-model="bookForm.publishDate"
          type="date"
          :disabled="!isEdit"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>

      <el-form-item label="分类" prop="category">
        <el-select v-model="bookForm.category" :disabled="!isEdit">
          <el-option label="文学" value="LITERATURE" />
          <el-option label="科技" value="TECHNOLOGY" />
          <el-option label="艺术" value="ART" />
          <el-option label="历史" value="HISTORY" />
          <el-option label="其他" value="OTHER" />
        </el-select>
      </el-form-item>

      <el-form-item label="简介" prop="description">
        <el-input
          v-model="bookForm.description"
          type="textarea"
          rows="4"
          :disabled="!isEdit"
        />
      </el-form-item>

      <el-form-item label="库存数量" prop="stock">
        <el-input-number
          v-model="bookForm.stock"
          :min="0"
          :max="999"
          :disabled="!isEdit"
        />
      </el-form-item>

      <el-form-item label="状态">
        <el-tag :type="bookForm.status === 'AVAILABLE' ? 'success' : 'danger'">
          {{ bookForm.status === 'AVAILABLE' ? '可借' : '已借出' }}
        </el-tag>
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
        <el-button @click="handleBack">返回</el-button>
      </el-form-item>
    </el-form>

    <!-- 借阅记录 -->
    <div class="borrow-records" v-if="borrowRecords.length > 0">
      <h3>借阅记录</h3>
      <el-table :data="borrowRecords" style="width: 100%">
        <el-table-column prop="borrower" label="借阅人" />
        <el-table-column prop="borrowDate" label="借阅时间" />
        <el-table-column prop="returnDate" label="归还时间" />
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="row.status === 'RETURNED' ? 'success' : 'warning'">
              {{ row.status === 'RETURNED' ? '已归还' : '借阅中' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'

const route = useRoute()
const router = useRouter()
const bookId = route.params.id

const bookFormRef = ref(null)
const loading = ref(false)
const submitting = ref(false)
const isEdit = ref(false)
const borrowRecords = ref([])

// 表单数据
const bookForm = reactive({
  title: '',
  author: '',
  isbn: '',
  publisher: '',
  publishDate: '',
  category: '',
  description: '',
  stock: 0,
  status: 'AVAILABLE'
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入书名', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  author: [
    { required: true, message: '请输入作者', trigger: 'blur' }
  ],
  isbn: [
    { required: true, message: '请输入ISBN', trigger: 'blur' },
    { pattern: /^[0-9-]{10,17}$/, message: 'ISBN格式不正确', trigger: 'blur' }
  ],
  publisher: [
    { required: true, message: '请输入出版社', trigger: 'blur' }
  ],
  publishDate: [
    { required: true, message: '请选择出版日期', trigger: 'change' }
  ],
  category: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入图书简介', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入库存数量', trigger: 'blur' }
  ]
}

// 获取图书详情
const fetchBookDetail = async () => {
  loading.value = true
  try {
    const response = await api.get(`/api/books/${bookId}`)
    Object.assign(bookForm, response)
  } catch (error) {
    console.error('获取图书详情失败:', error)
    ElMessage.error('获取图书详情失败')
  } finally {
    loading.value = false
  }
}

// 获取借阅记录
const fetchBorrowRecords = async () => {
  try {
    const response = await api.get(`/api/books/${bookId}/borrow-records`)
    borrowRecords.value = response
  } catch (error) {
    console.error('获取借阅记录失败:', error)
  }
}

// 编辑
const handleEdit = () => {
  isEdit.value = true
}

// 提交
const handleSubmit = async () => {
  if (!bookFormRef.value) return
  
  await bookFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        await api.put(`/api/books/${bookId}`, bookForm)
        ElMessage.success('保存成功')
        isEdit.value = false
        await fetchBookDetail()
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error('保存失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

// 取消
const handleCancel = () => {
  isEdit.value = false
  fetchBookDetail()
}

// 返回
const handleBack = () => {
  router.back()
}

// 初始化
onMounted(() => {
  fetchBookDetail()
  fetchBorrowRecords()
})
</script>

<style scoped>
.book-detail-container {
  padding: 20px;
}

.form-header {
  margin-bottom: 20px;
}

.book-form {
  max-width: 600px;
  margin-bottom: 30px;
}

.borrow-records {
  margin-top: 30px;
}

.borrow-records h3 {
  margin-bottom: 15px;
}
</style> 