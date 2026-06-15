<template>
  <aside class="app-sidebar">
    <nav class="sidebar-nav">
      <router-link to="/profile" class="sidebar-item">
        <el-avatar :size="36" :src="authStore.user?.avatar">
          {{ authStore.user?.displayName?.charAt(0) }}
        </el-avatar>
        <span>{{ authStore.user?.displayName }}</span>
      </router-link>
      
      <router-link to="/friends" class="sidebar-item">
        <el-icon :size="28" color="#1877f2"><User /></el-icon>
        <span>Friends</span>
      </router-link>
      
      <router-link to="/groups" class="sidebar-item">
        <el-icon :size="28" color="#1877f2"><UserFilled /></el-icon>
        <span>Groups</span>
      </router-link>
      
      <router-link to="/marketplace" class="sidebar-item">
        <el-icon :size="28" color="#1877f2"><ShoppingCart /></el-icon>
        <span>Marketplace</span>
      </router-link>
      
      <router-link to="/watch" class="sidebar-item">
        <el-icon :size="28" color="#1877f2"><VideoPlay /></el-icon>
        <span>Watch</span>
      </router-link>
      
      <router-link to="/memories" class="sidebar-item">
        <el-icon :size="28" color="#1877f2"><Clock /></el-icon>
        <span>Memories</span>
      </router-link>
      
      <router-link to="/saved" class="sidebar-item">
        <el-icon :size="28" color="#1877f2"><Bookmark /></el-icon>
        <span>Saved</span>
      </router-link>
      
      <router-link to="/events" class="sidebar-item">
        <el-icon :size="28" color="#1877f2"><Calendar /></el-icon>
        <span>Events</span>
      </router-link>
    </nav>
  </aside>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
</script>

<style lang="scss" scoped>
.app-sidebar {
  position: fixed;
  left: 0;
  top: $header-height;
  width: $sidebar-width;
  height: calc(100vh - #{$header-height});
  background: $bg-color-page;
  padding: $spacing-md 0;
  overflow-y: auto;
  @include scrollbar;
}

.sidebar-nav {
  @include flex-column;
  gap: $spacing-xs;
}

.sidebar-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-sm $spacing-md;
  color: $color-text-primary;
  text-decoration: none;
  border-radius: $border-radius-base;
  transition: background-color 0.2s;
  
  &:hover {
    background-color: $bg-color;
    text-decoration: none;
  }
  
  span {
    font-size: $font-size-lg;
    font-weight: 500;
  }
}

@media (max-width: $breakpoint-lg) {
  .app-sidebar {
    display: none;
  }
}
</style>
