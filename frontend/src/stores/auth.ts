import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import type { User } from '@/types'
import { useRouter } from 'vue-router'

export const useAuthStore = defineStore('auth', () => {
  const router = useRouter()
  const user = ref<User | null>(null)
  const accessToken = ref<string | null>(localStorage.getItem('accessToken'))
  const refreshToken = ref<string | null>(localStorage.getItem('refreshToken'))

  const isAuthenticated = computed(() => !!accessToken.value)

  async function login(email: string, password: string) {
    const response = await authApi.login({ email, password })
    setAuth(response)
    router.push('/')
  }

  async function register(data: { username: string; email: string; password: string; displayName: string }) {
    const response = await authApi.register(data)
    setAuth(response)
    router.push('/')
  }

  async function refresh() {
    if (!refreshToken.value) return
    try {
      const response = await authApi.refresh({ refreshToken: refreshToken.value })
      setAuth(response)
    } catch {
      logout()
    }
  }

  function logout() {
    user.value = null
    accessToken.value = null
    refreshToken.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    router.push('/login')
  }

  function setAuth(response: { accessToken: string; refreshToken: string; user: User }) {
    user.value = response.user
    accessToken.value = response.accessToken
    refreshToken.value = response.refreshToken
    localStorage.setItem('accessToken', response.accessToken)
    localStorage.setItem('refreshToken', response.refreshToken)
  }

  return {
    user,
    accessToken,
    refreshToken,
    isAuthenticated,
    login,
    register,
    refresh,
    logout,
  }
})
