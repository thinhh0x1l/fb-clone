import { defineStore } from 'pinia'
import { ref } from 'vue'
import { messageApi } from '@/api/message'
import type { Conversation, Message, PaginatedResponse } from '@/types'

export const useMessageStore = defineStore('message', () => {
  const conversations = ref<Conversation[]>([])
  const currentConversation = ref<Conversation | null>(null)
  const messages = ref<Message[]>([])
  const isLoading = ref(false)
  const messagesPage = ref(0)
  const messagesTotalPages = ref(1)
  const unreadCount = ref(0)

  async function fetchConversations(page = 0) {
    isLoading.value = true
    try {
      const res: PaginatedResponse<Conversation> = await messageApi.getConversations({ page, size: 20 })
      conversations.value = page === 0 ? res.content : [...conversations.value, ...res.content]
    } finally {
      isLoading.value = false
    }
  }

  async function createConversation(data: { participantIds: string[]; name?: string; initialMessage?: string }) {
    const conv: Conversation = await messageApi.createConversation(data)
    conversations.value.unshift(conv)
    return conv
  }

  async function selectConversation(id: string) {
    currentConversation.value = await messageApi.getConversation(id)
    messages.value = []
    messagesPage.value = 0
    messagesTotalPages.value = 1
    await fetchMessages(id)
  }

  async function fetchMessages(conversationId: string, page = 0) {
    if (isLoading.value) return
    isLoading.value = true
    try {
      const res: PaginatedResponse<Message> = await messageApi.getMessages(conversationId, { page, size: 50 })
      if (page === 0) {
        messages.value = res.content
      } else {
        messages.value = [...res.content, ...messages.value]
      }
      messagesPage.value = res.number
      messagesTotalPages.value = res.totalPages
    } finally {
      isLoading.value = false
    }
  }

  async function sendMessage(conversationId: string, content: string, type = 'TEXT') {
    const msg: Message = await messageApi.sendMessage(conversationId, { content, type })
    messages.value.push(msg)
    return msg
  }

  async function markAsRead(conversationId: string) {
    await messageApi.markAsRead(conversationId)
  }

  function addMessageToCurrent(msg: Message) {
    messages.value.push(msg)
  }

  function prependMessages(newMessages: Message[]) {
    messages.value = [...newMessages, ...messages.value]
  }

  return {
    conversations, currentConversation, messages, isLoading, unreadCount,
    fetchConversations, createConversation, selectConversation,
    fetchMessages, sendMessage, markAsRead, addMessageToCurrent, prependMessages,
  }
})
