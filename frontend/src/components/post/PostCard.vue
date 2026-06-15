<template>
  <el-card class="post-card">
    <template #header>
      <div class="post-header">
        <div class="post-author">
          <el-avatar :size="40" :src="post.user.avatar">
            {{ post.user.displayName.charAt(0) }}
          </el-avatar>
          <div class="author-info">
            <router-link :to="`/profile/${post.user.id}`" class="author-name">
              {{ post.user.displayName }}
            </router-link>
            <div class="post-meta">
              <span>{{ formatDate(post.createdAt) }}</span>
              <span>·</span>
              <el-icon><Monitor /></el-icon>
            </div>
          </div>
        </div>
        <el-dropdown trigger="click">
          <el-button circle>
            <el-icon><MoreFilled /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item>Save post</el-dropdown-item>
              <el-dropdown-item>Hide post</el-dropdown-item>
              <el-dropdown-item v-if="isOwner">Edit post</el-dropdown-item>
              <el-dropdown-item v-if="isOwner" divided>Delete post</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </template>
    
    <div class="post-content">
      <p>{{ post.content }}</p>
    </div>
    
    <div v-if="post.media && post.media.length > 0" class="post-media">
      <el-image
        v-for="media in post.media"
        :key="media.id"
        :src="media.thumbnailUrl || media.url"
        :preview-src-list="[media.url]"
        fit="cover"
        class="media-item"
      />
    </div>
    
    <div class="post-stats">
      <div class="stat-item">
        <el-icon color="#1877f2"><Pointer /></el-icon>
        <span>{{ post.likesCount }}</span>
      </div>
      <div class="stat-item">
        <span>{{ post.commentsCount }} comments</span>
        <span>{{ post.sharesCount }} shares</span>
      </div>
    </div>
    
    <el-divider />
    
    <div class="post-actions">
      <el-button class="action-btn" :class="{ active: isLiked }" @click="toggleLike">
        <el-icon><Pointer /></el-icon>
        <span>Like</span>
      </el-button>
      <el-button class="action-btn" @click="showComments = !showComments">
        <el-icon><ChatDotRound /></el-icon>
        <span>Comment</span>
      </el-button>
      <el-button class="action-btn">
        <el-icon><Share /></el-icon>
        <span>Share</span>
      </el-button>
    </div>
    
    <div v-if="showComments" class="post-comments">
      <CommentList :post-id="post.id" />
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import CommentList from '@/components/comment/CommentList.vue'
import type { Post } from '@/types'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'

dayjs.extend(relativeTime)

const props = defineProps<{
  post: Post
}>()

const authStore = useAuthStore()
const showComments = ref(false)
const isLiked = ref(false)

const isOwner = computed(() => authStore.user?.id === props.post.user.id)

function formatDate(date: string) {
  return dayjs(date).fromNow()
}

async function toggleLike() {
  isLiked.value = !isLiked.value
  // TODO: Call API to toggle like
}
</script>

<style lang="scss" scoped>
.post-card {
  :deep(.el-card__header) {
    padding: $spacing-md;
    border-bottom: none;
  }
  
  :deep(.el-card__body) {
    padding: 0 $spacing-md $spacing-md;
  }
}

.post-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.post-author {
  display: flex;
  gap: $spacing-sm;
  
  .el-avatar {
    background-color: $color-primary;
  }
}

.author-info {
  @include flex-column;
}

.author-name {
  font-weight: 600;
  color: $color-text-primary;
  text-decoration: none;
  
  &:hover {
    text-decoration: underline;
  }
}

.post-meta {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  font-size: $font-size-sm;
  color: $color-text-regular;
}

.post-content {
  margin-bottom: $spacing-md;
  
  p {
    font-size: $font-size-base;
    line-height: 1.5;
    white-space: pre-wrap;
  }
}

.post-media {
  display: grid;
  gap: 2px;
  border-radius: $border-radius-base;
  overflow: hidden;
  margin-bottom: $spacing-md;
  
  .media-item {
    width: 100%;
    height: 300px;
    cursor: pointer;
  }
}

.post-stats {
  @include flex-between;
  padding: $spacing-sm 0;
  color: $color-text-regular;
  font-size: $font-size-sm;
  
  .stat-item {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
  }
}

.post-actions {
  display: flex;
  justify-content: space-around;
  
  .action-btn {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: $spacing-sm;
    border: none;
    background: none;
    font-size: $font-size-base;
    font-weight: 500;
    color: $color-text-regular;
    padding: $spacing-sm;
    border-radius: $border-radius-base;
    
    &:hover {
      background-color: $bg-color;
    }
    
    &.active {
      color: $color-primary;
    }
  }
}

.post-comments {
  margin-top: $spacing-md;
  padding-top: $spacing-md;
  border-top: 1px solid $border-color;
}
</style>
