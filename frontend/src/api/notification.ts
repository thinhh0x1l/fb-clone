import request from '@/utils/request'

export const notificationApi = {
  getNotifications(params?: { page?: number; size?: number }) {
    return request.get('/notifications', { params }).then((res) => res.data)
  },

  getUnreadCount() {
    return request.get('/notifications/unread-count').then((res) => res.data)
  },

  markAsRead(id: string) {
    return request.put(`/notifications/${id}/read`).then((res) => res.data)
  },

  markAllAsRead() {
    return request.put('/notifications/read-all').then((res) => res.data)
  },
}
