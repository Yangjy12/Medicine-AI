import { defineStore } from 'pinia'
import type { UserInfo } from '../api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    accessToken: localStorage.getItem('accessToken') || '',
    refreshToken: localStorage.getItem('refreshToken') || '',
    user: JSON.parse(localStorage.getItem('user') || 'null') as UserInfo | null
  }),
  getters: {
    userId: (state) => state.user?.id,
    nickname: (state) => state.user?.nickname || '未登录',
    loggedIn: (state) => Boolean(state.accessToken && state.user),
    isAdmin: (state) => Boolean(state.user?.roles?.includes('ADMIN'))
  },
  actions: {
    setSession(accessToken: string, refreshToken: string, user: UserInfo) {
      this.accessToken = accessToken
      this.refreshToken = refreshToken
      this.user = user
      localStorage.setItem('accessToken', accessToken)
      localStorage.setItem('refreshToken', refreshToken)
      localStorage.setItem('user', JSON.stringify(user))
    },
    setUser(user: UserInfo) {
      this.user = user
      localStorage.setItem('user', JSON.stringify(user))
    },
    clearSession() {
      this.accessToken = ''
      this.refreshToken = ''
      this.user = null
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
    }
  }
})
