import { defineStore } from 'pinia'
import { ref } from 'vue'
import { notificationApi } from '@/api/notification'
import type { Notification, PaginatedResponse } from '@/types'

export const useNotificationStore = defineStore('notification', () => {
  const notifications = ref<Notification[]>([])
  const unreadCount = ref(0)
  const isLoading = ref(false)
  const page = ref(0)
  const totalPages = ref(1)

  async function fetchNotifications(p = 0) {
    if (isLoading.value) return
    isLoading.value = true
    try {
      const res: PaginatedResponse<Notification> = await notificationApi.getNotifications({ page: p, size: 20 })
      notifications.value = p === 0 ? res.content : [...notifications.value, ...res.content]
      page.value = res.number
      totalPages.value = res.totalPages
    } finally {
      isLoading.value = false
    }
  }

  async function fetchUnreadCount() {
    unreadCount.value = await notificationApi.getUnreadCount()
  }

  async function markAsRead(id: string) {
    await notificationApi.markAsRead(id)
    const n = notifications.value.find((n) => n.id === id)
    if (n) n.read = true
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }

  async function markAllAsRead() {
    await notificationApi.markAllAsRead()
    notifications.value.forEach((n) => (n.read = true))
    unreadCount.value = 0
  }

  return {
    notifications, unreadCount, isLoading, page, totalPages,
    fetchNotifications, fetchUnreadCount, markAsRead, markAllAsRead,
  }
})
