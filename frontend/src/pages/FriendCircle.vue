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
          <span>{{ activeConversation?.online }} 人在线</span>
        </div>
        <div class="chat-thread">
          <article
            v-for="message in activeConversation?.messages || []"
            :key="message.id"
            :class="['chat-bubble', message.side]"
          >
            {{ message.content }}
          </article>
        </div>
        <div class="chat-composer">
          <el-input placeholder="WebSocket 接入后可实时发送消息" disabled />
          <el-button disabled>发送</el-button>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { mockConversations } from '../mockData'

const conversations = mockConversations

const activeId = ref(1)
const activeConversation = computed(() => conversations.find((item) => item.id === activeId.value))
</script>
