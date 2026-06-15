import { ref, computed, watch } from 'vue'
import { useRealtimeChat, type ChatMessage } from './useRealtimeChat'
import { messageApi } from '@/api/message'

export function useChat(conversationId: string) {
  const {
    sendMessage: wsSendMessage,
    sendTypingIndicator,
    markAsRead,
    subscribeToConversation,
    getConversationMessages,
    isUserTyping,
  } = useRealtimeChat()

  const conversationMessages = ref<ChatMessage[]>([])
  const isLoading = ref(false)
  const isTyping = ref(false)
  const newMessage = ref('')
  let typingTimeout: ReturnType<typeof setTimeout> | null = null

  // Initialize
  subscribeToConversation(conversationId)
  loadMessages()

  async function loadMessages() {
    isLoading.value = true
    try {
      const response = await messageApi.getMessages(conversationId, { page: 0, size: 50 })
      conversationMessages.value = response.data?.content || []
    } catch (error) {
      console.error('Failed to load messages:', error)
    } finally {
      isLoading.value = false
    }
  }

  function sendMessage(content: string, type: string = 'TEXT') {
    if (!content.trim()) return

    wsSendMessage(conversationId, content, type)
    newMessage.value = ''
    stopTyping()
  }

  function handleTyping() {
    if (!isTyping.value) {
      isTyping.value = true
      sendTypingIndicator(conversationId, true)
    }

    // Reset typing timeout
    if (typingTimeout) clearTimeout(typingTimeout)
    typingTimeout = setTimeout(() => {
      stopTyping()
    }, 2000)
  }

  function stopTyping() {
    if (isTyping.value) {
      isTyping.value = false
      sendTypingIndicator(conversationId, false)
    }
    if (typingTimeout) {
      clearTimeout(typingTimeout)
      typingTimeout = null
    }
  }

  function handleRead() {
    markAsRead(conversationId)
  }

  function scrollToBottom() {
    const container = document.querySelector('.chat-messages')
    if (container) {
      container.scrollTop = container.scrollHeight
    }
  }

  return {
    messages: conversationMessages,
    isLoading,
    isTyping,
    newMessage,
    sendMessage,
    handleTyping,
    stopTyping,
    handleRead,
    scrollToBottom,
    loadMessages,
  }
}
