<template>
  <div class="search-page">
    <h2>Search Results for "{{ $route.query.q }}"</h2>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="Posts" name="posts">
        <el-empty v-if="posts.length === 0" description="No posts found" />
      </el-tab-pane>
      <el-tab-pane label="People" name="people">
        <el-empty v-if="users.length === 0" description="No people found" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { Post, User } from '@/types'

const activeTab = ref('posts')
const posts = ref<Post[]>([])
const users = ref<User[]>([])
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
</style>
