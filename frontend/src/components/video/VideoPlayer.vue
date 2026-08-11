<template>
  <div class="player-shell">
    <video
      ref="videoRef"
      class="video-player"
      :src="src"
      :poster="poster"
      controls
      preload="metadata"
      @loadedmetadata="handleLoaded"
      @timeupdate="handleTimeUpdate"
      @pause="emitProgress"
      @ended="emitEnded"
      @play="handlePlay"
    />
  </div>
</template>

<script setup lang="ts">
import { nextTick, ref, watch } from 'vue'

const props = defineProps<{
  src: string
  poster: string
  startSecond?: number
}>()

const emit = defineEmits<{
  progress: [payload: { currentSecond: number; duration: number }]
  effectivePlay: [payload: { playedSecond: number; duration: number }]
  ended: [payload: { currentSecond: number; duration: number }]
}>()

const videoRef = ref<HTMLVideoElement>()
const lastEmitSecond = ref(0)
const playStartedAt = ref<number | null>(null)
const effectiveReported = ref(false)

const handleLoaded = async () => {
  await nextTick()
  if (videoRef.value && props.startSecond && props.startSecond > 0) {
    videoRef.value.currentTime = props.startSecond
  }
}

const handlePlay = () => {
  playStartedAt.value = Date.now()
}

const handleTimeUpdate = () => {
  const video = videoRef.value
  if (!video) return
  if (!effectiveReported.value && video.currentTime >= 10) {
    effectiveReported.value = true
    emit('effectivePlay', { playedSecond: Math.floor(video.currentTime), duration: Math.floor(video.duration || 0) })
  }
  if (Math.floor(video.currentTime) - lastEmitSecond.value >= 15) {
    emitProgress()
  }
}

const emitProgress = () => {
  const video = videoRef.value
  if (!video || !video.duration) return
  const currentSecond = Math.floor(video.currentTime)
  lastEmitSecond.value = currentSecond
  emit('progress', { currentSecond, duration: Math.floor(video.duration) })
}

const emitEnded = () => {
  const video = videoRef.value
  if (!video || !video.duration) return
  emit('ended', { currentSecond: Math.floor(video.duration), duration: Math.floor(video.duration) })
}

watch(() => props.src, () => {
  effectiveReported.value = false
  lastEmitSecond.value = 0
})
</script>
