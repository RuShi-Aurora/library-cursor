<template>
  <div class="add-book-container">
    <div class="form-header">
      <h2>添加图书</h2>
    </div>

    <el-form
      ref="bookFormRef"
      :model="bookForm"
      :rules="rules"
      label-width="100px"
      class="book-form"
    >
      <el-form-item label="书名" prop="title">
        <el-input v-model="bookForm.title" placeholder="请输入书名" />
      </el-form-item>

      <el-form-item label="作者" prop="author">
        <el-input v-model="bookForm.author" placeholder="请输入作者" />
      </el-form-item>

      <el-form-item label="ISBN" prop="isbn">
        <el-input v-model="bookForm.isbn" placeholder="请输入ISBN" />
      </el-form-item>

      <el-form-item label="出版社" prop="publisher">
        <el-input v-model="bookForm.publisher" placeholder="请输入出版社" />
      </el-form-item>

      <el-form-item label="出版日期" prop="publishDate">
        <el-date-picker
          v-model="bookForm.publishDate"
          type="date"
          placeholder="选择出版日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>

      <el-form-item label="分类" prop="category">
        <el-select v-model="bookForm.category" placeholder="请选择分类">
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
          placeholder="请输入图书简介"
        />
      </el-form-item>

      <el-form-item label="库存数量" prop="stock">
        <el-input-number
          v-model="bookForm.stock"
          :min="0"
          :max="999"
          placeholder="请输入库存数量"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          保存
        </el-button>
        <el-button @click="handleCancel">取消</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import api from '../api'

const router = useRouter()
const bookFormRef = ref(null)
const loading = ref(false)

// 表单数据
const bookForm = reactive({
  title: '',
  author: '',
  isbn: '',
  publisher: '',
  publishDate: '',
  category: '',
  description: '',
  stock: 1
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入书名', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' }
  ],
  author: [
    { required: true, message: '请输入作者', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
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
    { required: true, message: '请输入图书简介', trigger: 'blur' },
    { max: 500, message: '简介最多 500 个字符', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入库存数量', trigger: 'blur' },
    { type: 'number', min: 0, message: '库存不能小于0', trigger: 'blur' }
  ]
}

// 提交表单
const handleSubmit = async () => {
  if (!bookFormRef.value) return
  
  await bookFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await api.post('/api/books', bookForm)
        ElMessage.success('添加图书成功')
        router.push('/books')
      } catch (error) {
        console.error('添加图书失败:', error)
        ElMessage.error(error.response?.data?.message || '添加图书失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 取消
const handleCancel = () => {
  router.back()
}
</script>

<style scoped>
.add-book-container {
  padding: 20px;
}

.form-header {
  margin-bottom: 20px;
}

.book-form {
  max-width: 600px;
}
</style> 