import { createRouter, createWebHistory } from 'vue-router'
import VideoHome from '../pages/video/VideoHome.vue'
import VideoDetail from '../pages/video/VideoDetail.vue'
import LearningHistory from '../pages/video/LearningHistory.vue'
import FavoriteVideos from '../pages/video/FavoriteVideos.vue'
import LoginPage from '../pages/LoginPage.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/videos' },
    { path: '/login', component: LoginPage },
    { path: '/videos', component: VideoHome },
    { path: '/videos/:id', component: VideoDetail },
    { path: '/learning-history', component: LearningHistory },
    { path: '/favorites', component: FavoriteVideos }
  ],
  scrollBehavior: () => ({ top: 0 })
})

export default router
