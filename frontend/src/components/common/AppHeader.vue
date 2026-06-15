<template>
  <header class="app-header">
    <div class="header-left">
      <router-link to="/" class="logo">
        <el-icon :size="40" color="#1877f2"><Histogram /></el-icon>
      </router-link>
      <el-input
        v-model="searchQuery"
        placeholder="Search Facebook"
        class="search-input"
        size="large"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>
    
    <nav class="header-center">
      <router-link to="/" class="nav-item" :class="{ active: $route.path === '/' }">
        <el-icon :size="24"><HomeFilled /></el-icon>
      </router-link>
      <router-link to="/friends" class="nav-item">
        <el-icon :size="24"><User /></el-icon>
      </router-link>
      <router-link to="/messages" class="nav-item">
        <el-icon :size="24"><ChatDotRound /></el-icon>
      </router-link>
    </nav>
    
    <div class="header-right">
      <el-badge :value="unreadNotifications" :hidden="unreadNotifications === 0" class="notification-badge">
        <el-button circle size="large" @click="$router.push('/notifications')">
          <el-icon :size="20"><Bell /></el-icon>
        </el-button>
      </el-badge>
      
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-avatar">
          <el-avatar :size="40" :src="authStore.user?.avatar">
            {{ authStore.user?.displayName?.charAt(0) }}
          </el-avatar>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>
              Profile
            </el-dropdown-item>
            <el-dropdown-item command="settings">
              <el-icon><Setting /></el-icon>
              Settings
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>
              Logout
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const searchQuery = ref('')
const unreadNotifications = ref(0)

function handleSearch() {
  if (searchQuery.value.trim()) {
    router.push({ path: '/search', query: { q: searchQuery.value } })
  }
}

function handleCommand(command: string) {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      router.push('/settings')
      break
    case 'logout':
      authStore.logout()
      break
  }
}
</script>

<style lang="scss" scoped>
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: $header-height;
  background: $bg-color-page;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 $spacing-md;
  z-index: $z-sticky;
}

.header-left {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  
  .logo {
    display: flex;
    align-items: center;
  }
  
  .search-input {
    width: 240px;
    
    :deep(.el-input__wrapper) {
      border-radius: 50px;
      background-color: $bg-color;
    }
  }
}

.header-center {
  display: flex;
  gap: $spacing-lg;
  
  .nav-item {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 110px;
    height: $header-height;
    color: $color-text-regular;
    border-bottom: 3px solid transparent;
    transition: all 0.2s;
    
    &:hover {
      color: $color-primary;
    }
    
    &.active {
      color: $color-primary;
      border-bottom-color: $color-primary;
    }
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  
  .user-avatar {
    cursor: pointer;
    
    .el-avatar {
      background-color: $color-primary;
    }
  }
}

@media (max-width: $breakpoint-md) {
  .search-input {
    display: none;
  }
  
  .header-center {
    gap: $spacing-md;
    
    .nav-item {
      width: auto;
      padding: 0 $spacing-sm;
    }
  }
}
</style>
