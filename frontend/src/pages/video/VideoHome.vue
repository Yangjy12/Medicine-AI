<template>
  <section class="video-page">
    <header class="topbar">
      <div>
        <p>视频学习中心</p>
        <h1>循序渐进学习中医知识</h1>
      </div>
      <VideoSearchBar v-model="keyword" @search="loadList(true)" />
    </header>

    <div class="category-rail">
      <button :class="{ active: !query.categoryId }" @click="selectCategory(undefined)">全部</button>
      <button
        v-for="category in home?.categories || []"
        :key="category.id"
        :class="{ active: query.categoryId === category.id }"
        @click="selectCategory(category.id)"
      >
        {{ category.name }}
      </button>
    </div>

    <section v-if="home?.continueLearning?.length" class="section-block">
      <div class="section-title">
        <div>
          <span>CONTINUE</span>
          <h2>继续学习</h2>
        </div>
      </div>
      <div class="continue-strip">
        <VideoCard v-for="video in home.continueLearning" :key="video.id" :video="video" />
      </div>
    </section>

    <div class="content-grid">
      <section class="section-block">
        <div class="section-title">
          <div>
            <span>COURSES</span>
            <h2>{{ keyword ? '搜索结果' : '推荐课程' }}</h2>
          </div>
          <el-select v-model="query.sort" style="width: 150px" @change="loadList(true)">
            <el-option label="综合排序" value="comprehensive" />
            <el-option label="最新发布" value="latest" />
            <el-option label="播放最多" value="hottest" />
            <el-option label="点赞最多" value="mostLiked" />
            <el-option label="收藏最多" value="mostCollected" />
          </el-select>
        </div>

        <div v-loading="loading" class="video-grid">
          <VideoCard v-for="video in videos" :key="video.id" :video="video" />
        </div>

        <el-empty v-if="!loading && videos.length === 0" description="暂无匹配课程" />

        <el-pagination
          v-if="pageResult.total > pageResult.pageSize"
          layout="prev, pager, next"
          :total="pageResult.total"
          :page-size="pageResult.pageSize"
          v-model:current-page="query.page"
          @current-change="loadList(false)"
        />
      </section>

      <aside class="rank-panel">
        <div class="section-title compact">
          <div>
            <span>HOT</span>
            <h2>热门课程</h2>
          </div>
        </div>
        <button
          v-for="(video, index) in home?.hot || []"
          :key="video.id"
          class="rank-row"
          @click="$router.push(`/videos/${video.id}`)"
        >
          <strong>{{ index + 1 }}</strong>
          <span>{{ video.title }}</span>
          <em>{{ compactNumber(video.playCount) }}</em>
        </button>
      </aside>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { videoApi, type HomeData, type PageResult, type VideoCard as VideoCardItem } from '../../api/video'
import VideoCard from '../../components/video/VideoCard.vue'
import VideoSearchBar from '../../components/video/VideoSearchBar.vue'
import { compactNumber } from '../../utils/format'

const home = ref<HomeData>()
const videos = ref<VideoCardItem[]>([])
const keyword = ref('')
const loading = ref(false)
const query = reactive({ categoryId: undefined as number | undefined, sort: 'comprehensive', page: 1, pageSize: 12 })
const pageResult = reactive<PageResult<VideoCardItem>>({ records: [], page: 1, pageSize: 12, total: 0, pages: 0 })

const loadHome = async () => {
  home.value = await videoApi.home()
}

const loadList = async (resetPage: boolean) => {
  if (resetPage) query.page = 1
  loading.value = true
  try {
    const api = keyword.value.trim() ? videoApi.search : videoApi.list
    const result = await api({ ...query, keyword: keyword.value.trim() || undefined })
    videos.value = result.records
    Object.assign(pageResult, result)
  } finally {
    loading.value = false
  }
}

const selectCategory = (id?: number) => {
  query.categoryId = id
  loadList(true)
}

onMounted(async () => {
  await loadHome()
  await loadList(true)
})
</script>
