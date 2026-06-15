import request from '@/utils/request'

export const postApi = {
  create(data: { content: string; visibility?: string; mediaUrls?: string[] }) {
    return request.post('/posts', data).then((res) => res.data)
  },

  getById(id: string) {
    return request.get(`/posts/${id}`).then((res) => res.data)
  },

  getFeed(params?: { page?: number; size?: number }) {
    return request.get('/posts', { params }).then((res) => res.data)
  },

  getUserPosts(userId: string, params?: { page?: number; size?: number }) {
    return request.get(`/posts/user/${userId}`, { params }).then((res) => res.data)
  },

  update(id: string, data: { content?: string; visibility?: string }) {
    return request.put(`/posts/${id}`, data).then((res) => res.data)
  },

  delete(id: string) {
    return request.delete(`/posts/${id}`).then((res) => res.data)
  },

  like(id: string) {
    return request.post(`/posts/${id}/reactions`, { type: 'LIKE' }).then((res) => res.data)
  },

  unlike(id: string) {
    return request.delete(`/posts/${id}/reactions`).then((res) => res.data)
  },

  getComments(id: string, params?: { page?: number; size?: number }) {
    return request.get(`/posts/${id}/comments`, { params }).then((res) => res.data)
  },

  addComment(id: string, data: { content: string; parentId?: string }) {
    return request.post(`/posts/${id}/comments`, data).then((res) => res.data)
  },

  search(query: string, params?: { page?: number; size?: number }) {
    return request.get('/posts/search', { params: { q: query, ...params } }).then((res) => res.data)
  },
}
