import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { postApi } from '@/api/post'
import type { Post, PaginatedResponse } from '@/types'

export const usePostStore = defineStore('post', () => {
  const feedPosts = ref<Post[]>([])
  const currentPage = ref(0)
  const totalPages = ref(1)
  const isLoading = ref(false)
  const hasMore = computed(() => currentPage.value < totalPages.value - 1)

  async function fetchFeed(page = 0) {
    if (isLoading.value) return
    isLoading.value = true
    try {
      const res: PaginatedResponse<Post> = await postApi.getFeed({ page, size: 10 })
      if (page === 0) {
        feedPosts.value = res.content
      } else {
        feedPosts.value.push(...res.content)
      }
      currentPage.value = res.number
      totalPages.value = res.totalPages
    } finally {
      isLoading.value = false
    }
  }

  async function fetchUserPosts(userId: string, page = 0) {
    if (isLoading.value) return
    isLoading.value = true
    try {
      const res: PaginatedResponse<Post> = await postApi.getUserPosts(userId, { page, size: 10 })
      if (page === 0) {
        feedPosts.value = res.content
      } else {
        feedPosts.value.push(...res.content)
      }
      currentPage.value = res.number
      totalPages.value = res.totalPages
    } finally {
      isLoading.value = false
    }
  }

  async function createPost(data: { content: string; visibility?: string; mediaUrls?: string[] }) {
    const post: Post = await postApi.create(data)
    feedPosts.value.unshift(post)
    return post
  }

  async function updatePost(id: string, data: { content?: string; visibility?: string }) {
    const updated: Post = await postApi.update(id, data)
    const idx = feedPosts.value.findIndex((p) => p.id === id)
    if (idx !== -1) feedPosts.value[idx] = updated
    return updated
  }

  async function deletePost(id: string) {
    await postApi.delete(id)
    feedPosts.value = feedPosts.value.filter((p) => p.id !== id)
  }

  async function toggleLike(id: string) {
    const post = feedPosts.value.find((p) => p.id === id)
    if (!post) return
    const liked = post.likedByMe
    await (liked ? postApi.unlike(id) : postApi.like(id))
    post.likedByMe = !liked
    post.likesCount += liked ? -1 : 1
  }

  function reset() {
    feedPosts.value = []
    currentPage.value = 0
    totalPages.value = 1
  }

  return {
    feedPosts, currentPage, totalPages, isLoading, hasMore,
    fetchFeed, fetchUserPosts, createPost, updatePost, deletePost, toggleLike, reset,
  }
})
