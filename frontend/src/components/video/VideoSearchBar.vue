<template>
  <div class="search-bar">
    <Search :size="20" />
    <input
      v-model="model"
      placeholder="搜索课程、讲师、知识点"
      @keydown.enter="$emit('search')"
    />
    <button @click="$emit('search')">搜索</button>
  </div>
</template>

<script setup lang="ts">
import { Search } from 'lucide-vue-next'

const model = defineModel<string>({ default: '' })
defineEmits<{ search: [] }>()
</script>
