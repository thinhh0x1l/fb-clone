<template>
  <div class="home-layout">
    <AppHeader />
    <div class="home-content">
      <AppSidebar />
      <main class="main-feed">
        <CreatePost />
        <NewsFeed />
      </main>
      <RightSidebar />
    </div>
  </div>
</template>

<script setup lang="ts">
import AppHeader from '@/components/common/AppHeader.vue'
import AppSidebar from '@/components/common/AppSidebar.vue'
import RightSidebar from '@/components/feed/RightSidebar.vue'
import CreatePost from '@/components/post/CreatePost.vue'
import NewsFeed from '@/components/feed/NewsFeed.vue'
</script>

<style lang="scss" scoped>
.home-layout {
  min-height: 100vh;
  background-color: $bg-color;
}

.home-content {
  display: flex;
  padding-top: $header-height;
  max-width: 1400px;
  margin: 0 auto;
}

.main-feed {
  flex: 1;
  max-width: 680px;
  margin: 0 auto;
  padding: $spacing-lg $spacing-md;
}

@media (max-width: $breakpoint-lg) {
  .main-feed {
    max-width: 100%;
  }
}
</style>
