<template>
  <section class="video-page narrow">
    <header class="simple-header">
      <h1>学习历史</h1>
      <p>按最近学习时间排序，快速回到上次课程。</p>
    </header>
    <div v-loading="loading" class="video-grid">
      <VideoCard v-for="video in videos" :key="video.id" :video="video" />
    </div>
    <el-empty v-if="!loading && videos.length === 0" description="暂无学习记录" />
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { videoApi, type VideoCard as VideoCardItem } from '../../api/video'
import VideoCard from '../../components/video/VideoCard.vue'

const videos = ref<VideoCardItem[]>([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    videos.value = (await videoApi.history({ page: 1, pageSize: 20 })).records
  } finally {
    loading.value = false
  }
})
</script>
