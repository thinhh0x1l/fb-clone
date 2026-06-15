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
        <el-button :loading="loading" @click="loadMore">
          Load more
        </el-button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { usePostStore } from '@/stores/post'
import PostCard from '@/components/post/PostCard.vue'

const props = defineProps<{
  userId?: string
}>()

const postStore = usePostStore()

const loading = computed(() => postStore.isLoading)
const posts = computed(() => postStore.feedPosts)
const hasMore = computed(() => postStore.hasMore)

onMounted(() => {
  if (props.userId) {
    postStore.fetchUserPosts(props.userId)
  } else {
    postStore.fetchFeed()
  }
})

async function loadMore() {
  if (props.userId) {
    await postStore.fetchUserPosts(props.userId, postStore.currentPage + 1)
  } else {
    await postStore.fetchFeed(postStore.currentPage + 1)
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
