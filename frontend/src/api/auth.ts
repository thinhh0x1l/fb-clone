import request from '@/utils/request'

export const authApi = {
  login(data: { email: string; password: string }) {
    return request.post('/auth/login', data).then((res) => res.data)
  },

  register(data: { username: string; email: string; password: string; displayName: string }) {
    return request.post('/auth/register', data).then((res) => res.data)
  },

  refresh(data: { refreshToken: string }) {
    return request.post('/auth/refresh', data).then((res) => res.data)
  },

  logout() {
    return request.post('/auth/logout')
  },
}
