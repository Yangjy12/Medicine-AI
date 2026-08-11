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
      </nav>

      <div class="login-card">
        <div class="avatar">学</div>
        <div>
          <strong>演示用户</strong>
          <span>ID: {{ userId }}</span>
        </div>
      </div>
    </aside>

    <main class="main-view">
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { BookOpen, Clock3, Star } from 'lucide-vue-next'
import { useUserStore } from './stores/user'

const { userId } = useUserStore()
</script>
