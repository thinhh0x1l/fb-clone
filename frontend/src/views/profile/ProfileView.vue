<template>
  <div class="profile-page">
    <div class="profile-cover">
      <el-image
        :src="user?.coverPhoto || 'https://via.placeholder.com/820x312'"
        fit="cover"
        class="cover-image"
      />
    </div>
    <div class="profile-info">
      <el-avatar :size="168" :src="user?.avatar" class="profile-avatar">
        {{ user?.displayName?.charAt(0) }}
      </el-avatar>
      <div class="profile-details">
        <h1>{{ user?.displayName }}</h1>
        <p class="friends-count">{{ user?.friendsCount }} friends</p>
      </div>
      <div class="profile-actions">
        <el-button v-if="isOwner" type="primary">
          <el-icon><EditPen /></el-icon>
          Edit profile
        </el-button>
        <template v-else>
          <el-button type="primary">
            <el-icon><Plus /></el-icon>
            Add friend
          </el-button>
          <el-button>
            <el-icon><ChatDotRound /></el-icon>
            Message
          </el-button>
        </template>
      </div>
    </div>
    <el-divider />
    <div class="profile-content">
      <div class="profile-sidebar">
        <el-card class="intro-card">
          <h3>Intro</h3>
          <p>{{ user?.bio || 'No bio yet' }}</p>
          <div v-if="user?.workplace" class="info-item">
            <el-icon><OfficeBuilding /></el-icon>
            <span>Works at {{ user.workplace }}</span>
          </div>
          <div v-if="user?.education" class="info-item">
            <el-icon><Reading /></el-icon>
            <span>Studied at {{ user.education }}</span>
          </div>
          <div v-if="user?.location" class="info-item">
            <el-icon><Location /></el-icon>
            <span>Lives in {{ user.location }}</span>
          </div>
        </el-card>
      </div>
      <div class="profile-main">
        <CreatePost v-if="isOwner" />
        <NewsFeed :user-id="route.params.id as string" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { userApi } from '@/api/user'
import CreatePost from '@/components/post/CreatePost.vue'
import NewsFeed from '@/components/feed/NewsFeed.vue'
import type { User } from '@/types'

const route = useRoute()
const authStore = useAuthStore()
const user = ref<User | null>(null)

const isOwner = computed(() => {
  const userId = route.params.id as string
  return !userId || userId === authStore.user?.id
})

onMounted(async () => {
  const userId = route.params.id as string
  if (userId && userId !== authStore.user?.id) {
    user.value = await userApi.getById(userId)
  } else {
    user.value = authStore.user
  }
})
</script>

<style lang="scss" scoped>
.profile-page {
  max-width: 940px;
  margin: 0 auto;
  background: $bg-color-page;
}

.profile-cover {
  .cover-image {
    width: 100%;
    height: 350px;
  }
}

.profile-info {
  display: flex;
  align-items: flex-end;
  gap: $spacing-md;
  padding: 0 $spacing-lg;
  margin-top: -84px;
  
  .profile-avatar {
    border: 4px solid $bg-color-page;
    background-color: $color-primary;
  }
  
  .profile-details {
    flex: 1;
    
    h1 {
      font-size: 32px;
      margin-bottom: $spacing-xs;
    }
    
    .friends-count {
      color: $color-text-regular;
    }
  }
  
  .profile-actions {
    display: flex;
    gap: $spacing-sm;
  }
}

.profile-content {
  display: flex;
  gap: $spacing-md;
  padding: $spacing-md $spacing-lg;
}

.profile-sidebar {
  width: 360px;
  flex-shrink: 0;
  
  .intro-card {
    h3 {
      margin-bottom: $spacing-sm;
    }
    
    p {
      color: $color-text-regular;
      margin-bottom: $spacing-md;
    }
  }
}

.info-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-bottom: $spacing-sm;
  color: $color-text-regular;
}

.profile-main {
  flex: 1;
}

@media (max-width: $breakpoint-md) {
  .profile-info {
    flex-direction: column;
    align-items: center;
    text-align: center;
    padding-top: $spacing-md;
  }
  
  .profile-content {
    flex-direction: column;
  }
  
  .profile-sidebar {
    width: 100%;
  }
}
</style>
