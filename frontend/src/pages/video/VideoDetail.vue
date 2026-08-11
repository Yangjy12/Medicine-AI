<template>
  <section v-if="video" class="detail-page">
    <div class="detail-main">
      <VideoPlayer
        :src="video.videoUrl"
        :poster="video.coverUrl"
        :start-second="video.progress?.currentSecond"
        @progress="saveProgress"
        @effective-play="recordPlay"
        @ended="saveProgress"
      />

      <div class="detail-info">
        <div>
          <p>{{ video.categoryName }} / {{ video.lecturer }}</p>
          <h1>{{ video.title }}</h1>
        </div>
        <div class="action-row">
          <button :class="{ active: video.liked }" @click="toggleLike">
            <ThumbsUp :size="18" />
            点赞 {{ video.likeCount }}
          </button>
          <button :class="{ active: video.collected }" @click="toggleFavorite">
            <Star :size="18" />
            收藏 {{ video.collectCount }}
          </button>
        </div>
      </div>

      <div class="progress-box">
        <div>
          <strong>学习进度 {{ video.progress?.progressPercent || 0 }}%</strong>
          <span>{{ video.progress?.finished ? '已完成学习' : '继续保持节奏' }}</span>
        </div>
        <div class="progress-line large">
          <div :style="{ width: `${video.progress?.progressPercent || 0}%` }"></div>
        </div>
      </div>

      <article class="description-block">
        <h2>课程简介</h2>
        <p>{{ video.description }}</p>
        <div class="tags">
          <span v-for="tag in video.tags" :key="tag">{{ tag }}</span>
        </div>
      </article>
    </div>

    <aside class="related-panel">
      <h2>相关推荐</h2>
      <VideoCard v-for="item in video.relatedVideos" :key="item.id" :video="item" />
    </aside>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Star, ThumbsUp } from 'lucide-vue-next'
import { videoApi, type ProgressInfo, type VideoDetail } from '../../api/video'
import VideoPlayer from '../../components/video/VideoPlayer.vue'
import VideoCard from '../../components/video/VideoCard.vue'

const route = useRoute()
const video = ref<VideoDetail>()
const id = Number(route.params.id)

const load = async () => {
  video.value = await videoApi.detail(id)
}

const saveProgress = async (payload: { currentSecond: number; duration: number }) => {
  const progress: ProgressInfo = await videoApi.progress(id, payload)
  if (video.value) video.value.progress = progress
}

const recordPlay = async (payload: { playedSecond: number; duration: number }) => {
  await videoApi.recordPlay(id, payload)
}

const toggleLike = async () => {
  if (!video.value) return
  if (video.value.liked) {
    await videoApi.unlike(id)
    video.value.liked = false
    video.value.likeCount -= 1
  } else {
    await videoApi.like(id)
    video.value.liked = true
    video.value.likeCount += 1
  }
}

const toggleFavorite = async () => {
  if (!video.value) return
  if (video.value.collected) {
    await videoApi.unfavorite(id)
    video.value.collected = false
    video.value.collectCount -= 1
  } else {
    await videoApi.favorite(id)
    video.value.collected = true
    video.value.collectCount += 1
  }
}

onMounted(load)
</script>
