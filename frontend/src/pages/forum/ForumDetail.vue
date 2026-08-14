<template>
  <section class="forum-detail-page" v-loading="loading">
    <article v-if="post" class="forum-post-detail">
      <header class="post-detail-head">
        <div class="post-badges">
          <span>{{ post.boardName }}</span>
          <em v-if="post.topFlag">置顶</em>
          <em v-if="post.essenceFlag">精华</em>
        </div>
        <h1>{{ post.title }}</h1>
        <p>{{ post.authorName }} · {{ formatDateTime(post.publishTime || post.createdAt) }}</p>
      </header>

      <div class="post-content">{{ post.content }}</div>

      <div class="post-action-bar">
        <button :class="{ active: post.liked }" @click="toggleLike">
          <ThumbsUp :size="17" />
          <span>{{ post.liked ? '已点赞' : '点赞' }} {{ compactNumber(post.likeCount) }}</span>
        </button>
        <button :class="{ active: post.favorited }" @click="toggleFavorite">
          <Star :size="17" />
          <span>{{ post.favorited ? '已收藏' : '收藏' }} {{ compactNumber(post.favoriteCount) }}</span>
        </button>
        <span><Eye :size="16" /> {{ compactNumber(post.viewCount) }} 浏览</span>
        <span><MessageCircle :size="16" /> {{ compactNumber(post.commentCount) }} 评论</span>
      </div>
    </article>

    <section v-if="post" class="section-block comment-block">
      <div class="section-title">
        <div>
          <span>COMMENTS</span>
          <h2>评论讨论</h2>
        </div>
      </div>

      <div class="comment-editor">
        <el-input v-model="commentText" type="textarea" :rows="3" maxlength="2000" show-word-limit placeholder="写下你的看法" />
        <el-button type="primary" @click="submitComment()">发表评论</el-button>
      </div>

      <div class="comment-list">
        <article v-for="comment in comments" :key="comment.id" class="comment-row">
          <div class="comment-avatar">学</div>
          <div class="comment-main">
            <header>
              <strong>{{ comment.authorName }}</strong>
              <span>{{ formatDateTime(comment.createdAt) }}</span>
            </header>
            <p>{{ comment.content }}</p>
            <footer>
              <button @click="toggleCommentLike(comment)">
                <ThumbsUp :size="14" />
                <span>{{ comment.likeCount }}</span>
              </button>
              <button @click="replyTarget = comment">回复</button>
            </footer>

            <div v-if="comment.replies?.length" class="reply-list">
              <article v-for="reply in comment.replies" :key="reply.id" class="reply-row">
                <strong>{{ reply.authorName }}</strong>
                <span>{{ reply.content }}</span>
              </article>
            </div>

            <div v-if="replyTarget?.id === comment.id" class="reply-editor">
              <el-input v-model="replyText" size="small" placeholder="回复这条评论" @keyup.enter="submitComment(comment.id)" />
              <el-button size="small" type="primary" @click="submitComment(comment.id)">发送</el-button>
              <el-button size="small" @click="replyTarget = undefined">取消</el-button>
            </div>
          </div>
        </article>
      </div>

      <el-empty v-if="comments.length === 0" description="还没有评论" />
    </section>
  </section>
</template>

<script setup lang="ts">
import { Eye, MessageCircle, Star, ThumbsUp } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { forumApi, type CommentItem, type PostDetail } from '../../api/forum'
import { useUserStore } from '../../stores/user'
import { compactNumber, formatDateTime } from '../../utils/format'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const post = ref<PostDetail>()
const comments = ref<CommentItem[]>([])
const loading = ref(false)
const commentText = ref('')
const replyText = ref('')
const replyTarget = ref<CommentItem>()

const postId = () => Number(route.params.id)

const requireLogin = () => {
  if (!userStore.loggedIn) {
    router.push('/login')
    return false
  }
  return true
}

const loadPost = async () => {
  loading.value = true
  try {
    post.value = await forumApi.detail(postId())
  } finally {
    loading.value = false
  }
}

const loadComments = async () => {
  const result = await forumApi.comments(postId(), { page: 1, pageSize: 20 })
  comments.value = result.records
}

const toggleLike = async () => {
  if (!post.value || !requireLogin()) return
  if (post.value.liked) {
    await forumApi.unlikePost(post.value.id)
    post.value.liked = false
    post.value.likeCount = Math.max(0, post.value.likeCount - 1)
  } else {
    await forumApi.likePost(post.value.id)
    post.value.liked = true
    post.value.likeCount += 1
  }
}

const toggleFavorite = async () => {
  if (!post.value || !requireLogin()) return
  if (post.value.favorited) {
    await forumApi.unfavoritePost(post.value.id)
    post.value.favorited = false
    post.value.favoriteCount = Math.max(0, post.value.favoriteCount - 1)
  } else {
    await forumApi.favoritePost(post.value.id)
    post.value.favorited = true
    post.value.favoriteCount += 1
  }
}

const submitComment = async (parentId = 0) => {
  if (!requireLogin()) return
  const content = parentId ? replyText.value.trim() : commentText.value.trim()
  if (!content) {
    ElMessage.warning('请输入评论内容')
    return
  }
  await forumApi.createComment(postId(), { parentId, content })
  commentText.value = ''
  replyText.value = ''
  replyTarget.value = undefined
  await loadComments()
  await loadPost()
}

const toggleCommentLike = async (comment: CommentItem) => {
  if (!requireLogin()) return
  if (comment.liked) {
    await forumApi.unlikeComment(comment.id)
    comment.liked = false
    comment.likeCount = Math.max(0, comment.likeCount - 1)
  } else {
    await forumApi.likeComment(comment.id)
    comment.liked = true
    comment.likeCount += 1
  }
}

onMounted(async () => {
  await loadPost()
  await loadComments()
})
</script>
