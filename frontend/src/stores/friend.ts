import { defineStore } from 'pinia'
import { ref } from 'vue'
import { friendApi } from '@/api/friend'
import type { User, FriendRequest, PaginatedResponse } from '@/types'

export const useFriendStore = defineStore('friend', () => {
  const friends = ref<User[]>([])
  const pendingRequests = ref<FriendRequest[]>([])
  const suggestions = ref<User[]>([])
  const isLoading = ref(false)

  async function fetchFriends(page = 0) {
    isLoading.value = true
    try {
      const res: PaginatedResponse<User> = await friendApi.getFriends({ page, size: 20 })
      friends.value = page === 0 ? res.content : [...friends.value, ...res.content]
    } finally {
      isLoading.value = false
    }
  }

  async function fetchPendingRequests(page = 0) {
    isLoading.value = true
    try {
      const res: PaginatedResponse<FriendRequest> = await friendApi.getPendingRequests({ page, size: 20 })
      pendingRequests.value = page === 0 ? res.content : [...pendingRequests.value, ...res.content]
    } finally {
      isLoading.value = false
    }
  }

  async function fetchSuggestions(limit = 10) {
    suggestions.value = await friendApi.getSuggestions(limit)
  }

  async function sendRequest(userId: string, message?: string) {
    await friendApi.sendRequest(userId, message)
  }

  async function acceptRequest(requestId: string) {
    await friendApi.acceptRequest(requestId)
    pendingRequests.value = pendingRequests.value.filter((r) => r.id !== requestId)
  }

  async function rejectRequest(requestId: string) {
    await friendApi.rejectRequest(requestId)
    pendingRequests.value = pendingRequests.value.filter((r) => r.id !== requestId)
  }

  async function unfriend(friendId: string) {
    await friendApi.unfriend(friendId)
    friends.value = friends.value.filter((f) => f.id !== friendId)
  }

  return {
    friends, pendingRequests, suggestions, isLoading,
    fetchFriends, fetchPendingRequests, fetchSuggestions,
    sendRequest, acceptRequest, rejectRequest, unfriend,
  }
})
