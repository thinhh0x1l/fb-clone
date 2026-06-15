import request from '@/utils/request'

export const userApi = {
  getMe() {
    return request.get('/users/me').then((res) => res.data)
  },

  getById(id: string) {
    return request.get(`/users/${id}`).then((res) => res.data)
  },

  getByUsername(username: string) {
    return request.get(`/users/username/${username}`).then((res) => res.data)
  },

  updateProfile(data: {
    displayName?: string
    bio?: string
    gender?: string
    birthday?: string
    location?: string
    workplace?: string
    education?: string
  }) {
    return request.put('/users/me', data).then((res) => res.data)
  },

  updateAvatar(avatarUrl: string) {
    return request.put('/users/me/avatar', null, { params: { avatarUrl } }).then((res) => res.data)
  },

  updateCoverPhoto(coverPhotoUrl: string) {
    return request.put('/users/me/cover', null, { params: { coverPhotoUrl } }).then((res) => res.data)
  },

  search(query: string, params?: { page?: number; size?: number }) {
    return request.get('/users/search', { params: { q: query, ...params } }).then((res) => res.data)
  },
}
