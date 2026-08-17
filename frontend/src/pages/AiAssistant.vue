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
import { mockAiMessages, mockAiPresets } from '../mockData'

interface ChatMessage {
  id: number
  role: 'user' | 'assistant'
  content: string
}

const presets = mockAiPresets
const draft = ref('')
const messages = ref<ChatMessage[]>([...mockAiMessages])

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
    content: '我会先拆解你的问题，再结合课程知识库给出学习建议。当前为演示回答：建议把相关课程加入收藏，并在论坛发一条复盘帖巩固。'
  })
  draft.value = ''
}
</script>
