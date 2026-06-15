import request from '@/utils/request'

export const messageApi = {
  getConversations(params?: { page?: number; size?: number }) {
    return request.get('/conversations', { params }).then((res) => res.data)
  },

  createConversation(data: { participantIds: string[]; name?: string; initialMessage?: string }) {
    return request.post('/conversations', data).then((res) => res.data)
  },

  getConversation(id: string) {
    return request.get(`/conversations/${id}`).then((res) => res.data)
  },

  getMessages(conversationId: string, params?: { page?: number; size?: number }) {
    return request.get(`/conversations/${conversationId}/messages`, { params }).then((res) => res.data)
  },

  sendMessage(conversationId: string, data: { content: string; type?: string }) {
    return request.post(`/conversations/${conversationId}/messages`, data).then((res) => res.data)
  },

  markAsRead(conversationId: string) {
    return request.put(`/conversations/${conversationId}/read`).then((res) => res.data)
  },
}
