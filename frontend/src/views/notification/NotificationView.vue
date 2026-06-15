<template>
  <div class="notification-page">
    <div class="page-header">
      <h2>Notifications</h2>
      <el-button link>Mark all as read</el-button>
    </div>
    <div class="notification-list">
      <div
        v-for="notification in notifications"
        :key="notification.id"
        class="notification-item"
        :class="{ unread: !notification.read }"
      >
        <el-avatar :size="56" :src="notification.actor?.avatar">
          {{ notification.actor?.displayName?.charAt(0) }}
        </el-avatar>
        <div class="notification-content">
          <p>{{ notification.message }}</p>
          <span class="notification-time">{{ formatDate(notification.createdAt) }}</span>
        </div>
      </div>
      <el-empty v-if="notifications.length === 0" description="No notifications" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { Notification } from '@/types'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'

dayjs.extend(relativeTime)

const notifications = ref<Notification[]>([])

function formatDate(date: string) {
  return dayjs(date).fromNow()
}
</script>

<style lang="scss" scoped>
.notification-page {
  max-width: 680px;
  margin: 0 auto;
  padding: $spacing-md;
  margin-top: $header-height;
}

.page-header {
  @include flex-between;
  margin-bottom: $spacing-md;
  
  h2 {
    font-size: 24px;
  }
}

.notification-list {
  @include flex-column;
  gap: $spacing-sm;
}

.notification-item {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-md;
  border-radius: $border-radius-base;
  cursor: pointer;
  transition: background-color 0.2s;
  
  &:hover {
    background-color: $bg-color;
  }
  
  &.unread {
    background-color: $color-primary-light;
  }
  
  .el-avatar {
    background-color: $color-primary;
    flex-shrink: 0;
  }
}

.notification-content {
  flex: 1;
  
  p {
    margin-bottom: $spacing-xs;
  }
}

.notification-time {
  font-size: $font-size-sm;
  color: $color-primary;
  font-weight: 500;
}
</style>
