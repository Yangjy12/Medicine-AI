import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    userId: 10001,
    nickname: '演示用户'
  })
})
