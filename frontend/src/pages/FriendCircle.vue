<template>
  <section class="workspace-page">
    <header class="simple-header">
      <p>医友圈</p>
      <h1>学习交流</h1>
    </header>

    <div class="circle-layout">
      <aside class="section-block conversation-list">
        <button v-for="conversation in conversations" :key="conversation.id" :class="{ active: activeId === conversation.id }" @click="activeId = conversation.id">
          <div class="avatar small">友</div>
          <div>
            <strong>{{ conversation.name }}</strong>
            <span>{{ conversation.preview }}</span>
          </div>
        </button>
      </aside>

      <section class="section-block chat-panel">
        <div class="chat-head">
          <strong>{{ activeConversation?.name }}</strong>
          <span>WebSocket 服务接入中</span>
        </div>
        <div class="chat-thread">
          <article class="chat-bubble peer">医友圈后端服务接入后，可在这里进行实时学习交流。</article>
          <article class="chat-bubble self">论坛已可用于帖子讨论，实时聊天会在下一阶段接入。</article>
        </div>
        <div class="chat-composer">
          <el-input placeholder="实时聊天服务接入中" disabled />
          <el-button disabled>发送</el-button>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

const conversations = [
  { id: 1, name: '中医基础学习群', preview: '阴阳五行、藏象理论交流' },
  { id: 2, name: '针灸推拿研习', preview: '经络腧穴学习讨论' },
  { id: 3, name: '经方读书会', preview: '伤寒论条文共读' }
]

const activeId = ref(1)
const activeConversation = computed(() => conversations.find((item) => item.id === activeId.value))
</script>
