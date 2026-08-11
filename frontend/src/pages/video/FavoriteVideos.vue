<template>
  <section class="video-page narrow">
    <header class="simple-header">
      <h1>我的收藏</h1>
      <p>沉淀值得反复看的中医课程。</p>
    </header>
    <div v-loading="loading" class="video-grid">
      <VideoCard v-for="video in videos" :key="video.id" :video="video" />
    </div>
    <el-empty v-if="!loading && videos.length === 0" description="暂无收藏课程" />
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
    videos.value = (await videoApi.favorites({ page: 1, pageSize: 20 })).records
  } finally {
    loading.value = false
  }
})
</script>
