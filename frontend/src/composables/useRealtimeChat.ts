import { ref, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '@/stores/auth'

export interface ChatMessage {
  id: string
  conversationId: string
  sender: {
    id: string
    username: string
    displayName: string
    avatar: string
  }
  content: string
  type: 'TEXT' | 'IMAGE' | 'FILE' | 'LINK'
  read: boolean
  createdAt: string
}

export interface TypingIndicator {
  conversationId: string
  userId: string
  isTyping: boolean
}

export interface PresenceStatus {
  userId: string
  status: 'online' | 'offline'
  lastSeen?: string
}

export function useRealtimeChat() {
  const authStore = useAuthStore()
  const stompClient = ref<any>(null)
  const isConnected = ref(false)
  const onlineUsers = ref<Set<string>>(new Set())
  const typingUsers = ref<Map<string, Set<string>>>(new Map())
  const messages = ref<Map<string, ChatMessage[]>>(new Map())

  let reconnectAttempts = 0
  const maxReconnectAttempts = 5
  const reconnectDelay = 3000

  async function connect() {
    if (!authStore.accessToken) return

    try {
      const { Client } = await import('@stomp/stompjs')
      
      const client = new Client({
        brokerURL: `ws://localhost:8080/ws`,
        connectHeaders: {
          Authorization: `Bearer ${authStore.accessToken}`,
        },
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000,
        reconnectDelay: 5000,
        onConnect: () => {
          console.log('WebSocket connected')
          isConnected.value = true
          reconnectAttempts = 0
          subscribeToTopics()
        },
        onDisconnect: () => {
          console.log('WebSocket disconnected')
          isConnected.value = false
        },
        onStompError: (frame) => {
          console.error('STOMP error:', frame.headers['message'])
          console.error('Details:', frame.body)
          isConnected.value = false
        },
      })

      client.activate()
      stompClient.value = client
    } catch (error) {
      console.error('Failed to connect WebSocket:', error)
    }
  }

  function subscribeToTopics() {
    if (!stompClient.value?.connected) return

    const userId = authStore.user?.id

    // Subscribe to user's personal notifications
    stompClient.value.subscribe(`/user/queue/notification`, (message: any) => {
      const data = JSON.parse(message.body)
      console.log('Notification received:', data)
      // Emit to notification system
    })

    // Subscribe to friend requests
    stompClient.value.subscribe(`/user/queue/friend.request`, (message: any) => {
      const data = JSON.parse(message.body)
      console.log('Friend request received:', data)
    })

    // Subscribe to friend acceptances
    stompClient.value.subscribe(`/user/queue/friend.accept`, (message: any) => {
      const data = JSON.parse(message.body)
      console.log('Friend accepted:', data)
    })

    // Subscribe to presence updates
    stompClient.value.subscribe('/topic/presence', (message: any) => {
      const data: PresenceStatus = JSON.parse(message.body)
      if (data.status === 'online') {
        onlineUsers.value.add(data.userId)
      } else {
        onlineUsers.value.delete(data.userId)
      }
    })

    // Subscribe to unread notification count
    stompClient.value.subscribe('/user/queue/notification.unread', (message: any) => {
      const data = JSON.parse(message.body)
      console.log('Unread count:', data.count)
    })
  }

  function subscribeToConversation(conversationId: string) {
    if (!stompClient.value?.connected) return

    // Subscribe to conversation messages
    stompClient.value.subscribe(`/topic/conversation.${conversationId}.messages`, (message: any) => {
      const data = JSON.parse(message.body)
      if (data.type === 'NEW_MESSAGE') {
        const convMessages = messages.value.get(conversationId) || []
        convMessages.push(data.message)
        messages.value.set(conversationId, [...convMessages])
      }
    })

    // Subscribe to typing indicators
    stompClient.value.subscribe(`/topic/conversation.${conversationId}.typing`, (message: any) => {
      const data: TypingIndicator = JSON.parse(message.body)
      const convTyping = typingUsers.value.get(conversationId) || new Set()
      if (data.isTyping) {
        convTyping.add(data.userId)
      } else {
        convTyping.delete(data.userId)
      }
      typingUsers.value.set(conversationId, new Set(convTyping))
    })

    // Subscribe to read receipts
    stompClient.value.subscribe(`/topic/conversation.${conversationId}.read`, (message: any) => {
      const data = JSON.parse(message.body)
      console.log('Read receipt:', data)
    })
  }

  function sendMessage(conversationId: string, content: string, type: string = 'TEXT') {
    if (!stompClient.value?.connected) return

    stompClient.value.publish({
      destination: `/app/chat.${conversationId}.send`,
      body: JSON.stringify({ content, type }),
    })
  }

  function sendTypingIndicator(conversationId: string, isTyping: boolean) {
    if (!stompClient.value?.connected) return

    stompClient.value.publish({
      destination: `/app/chat.${conversationId}.typing`,
      body: JSON.stringify({ isTyping }),
    })
  }

  function markAsRead(conversationId: string) {
    if (!stompClient.value?.connected) return

    stompClient.value.publish({
      destination: `/app/chat.${conversationId}.read`,
      body: JSON.stringify({}),
    })
  }

  function sendFriendRequest(targetUserId: string) {
    if (!stompClient.value?.connected) return

    stompClient.value.publish({
      destination: '/app.friend.request',
      body: JSON.stringify({ userId: targetUserId }),
    })
  }

  function disconnect() {
    stompClient.value?.deactivate()
    stompClient.value = null
    isConnected.value = false
  }

  function getConversationMessages(conversationId: string): ChatMessage[] {
    return messages.value.get(conversationId) || []
  }

  function isUserTyping(conversationId: string, userId: string): boolean {
    return typingUsers.value.get(conversationId)?.has(userId) || false
  }

  function isUserOnline(userId: string): boolean {
    return onlineUsers.value.has(userId)
  }

  onMounted(() => {
    connect()
  })

  onUnmounted(() => {
    disconnect()
  })

  return {
    isConnected,
    onlineUsers,
    typingUsers,
    messages,
    connect,
    disconnect,
    subscribeToConversation,
    sendMessage,
    sendTypingIndicator,
    markAsRead,
    sendFriendRequest,
    getConversationMessages,
    isUserTyping,
    isUserOnline,
  }
}
