<template>
  <article class="video-card" @click="$router.push(`/videos/${video.id}`)">
    <div class="thumb-wrap">
      <img :src="video.coverUrl" :alt="video.title" loading="lazy" />
      <span class="duration">{{ formatDuration(video.duration) }}</span>
      <span v-if="video.finished" class="finished">已学完</span>
    </div>
    <div class="video-card-body">
      <h3>{{ video.title }}</h3>
      <div class="meta">
        <span>{{ video.lecturer }}</span>
        <span>{{ video.categoryName }}</span>
      </div>
      <div class="tags">
        <span v-for="tag in video.tags.slice(0, 3)" :key="tag">{{ tag }}</span>
      </div>
      <div v-if="video.progressPercent > 0" class="progress-line">
        <div :style="{ width: `${video.progressPercent}%` }"></div>
      </div>
      <footer>
        <span><Play :size="14" />{{ compactNumber(video.playCount) }}</span>
        <span><ThumbsUp :size="14" />{{ compactNumber(video.likeCount) }}</span>
        <span><Star :size="14" />{{ compactNumber(video.collectCount) }}</span>
      </footer>
    </div>
  </article>
</template>

<script setup lang="ts">
import { Play, Star, ThumbsUp } from 'lucide-vue-next'
import type { VideoCard } from '../../api/video'
import { compactNumber, formatDuration } from '../../utils/format'

defineProps<{ video: VideoCard }>()
</script>
