<template>
  <div class="search-page">
    <h2>Search Results for "{{ $route.query.q }}"</h2>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="Posts" name="posts">
        <PostCard v-for="post in posts" :key="post.id" :post="post" />
        <el-empty v-if="posts.length === 0" description="No posts found" />
      </el-tab-pane>
      <el-tab-pane label="People" name="people">
        <div v-for="u in users" :key="u.id" class="user-item">
          <router-link :to="`/profile/${u.id}`" class="user-link">
            <el-avatar :size="40" :src="u.avatar">{{ u.displayName.charAt(0) }}</el-avatar>
            <span>{{ u.displayName }}</span>
          </router-link>
        </div>
        <el-empty v-if="users.length === 0" description="No people found" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { postApi } from '@/api/post'
import { userApi } from '@/api/user'
import PostCard from '@/components/post/PostCard.vue'
import type { Post, User } from '@/types'

const route = useRoute()
const activeTab = ref('posts')
const posts = ref<Post[]>([])
const users = ref<User[]>([])

async function doSearch(query: string) {
  if (!query.trim()) return
  posts.value = []
  users.value = []
  try {
    const [postRes, userRes] = await Promise.all([
      postApi.search(query),
      userApi.search(query),
    ])
    posts.value = postRes.content ?? postRes ?? []
    users.value = userRes.content ?? userRes ?? []
  } catch {
    // ignore
  }
}

onMounted(() => {
  if (route.query.q as string) {
    doSearch(route.query.q as string)
  }
})

watch(() => route.query.q, (q) => {
  if (q) doSearch(q as string)
})
</script>

<style lang="scss" scoped>
.search-page {
  max-width: 680px;
  margin: 0 auto;
  padding: $spacing-md;
  margin-top: $header-height;
  
  h2 {
    font-size: 24px;
    margin-bottom: $spacing-md;
  }
}

.user-item {
  padding: $spacing-sm 0;
}

.user-link {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  text-decoration: none;
  color: $color-text-primary;
  font-weight: 500;
  
  &:hover {
    color: $color-primary;
  }
}
</style>
