import request from '@/utils/request'

export const friendApi = {
  sendRequest(userId: string, message?: string) {
    return request.post('/friends/request', { userId, message }).then((res) => res.data)
  },

  acceptRequest(requestId: string) {
    return request.put(`/friends/${requestId}/accept`).then((res) => res.data)
  },

  rejectRequest(requestId: string) {
    return request.put(`/friends/${requestId}/reject`).then((res) => res.data)
  },

  cancelRequest(requestId: string) {
    return request.delete(`/friends/request/${requestId}`).then((res) => res.data)
  },

  unfriend(friendId: string) {
    return request.delete(`/friends/${friendId}`).then((res) => res.data)
  },

  getFriends(params?: { page?: number; size?: number }) {
    return request.get('/friends', { params }).then((res) => res.data)
  },

  getPendingRequests(params?: { page?: number; size?: number }) {
    return request.get('/friends/requests', { params }).then((res) => res.data)
  },

  getSuggestions(limit?: number) {
    return request.get('/friends/suggestions', { params: { limit } }).then((res) => res.data)
  },
}
