<template>
  <div class="app-shell">
    <aside class="side-nav">
      <div class="brand">
        <div class="brand-mark">杏</div>
        <div>
          <strong>杏林学堂</strong>
          <span>中医学习系统</span>
        </div>
      </div>

      <nav>
        <RouterLink to="/videos" class="nav-item">
          <BookOpen :size="18" />
          <span>视频学习中心</span>
        </RouterLink>
        <RouterLink to="/learning-history" class="nav-item">
          <Clock3 :size="18" />
          <span>学习历史</span>
        </RouterLink>
        <RouterLink to="/favorites" class="nav-item">
          <Star :size="18" />
          <span>我的收藏</span>
        </RouterLink>
        <RouterLink to="/ai" class="nav-item">
          <Bot :size="18" />
          <span>AI助手</span>
        </RouterLink>
        <RouterLink to="/circle" class="nav-item">
          <UsersRound :size="18" />
          <span>医友圈</span>
        </RouterLink>
        <RouterLink to="/forum" class="nav-item">
          <MessagesSquare :size="18" />
          <span>杏林论坛</span>
        </RouterLink>
        <RouterLink v-if="loggedIn && !isAdmin" to="/uploads" class="nav-item">
          <UploadCloud :size="18" />
          <span>我的上传</span>
        </RouterLink>
        <RouterLink v-if="loggedIn && isAdmin" to="/admin" class="nav-item">
          <Settings :size="18" />
          <span>数据维护</span>
        </RouterLink>
      </nav>

      <div class="login-card">
        <div class="avatar">学</div>
        <div v-if="loggedIn">
          <strong>{{ nickname }}</strong>
          <span>ID: {{ userId }}</span>
          <button class="logout-link" @click="logout">
            <LogOut :size="14" />
            <span>退出登录</span>
          </button>
        </div>
        <RouterLink v-else to="/login" class="login-link">登录</RouterLink>
      </div>
    </aside>

    <main class="main-view">
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { BookOpen, Bot, Clock3, LogOut, MessagesSquare, Settings, Star, UploadCloud, UsersRound } from 'lucide-vue-next'
import { storeToRefs } from 'pinia'
import { useRouter } from 'vue-router'
import { userApi } from './api/user'
import { useUserStore } from './stores/user'

const router = useRouter()
const userStore = useUserStore()
const { userId, nickname, loggedIn, isAdmin } = storeToRefs(userStore)

const logout = async () => {
  try {
    await userApi.logout()
  } finally {
    userStore.clearSession()
    router.push('/login')
  }
}
</script>
