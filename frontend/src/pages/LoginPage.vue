<template>
  <section class="auth-page">
    <div class="auth-panel">
      <div class="auth-copy">
        <p>杏林学堂</p>
        <h1>登录后同步学习进度、签到积分和收藏课程</h1>
        <span>演示账号：student001 / abc123456</span>
      </div>

      <el-tabs v-model="mode" stretch>
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" label-position="top" @submit.prevent>
            <el-form-item label="账号">
              <el-input v-model="loginForm.account" placeholder="用户名或手机号" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="loginForm.password" type="password" show-password placeholder="请输入密码" />
            </el-form-item>
            <el-button type="primary" :loading="loading" class="full-btn" @click="login">登录</el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" label-position="top" @submit.prevent>
            <el-form-item label="用户名">
              <el-input v-model="registerForm.username" placeholder="4-32位中文、字母、数字或下划线" />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="registerForm.nickname" placeholder="2-20位昵称" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="registerForm.phone" placeholder="可选" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="registerForm.password" type="password" show-password placeholder="至少8位，包含字母和数字" />
            </el-form-item>
            <el-button type="primary" :loading="loading" class="full-btn" @click="register">注册并登录</el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi } from '../api/user'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const mode = ref('login')
const loading = ref(false)

const loginForm = reactive({
  account: 'student001',
  password: 'abc123456',
  deviceId: 'web'
})

const registerForm = reactive({
  username: '',
  nickname: '',
  phone: '',
  password: ''
})

const login = async () => {
  loading.value = true
  try {
    const result = await userApi.login(loginForm)
    userStore.setSession(result.accessToken, result.refreshToken, result.user)
    ElMessage.success('登录成功')
    router.push('/videos')
  } finally {
    loading.value = false
  }
}

const register = async () => {
  loading.value = true
  try {
    await userApi.register(registerForm)
    const result = await userApi.login({ account: registerForm.username, password: registerForm.password, deviceId: 'web' })
    userStore.setSession(result.accessToken, result.refreshToken, result.user)
    ElMessage.success('注册成功')
    router.push('/videos')
  } finally {
    loading.value = false
  }
}
</script>
