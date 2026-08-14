<template>
  <section class="forum-page">
    <header class="topbar">
      <div>
        <p>杏林论坛</p>
        <h1>沉淀学习笔记与中医讨论</h1>
      </div>
      <div class="forum-actions">
        <div class="search-bar">
          <Search :size="18" />
          <input v-model="keyword" placeholder="搜索帖子、笔记、课程讨论" @keyup.enter="loadPosts(true)" />
          <button @click="loadPosts(true)">搜索</button>
        </div>
        <el-button type="primary" :icon="PenLine" @click="goEditor">发帖</el-button>
      </div>
    </header>

    <div class="category-rail">
      <button :class="{ active: !query.boardId }" @click="selectBoard(undefined)">全部</button>
      <button
        v-for="board in boards"
        :key="board.id"
        :class="{ active: query.boardId === board.id }"
        @click="selectBoard(board.id)"
      >
        {{ board.name }}
      </button>
    </div>

    <div class="content-grid">
      <section class="section-block">
        <div class="section-title">
          <div>
            <span>POSTS</span>
            <h2>{{ keyword.trim() ? '搜索结果' : '最新讨论' }}</h2>
          </div>
          <el-select v-model="query.sort" style="width: 150px" @change="loadPosts(true)">
            <el-option label="最新发布" value="latest" />
            <el-option label="热门优先" value="hot" />
            <el-option label="浏览最多" value="mostViewed" />
            <el-option label="评论最多" value="mostCommented" />
            <el-option label="点赞最多" value="mostLiked" />
          </el-select>
        </div>

        <div v-loading="loading" class="post-list">
          <article v-for="post in posts" :key="post.id" class="post-row" @click="$router.push(`/forum/posts/${post.id}`)">
            <div class="post-row-main">
              <div class="post-badges">
                <span>{{ post.boardName }}</span>
                <em v-if="post.topFlag">置顶</em>
                <em v-if="post.essenceFlag">精华</em>
              </div>
              <h3>{{ post.title }}</h3>
              <p>{{ post.summary }}</p>
              <footer>
                <span>{{ post.authorName }}</span>
                <span>{{ formatDateTime(post.publishTime || post.createdAt) }}</span>
              </footer>
            </div>
            <div class="post-stats">
              <span><Eye :size="15" />{{ compactNumber(post.viewCount) }}</span>
              <span><MessageCircle :size="15" />{{ compactNumber(post.commentCount) }}</span>
              <span><ThumbsUp :size="15" />{{ compactNumber(post.likeCount) }}</span>
            </div>
          </article>
        </div>

        <el-empty v-if="!loading && posts.length === 0" description="暂无帖子" />
        <el-pagination
          v-if="pageResult.total > pageResult.pageSize"
          layout="prev, pager, next"
          :total="pageResult.total"
          :page-size="pageResult.pageSize"
          v-model:current-page="query.page"
          @current-change="loadPosts(false)"
        />
      </section>

      <aside class="rank-panel">
        <div class="section-title compact">
          <div>
            <span>HOT</span>
            <h2>热门帖子</h2>
          </div>
        </div>
        <button
          v-for="(post, index) in hotPosts"
          :key="post.id"
          class="rank-row"
          @click="$router.push(`/forum/posts/${post.id}`)"
        >
          <strong>{{ index + 1 }}</strong>
          <span>{{ post.title }}</span>
          <em>{{ compactNumber(post.viewCount) }}</em>
        </button>
      </aside>
    </div>
  </section>
</template>

<script setup lang="ts">
import { Eye, MessageCircle, PenLine, Search, ThumbsUp } from 'lucide-vue-next'
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { forumApi, type Board, type PageResult, type PostCard } from '../../api/forum'
import { useUserStore } from '../../stores/user'
import { compactNumber, formatDateTime } from '../../utils/format'

const router = useRouter()
const userStore = useUserStore()
const boards = ref<Board[]>([])
const posts = ref<PostCard[]>([])
const hotPosts = ref<PostCard[]>([])
const keyword = ref('')
const loading = ref(false)
const query = reactive({ boardId: undefined as number | undefined, sort: 'latest', page: 1, pageSize: 12 })
const pageResult = reactive<PageResult<PostCard>>({ records: [], page: 1, pageSize: 12, total: 0 })

const loadBoards = async () => {
  boards.value = await forumApi.boards()
}

const loadHot = async () => {
  hotPosts.value = await forumApi.hotPosts()
}

const loadPosts = async (resetPage: boolean) => {
  if (resetPage) query.page = 1
  loading.value = true
  try {
    const result = await forumApi.posts({ ...query, keyword: keyword.value.trim() || undefined })
    posts.value = result.records
    Object.assign(pageResult, result)
  } finally {
    loading.value = false
  }
}

const selectBoard = (id?: number) => {
  query.boardId = id
  loadPosts(true)
}

const goEditor = () => {
  router.push(userStore.loggedIn ? '/forum/editor' : '/login')
}

onMounted(async () => {
  await Promise.all([loadBoards(), loadHot()])
  await loadPosts(true)
})
</script>
