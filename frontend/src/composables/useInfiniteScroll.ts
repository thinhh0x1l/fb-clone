import { ref, onMounted, onUnmounted } from 'vue'

export function useInfiniteScroll(callback: () => Promise<void>, options?: { threshold?: number }) {
  const isLoading = ref(false)
  const hasMore = ref(true)
  const threshold = options?.threshold || 200

  function handleScroll() {
    if (isLoading.value || !hasMore.value) return

    const scrollTop = document.documentElement.scrollTop || document.body.scrollTop
    const scrollHeight = document.documentElement.scrollHeight
    const clientHeight = document.documentElement.clientHeight

    if (scrollTop + clientHeight >= scrollHeight - threshold) {
      loadMore()
    }
  }

  async function loadMore() {
    if (isLoading.value || !hasMore.value) return
    
    isLoading.value = true
    try {
      await callback()
    } finally {
      isLoading.value = false
    }
  }

  onMounted(() => {
    window.addEventListener('scroll', handleScroll, { passive: true })
  })

  onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll)
  })

  return {
    isLoading,
    hasMore,
    loadMore,
  }
}
