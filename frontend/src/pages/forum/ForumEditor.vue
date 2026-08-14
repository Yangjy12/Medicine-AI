<template>
  <section class="forum-page narrow">
    <header class="simple-header">
      <p>发布帖子</p>
      <h1>把学习过程沉淀下来</h1>
    </header>

    <section class="section-block">
      <el-form label-position="top">
        <el-form-item label="标题">
          <el-input v-model="form.title" maxlength="128" show-word-limit placeholder="例如：阴阳五行学习笔记" />
        </el-form-item>
        <el-form-item label="板块">
          <el-select v-model="form.boardId" placeholder="请选择板块" style="width: 260px">
            <el-option v-for="board in boards" :key="board.id" :label="board.name" :value="board.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="正文">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="16"
            maxlength="20000"
            show-word-limit
            placeholder="支持普通文本或 Markdown，建议写清楚学习背景、问题和参考课程。"
          />
        </el-form-item>
        <div class="editor-actions">
          <el-button @click="$router.push('/forum')">取消</el-button>
          <el-button type="primary" :loading="saving" @click="save">发布</el-button>
        </div>
      </el-form>
    </section>
  </section>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { forumApi, type Board } from '../../api/forum'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const boards = ref<Board[]>([])
const saving = ref(false)
const form = reactive({
  boardId: undefined as number | undefined,
  title: '',
  content: ''
})

const save = async () => {
  if (!userStore.loggedIn) {
    router.push('/login')
    return
  }
  if (!form.boardId || form.title.trim().length < 5 || form.content.trim().length < 10) {
    ElMessage.warning('请完整填写板块、标题和正文')
    return
  }
  saving.value = true
  try {
    const post = await forumApi.createPost({
      boardId: form.boardId,
      title: form.title.trim(),
      content: form.content.trim()
    })
    ElMessage.success('发布成功')
    router.push(`/forum/posts/${post.id}`)
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  if (!userStore.loggedIn) {
    router.push('/login')
    return
  }
  boards.value = await forumApi.boards()
})
</script>
