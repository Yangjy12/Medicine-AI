<template>
  <section class="workspace-page">
    <header class="simple-header">
      <p>AI 助手</p>
      <h1>杏林问答</h1>
    </header>

    <div class="assistant-layout">
      <aside class="section-block assistant-sidebar">
        <button v-for="item in presets" :key="item" @click="draft = item">
          <Sparkles :size="16" />
          <span>{{ item }}</span>
        </button>
      </aside>

      <section class="section-block assistant-panel">
        <div class="assistant-thread">
          <article v-for="message in messages" :key="message.id" :class="['assistant-message', message.role]">
            <strong>{{ message.role === 'user' ? '我' : '杏林助手' }}</strong>
            <p>{{ message.content }}</p>
          </article>
        </div>

        <div class="assistant-composer">
          <el-input v-model="draft" type="textarea" :rows="3" maxlength="1000" show-word-limit placeholder="输入中医学习问题" />
          <el-button type="primary" :icon="Send" @click="send">发送</el-button>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { Send, Sparkles } from 'lucide-vue-next'
import { ref } from 'vue'

interface ChatMessage {
  id: number
  role: 'user' | 'assistant'
  content: string
}

const presets = ['帮我制定中医基础学习计划', '解释阴阳五行的关系', '推荐适合初学者的课程']
const draft = ref('')
const messages = ref<ChatMessage[]>([
  {
    id: 1,
    role: 'assistant',
    content: 'AI 助手后端服务正在接入，当前页面已预留对话入口。'
  }
])

const send = () => {
  const content = draft.value.trim()
  if (!content) {
    ElMessage.warning('请输入问题')
    return
  }
  messages.value.push({ id: Date.now(), role: 'user', content })
  messages.value.push({
    id: Date.now() + 1,
    role: 'assistant',
    content: '问题已收到。后续接入 ai-service 后，这里会返回 RAG 检索和课程引用结果。'
  })
  draft.value = ''
}
</script>
