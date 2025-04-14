<template>
  <el-form :model="form" :rules="rules" ref="formRef">
    <!-- 其他表单项 -->
    <el-form-item label="角色" prop="role">
      <el-select v-model="form.role">
        <el-option label="普通用户" value="USER" />
        <el-option label="管理员" value="ADMIN" />
      </el-select>
    </el-form-item>
  </el-form>
</template>

<script setup>
const form = reactive({
  username: '',
  password: '',
  email: '',
  role: 'USER' // 默认角色
})
</script> 