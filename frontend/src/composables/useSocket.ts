import { ref, onMounted, onUnmounted } from 'vue'
import { io, Socket } from 'socket.io-client'
import { useAuthStore } from '@/stores/auth'

export function useSocket() {
  const authStore = useAuthStore()
  const socket = ref<Socket | null>(null)
  const isConnected = ref(false)

  function connect() {
    if (!authStore.accessToken) return

    socket.value = io('/', {
      auth: {
        token: authStore.accessToken,
      },
      transports: ['websocket'],
    })

    socket.value.on('connect', () => {
      isConnected.value = true
      console.log('Socket connected')
    })

    socket.value.on('disconnect', () => {
      isConnected.value = false
      console.log('Socket disconnected')
    })

    socket.value.on('connect_error', (error) => {
      console.error('Socket connection error:', error)
    })
  }

  function disconnect() {
    socket.value?.disconnect()
    socket.value = null
    isConnected.value = false
  }

  function emit(event: string, data?: unknown) {
    socket.value?.emit(event, data)
  }

  function on(event: string, callback: (...args: unknown[]) => void) {
    socket.value?.on(event, callback)
  }

  function off(event: string, callback?: (...args: unknown[]) => void) {
    socket.value?.off(event, callback)
  }

  onMounted(() => {
    connect()
  })

  onUnmounted(() => {
    disconnect()
  })

  return {
    socket,
    isConnected,
    connect,
    disconnect,
    emit,
    on,
    off,
  }
}
