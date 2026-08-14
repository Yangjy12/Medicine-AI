import { createRouter, createWebHistory } from 'vue-router'
import VideoHome from '../pages/video/VideoHome.vue'
import VideoDetail from '../pages/video/VideoDetail.vue'
import LearningHistory from '../pages/video/LearningHistory.vue'
import FavoriteVideos from '../pages/video/FavoriteVideos.vue'
import LoginPage from '../pages/LoginPage.vue'
import AdminCenter from '../pages/AdminCenter.vue'
import MyUploads from '../pages/MyUploads.vue'
import AiAssistant from '../pages/AiAssistant.vue'
import FriendCircle from '../pages/FriendCircle.vue'
import ForumHome from '../pages/forum/ForumHome.vue'
import ForumDetail from '../pages/forum/ForumDetail.vue'
import ForumEditor from '../pages/forum/ForumEditor.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/videos' },
    { path: '/login', component: LoginPage },
    { path: '/videos', component: VideoHome },
    { path: '/videos/:id', component: VideoDetail },
    { path: '/learning-history', component: LearningHistory },
    { path: '/favorites', component: FavoriteVideos },
    { path: '/ai', component: AiAssistant },
    { path: '/circle', component: FriendCircle },
    { path: '/forum', component: ForumHome },
    { path: '/forum/posts/:id', component: ForumDetail },
    { path: '/forum/editor', component: ForumEditor },
    { path: '/uploads', component: MyUploads },
    { path: '/admin', component: AdminCenter }
  ],
  scrollBehavior: () => ({ top: 0 })
})

export default router
