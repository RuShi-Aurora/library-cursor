<template>
  <div class="admin-container">
    <h1>管理后台</h1>

    <el-tabs type="border-card">
      <el-tab-pane label="用户管理">
        <el-button type="primary" @click="router.push('/admin/users')">用户管理</el-button>
      </el-tab-pane>
      <el-tab-pane label="图书管理">
        <el-button type="primary" @click="router.push('/books')">图书管理</el-button>
      </el-tab-pane>
      <el-tab-pane label="借阅管理">
        <el-button type="primary" @click="router.push('/admin/borrows')">借阅管理</el-button>
      </el-tab-pane>
      <el-tab-pane label="系统维护">
        <el-button type="primary" @click="router.push('/admin/maintenance')">系统维护</el-button>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.admin-container {
  padding: 20px;
}

h1 {
  margin-bottom: 20px;
}
</style> 