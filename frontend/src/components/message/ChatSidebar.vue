<template>
  <div class="chat-sidebar">
    <div class="sidebar-header">
      <h2>Chats</h2>
      <el-dropdown trigger="click">
        <el-button circle size="small">
          <el-icon><MoreHorizontal /></el-icon>
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item>New group chat</el-dropdown-item>
            <el-dropdown-item>Message requests</el-dropdown-item>
            <el-dropdown-item>Archived chats</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
    
    <el-input
      v-model="searchQuery"
      placeholder="Search Messenger"
      class="search-input"
    >
      <template #prefix>
        <el-icon><Search /></el-icon>
      </template>
    </el-input>
    
    <div class="conversations-list">
      <div
        v-for="conversation in conversations"
        :key="conversation.id"
        class="conversation-item"
        :class="{ unread: hasUnread(conversation) }"
        @click="$emit('select', conversation)"
      >
        <div class="avatar-wrapper">
          <el-avatar :size="56" :src="getFriend(conversation)?.avatar">
            {{ getFriend(conversation)?.displayName?.charAt(0) }}
          </el-avatar>
          <span 
            v-if="isOnline(getFriend(conversation)?.id)" 
            class="online-dot"
          ></span>
        </div>
        <div class="conversation-content">
          <div class="conversation-header">
            <span class="name">
              {{ conversation.name || getFriend(conversation)?.displayName }}
            </span>
            <span v-if="conversation.lastMessageAt" class="time">
              {{ formatTime(conversation.lastMessageAt) }}
            </span>
          </div>
          <div class="last-message">
            {{ conversation.lastMessage?.content || 'Start a conversation' }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRealtimeChat } from '@/composables/useRealtimeChat'
import type { Conversation, User } from '@/types'
import dayjs from 'dayjs'

const props = defineProps<{
  conversations: Conversation[]
}>()

defineEmits<{
  select: [conversation: Conversation]
}>()

const { isUserOnline } = useRealtimeChat()
const searchQuery = ref('')

function getFriend(conversation: Conversation): User | null {
  if (conversation.type === 'DIRECT' && conversation.participants?.length === 2) {
    return conversation.participants[0]
  }
  return conversation.participants?.[0] || null
}

function isOnline(userId?: string): boolean {
  if (!userId) return false
  return isUserOnline(userId)
}

function hasUnread(conversation: Conversation): boolean {
  return false // TODO: Implement unread tracking
}

function formatTime(date: string): string {
  const now = dayjs()
  const messageDate = dayjs(date)
  
  if (now.diff(messageDate, 'day') === 0) {
    return messageDate.format('HH:mm')
  } else if (now.diff(messageDate, 'day') === 1) {
    return 'Yesterday'
  } else {
    return messageDate.format('DD/MM')
  }
}
</script>

<style lang="scss" scoped>
.chat-sidebar {
  width: 340px;
  background: $bg-color-page;
  border-right: 1px solid $border-color;
  display: flex;
  flex-direction: column;
  height: calc(100vh - #{$header-height});
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md $spacing-md $spacing-sm;
  
  h2 {
    font-size: 24px;
    font-weight: 700;
  }
}

.search-input {
  margin: 0 $spacing-md $spacing-md;
}

.conversations-list {
  flex: 1;
  overflow-y: auto;
  @include scrollbar;
}

.conversation-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  cursor: pointer;
  transition: background-color 0.2s;
  
  &:hover {
    background-color: $bg-color;
  }
  
  &.unread {
    .name, .last-message {
      font-weight: 600;
    }
  }
}

.avatar-wrapper {
  position: relative;
  
  .el-avatar {
    background-color: $color-primary;
  }
  
  .online-dot {
    position: absolute;
    bottom: 2px;
    right: 2px;
    width: 12px;
    height: 12px;
    background-color: $color-success;
    border: 2px solid $bg-color-page;
    border-radius: 50%;
  }
}

.conversation-content {
  flex: 1;
  min-width: 0;
}

.conversation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2px;
  
  .name {
    font-weight: 500;
  }
  
  .time {
    font-size: $font-size-xs;
    color: $color-text-secondary;
  }
}

.last-message {
  font-size: $font-size-sm;
  color: $color-text-regular;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
