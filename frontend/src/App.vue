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
        <RouterLink v-if="loggedIn" to="/admin" class="nav-item">
          <Settings :size="18" />
          <span>{{ isAdmin ? '数据维护' : '上传课程' }}</span>
        </RouterLink>
      </nav>

      <div class="login-card">
        <div class="avatar">学</div>
        <div v-if="loggedIn">
          <strong>{{ nickname }}</strong>
          <span>ID: {{ userId }}</span>
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
import { BookOpen, Clock3, Settings, Star } from 'lucide-vue-next'
import { storeToRefs } from 'pinia'
import { useUserStore } from './stores/user'

const userStore = useUserStore()
const { userId, nickname, loggedIn, isAdmin } = storeToRefs(userStore)
</script>
