<template>
  <div class="chat-list" :class="{ open: isOpen }">
    <div class="list-header">
      <h2>Chats</h2>
      <el-button circle size="small">
        <el-icon><Edit /></el-icon>
      </el-button>
    </div>
    
    <el-input
      v-model="searchQuery"
      placeholder="Search Messenger"
      class="search-input"
      size="small"
    >
      <template #prefix>
        <el-icon><Search /></el-icon>
      </template>
    </el-input>
    
    <div class="conversations">
      <div
        v-for="conversation in filteredConversations"
        :key="conversation.id"
        class="conversation-item"
        :class="{ active: selectedId === conversation.id, unread: hasUnread(conversation) }"
        @click="selectConversation(conversation)"
      >
        <div class="conversation-avatar">
          <el-avatar :size="56" :src="getFriend(conversation)?.avatar">
            {{ getFriend(conversation)?.displayName?.charAt(0) }}
          </el-avatar>
          <span 
            v-if="isOnline(getFriend(conversation)?.id)" 
            class="online-indicator"
          ></span>
        </div>
        <div class="conversation-info">
          <div class="conversation-name">
            {{ conversation.name || getFriend(conversation)?.displayName }}
          </div>
          <div class="last-message">
            {{ conversation.lastMessage?.content || 'Start a conversation' }}
            <span v-if="conversation.lastMessageAt" class="time">
              · {{ formatTime(conversation.lastMessageAt) }}
            </span>
          </div>
        </div>
        <div v-if="hasUnread(conversation)" class="unread-badge"></div>
      </div>
      
      <div v-if="filteredConversations.length === 0" class="empty-state">
        <p>No conversations found</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRealtimeChat } from '@/composables/useRealtimeChat'
import type { Conversation, User } from '@/types'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'

dayjs.extend(relativeTime)

const props = defineProps<{
  conversations: Conversation[]
  selectedId?: string
}>()

const emit = defineEmits<{
  select: [conversation: Conversation]
}>()

const { isUserOnline } = useRealtimeChat()
const searchQuery = ref('')

const filteredConversations = computed(() => {
  if (!searchQuery.value) return props.conversations
  
  const query = searchQuery.value.toLowerCase()
  return props.conversations.filter(conv => {
    const friend = getFriend(conv)
    return friend?.displayName?.toLowerCase().includes(query) ||
           conv.name?.toLowerCase().includes(query)
  })
})

function getFriend(conversation: Conversation): User | null {
  // For direct messages, return the other participant
  if (conversation.type === 'DIRECT' && conversation.participants?.length === 2) {
    return conversation.participants.find(p => p.id !== conversation.participants[0].id) || conversation.participants[0]
  }
  return conversation.participants?.[0] || null
}

function isOnline(userId?: string): boolean {
  if (!userId) return false
  return isUserOnline(userId)
}

function hasUnread(conversation: Conversation): boolean {
  // TODO: Track unread status
  return false
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

function selectConversation(conversation: Conversation) {
  emit('select', conversation)
}
</script>

<style lang="scss" scoped>
.chat-list {
  position: fixed;
  bottom: 0;
  right: 12px;
  width: 328px;
  height: 455px;
  background: $bg-color-page;
  border-radius: $border-radius-base $border-radius-base 0 0;
  box-shadow: $box-shadow-lg;
  display: flex;
  flex-direction: column;
  z-index: $z-fixed;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s, transform 0.2s;
  transform: translateY(20px);
  
  &.open {
    opacity: 1;
    pointer-events: auto;
    transform: translateY(0);
  }
}

.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md;
  
  h2 {
    font-size: 24px;
    font-weight: 700;
  }
}

.search-input {
  padding: 0 $spacing-md $spacing-md;
}

.conversations {
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
  
  &.active {
    background-color: $color-primary-light;
  }
}

.conversation-avatar {
  position: relative;
  
  .el-avatar {
    background-color: $color-primary;
  }
  
  .online-indicator {
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

.conversation-info {
  flex: 1;
  min-width: 0;
}

.conversation-name {
  font-weight: 500;
  margin-bottom: 2px;
}

.last-message {
  font-size: $font-size-sm;
  color: $color-text-regular;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  
  .time {
    color: $color-text-secondary;
  }
}

.unread-badge {
  width: 12px;
  height: 12px;
  background-color: $color-primary;
  border-radius: 50%;
  flex-shrink: 0;
}

.empty-state {
  @include flex-center;
  padding: $spacing-xl;
  color: $color-text-regular;
}
</style>
