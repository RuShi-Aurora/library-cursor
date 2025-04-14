<template>
  <div class="maintenance-container">
    <h2>系统维护</h2>
    
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>数据库维护</span>
        </div>
      </template>
      
      <el-alert
        v-if="message"
        :title="message"
        :type="messageType"
        :closable="true"
        show-icon
      />
      
      <div class="maintenance-actions">
        <el-button 
          type="primary" 
          @click="fixConstraints" 
          :loading="loading"
        >
          修复数据库外键约束
        </el-button>
        <p class="description">
          修复数据库外键约束，允许删除有已完成借阅记录的图书。
        </p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import api from '../../api'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const message = ref('')
const messageType = ref('info')

const fixConstraints = async () => {
  loading.value = true
  message.value = ''
  messageType.value = 'info'
  
  try {
    console.log('开始修复数据库外键约束')
    // 先测试API是否可用
    try {
      console.log('测试维护API...')
      const testResponse = await api.get('/api/admin/maintenance/test')
      console.log('维护API测试响应:', testResponse)
    } catch (testError) {
      console.error('维护API测试失败:', testError)
      throw new Error('系统维护API不可用，请联系管理员')
    }
    
    // 执行修复操作
    console.log('执行修复外键约束操作...')
    const response = await api.post('/api/admin/maintenance/fix-constraints')
    console.log('修复响应:', response)
    
    messageType.value = 'success'
    message.value = response.message || '外键约束修复成功'
    ElMessage.success(message.value)
    
    // 修复成功后，刷新页面
    setTimeout(() => {
      window.location.reload()
    }, 2000)
  } catch (error) {
    console.error('修复外键约束失败:', error)
    messageType.value = 'error'
    
    if (error.response?.data?.message) {
      message.value = error.response.data.message
    } else if (error.message) {
      message.value = error.message
    } else {
      message.value = '修复失败，请联系系统管理员'
    }
    
    ElMessage.error(message.value)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.maintenance-container {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.maintenance-actions {
  margin-top: 20px;
}

.description {
  margin-top: 10px;
  color: #666;
  font-size: 14px;
}
</style> 