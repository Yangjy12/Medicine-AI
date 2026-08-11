<template>
  <section class="auth-page">
    <div class="auth-panel">
      <div class="auth-copy">
        <p>杏林学堂</p>
        <h1>登录后同步学习进度、签到积分和收藏课程</h1>
        <span>可注册新账号，或使用数据库中已初始化的账号登录</span>
      </div>

      <el-tabs v-model="mode" stretch>
        <el-tab-pane label="登录" name="login">
          <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-position="top" @submit.prevent>
            <el-form-item label="账号" prop="account">
              <el-input v-model.trim="loginForm.account" clearable placeholder="用户名或手机号" @keyup.enter="login" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="loginForm.password" type="password" show-password placeholder="请输入密码" @keyup.enter="login" />
            </el-form-item>
            <el-button type="primary" :loading="loading" class="full-btn" @click="login">登录</el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-position="top" @submit.prevent>
            <el-form-item label="用户名" prop="username">
              <el-input v-model.trim="registerForm.username" clearable placeholder="2-32位中文、字母、数字或下划线" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model.trim="registerForm.phone" clearable placeholder="请输入11位手机号" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="registerForm.password" type="password" show-password placeholder="8-32位，包含字母和数字" @keyup.enter="register" />
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
import type { FormInstance, FormRules } from 'element-plus'
import { userApi } from '../api/user'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const mode = ref('login')
const loading = ref(false)
const loginFormRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()

const loginForm = reactive({
  account: '',
  password: '',
  deviceId: 'web'
})

const registerForm = reactive({
  username: '',
  phone: '',
  password: ''
})

const usernamePattern = /^[\u4e00-\u9fa5A-Za-z0-9_]+$/
const phonePattern = /^1[3-9]\d{9}$/

const validateUsername = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (!value) {
    callback(new Error('请输入用户名'))
    return
  }
  if (value.length < 2 || value.length > 32) {
    callback(new Error('用户名长度需为2-32位'))
    return
  }
  if (!usernamePattern.test(value)) {
    callback(new Error('用户名只能包含中文、字母、数字或下划线'))
    return
  }
  callback()
}

const validatePhone = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (!value) {
    callback(new Error('请输入手机号'))
    return
  }
  if (!phonePattern.test(value)) {
    callback(new Error('请输入正确的手机号'))
    return
  }
  callback()
}

const validateRegisterPassword = (_rule: unknown, value: string, callback: (error?: Error) => void) => {
  if (!value) {
    callback(new Error('请输入密码'))
    return
  }
  if (value.length < 8 || value.length > 32) {
    callback(new Error('密码长度需为8-32位'))
    return
  }
  if (!/[A-Za-z]/.test(value) || !/\d/.test(value)) {
    callback(new Error('密码至少包含字母和数字'))
    return
  }
  if (registerForm.username && value.toLowerCase().includes(registerForm.username.toLowerCase())) {
    callback(new Error('密码不能包含用户名'))
    return
  }
  if (registerForm.phone && value.includes(registerForm.phone.slice(-4))) {
    callback(new Error('密码不能包含手机号后四位'))
    return
  }
  callback()
}

const loginRules = reactive<FormRules>({
  account: [{ required: true, message: '请输入用户名或手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
})

const registerRules = reactive<FormRules>({
  username: [{ validator: validateUsername, trigger: 'blur' }],
  phone: [{ validator: validatePhone, trigger: 'blur' }],
  password: [{ validator: validateRegisterPassword, trigger: 'blur' }]
})

const validateForm = async (form?: FormInstance) => {
  if (!form) {
    return false
  }
  try {
    await form.validate()
    return true
  } catch {
    return false
  }
}

const login = async () => {
  if (!(await validateForm(loginFormRef.value))) {
    return
  }
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
  if (!(await validateForm(registerFormRef.value))) {
    return
  }
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
