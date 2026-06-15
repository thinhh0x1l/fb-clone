<template>
  <div class="news-feed">
    <div v-if="loading" class="loading-spinner">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
    </div>
    
    <div v-else-if="posts.length === 0" class="empty-feed">
      <el-empty description="No posts yet" />
    </div>
    
    <template v-else>
      <PostCard
        v-for="post in posts"
        :key="post.id"
        :post="post"
      />
      
      <div v-if="hasMore" class="load-more">
        <el-button :loading="loadingMore" @click="loadMore">
          Load more
        </el-button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import PostCard from './PostCard.vue'
import type { Post } from '@/types'

const loading = ref(true)
const loadingMore = ref(false)
const posts = ref<Post[]>([])
const page = ref(0)
const hasMore = ref(true)

onMounted(() => {
  fetchPosts()
})

async function fetchPosts() {
  loading.value = true
  try {
    // TODO: Call API to fetch posts
    posts.value = []
    hasMore.value = false
  } finally {
    loading.value = false
  }
}

async function loadMore() {
  loadingMore.value = true
  try {
    page.value++
    // TODO: Call API to fetch more posts
  } finally {
    loadingMore.value = false
  }
}
</script>

<style lang="scss" scoped>
.news-feed {
  @include flex-column;
  gap: $spacing-md;
}

.loading-spinner {
  @include flex-center;
  padding: $spacing-xl;
}

.load-more {
  @include flex-center;
  padding: $spacing-md;
}
</style>
