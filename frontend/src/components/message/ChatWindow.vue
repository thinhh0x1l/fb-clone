<template>
  <div class="chat-window" :class="{ active: isOpen }">
    <div class="chat-header">
      <div class="chat-user-info">
        <el-avatar :size="32" :src="friend?.avatar">
          {{ friend?.displayName?.charAt(0) }}
        </el-avatar>
        <div>
          <div class="chat-user-name">{{ friend?.displayName }}</div>
          <div class="chat-user-status" :class="{ online: isOnline }">
            {{ isOnline ? 'Active now' : 'Offline' }}
          </div>
        </div>
      </div>
      <div class="chat-actions">
        <el-button circle size="small">
          <el-icon><Phone /></el-icon>
        </el-button>
        <el-button circle size="small">
          <el-icon><VideoCamera /></el-icon>
        </el-button>
        <el-button circle size="small" @click="$emit('close')">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
    </div>

    <div class="chat-messages" ref="messagesContainer">
      <div v-if="isLoading" class="loading">
        <el-icon class="is-loading"><Loading /></el-icon>
      </div>
      
      <div v-else class="messages-list">
        <div
          v-for="message in messages"
          :key="message.id"
          class="message-item"
          :class="{ own: message.sender.id === currentUserId }"
        >
          <el-avatar
            v-if="message.sender.id !== currentUserId"
            :size="32"
            :src="message.sender.avatar"
          >
            {{ message.sender.displayName?.charAt(0) }}
          </el-avatar>
          <div class="message-bubble">
            <div class="message-content">{{ message.content }}</div>
            <div class="message-time">{{ formatTime(message.createdAt) }}</div>
          </div>
        </div>
      </div>

      <div v-if="typingUser" class="typing-indicator">
        <div class="typing-dots">
          <span></span>
          <span></span>
          <span></span>
        </div>
        <span>{{ typingUser }} is typing...</span>
      </div>
    </div>

    <div class="chat-input">
      <el-button circle size="small">
        <el-icon><Plus /></el-icon>
      </el-button>
      <el-input
        v-model="newMessage"
        placeholder="Aa"
        @input="handleTyping"
        @keyup.enter="handleSend"
      >
      </el-input>
      <el-button circle size="small" @click="handleSend">
        <el-icon><Promotion /></el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useChat } from '@/composables/useChat'
import { useRealtimeChat } from '@/composables/useRealtimeChat'
import type { User } from '@/types'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'

dayjs.extend(relativeTime)

const props = defineProps<{
  conversationId: string
  friend: User | null
  isOpen: boolean
}>()

defineEmits<{
  close: []
}>()

const authStore = useAuthStore()
const { isUserOnline } = useRealtimeChat()
const messagesContainer = ref<HTMLElement | null>(null)

const {
  messages,
  isLoading,
  newMessage,
  sendMessage,
  handleTyping,
  handleRead,
  scrollToBottom,
} = useChat(props.conversationId)

const currentUserId = computed(() => authStore.user?.id)
const isOnline = computed(() => props.friend ? isUserOnline(props.friend.id) : false)

const typingUser = computed(() => {
  // Get typing users for this conversation (excluding current user)
  return null // Will be populated from WebSocket
})

function handleSend() {
  if (!newMessage.value.trim()) return
  sendMessage(newMessage.value)
}

function formatTime(date: string) {
  return dayjs(date).format('HH:mm')
}

watch(messages, () => {
  nextTick(() => {
    scrollToBottom()
  })
}, { deep: true })
</script>

<style lang="scss" scoped>
.chat-window {
  position: fixed;
  bottom: 0;
  right: 80px;
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
  
  &.active {
    opacity: 1;
    pointer-events: auto;
    transform: translateY(0);
  }
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-sm $spacing-md;
  border-bottom: 1px solid $border-color;
  cursor: pointer;
  
  .chat-user-info {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    
    .el-avatar {
      background-color: $color-primary;
    }
  }
  
  .chat-user-name {
    font-weight: 600;
    font-size: $font-size-base;
  }
  
  .chat-user-status {
    font-size: $font-size-xs;
    color: $color-text-regular;
    
    &.online {
      color: $color-success;
    }
  }
  
  .chat-actions {
    display: flex;
    gap: $spacing-xs;
  }
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: $spacing-md;
  @include scrollbar;
}

.loading {
  @include flex-center;
  height: 100%;
}

.messages-list {
  @include flex-column;
  gap: $spacing-sm;
}

.message-item {
  display: flex;
  align-items: flex-end;
  gap: $spacing-xs;
  
  &.own {
    flex-direction: row-reverse;
    
    .message-bubble {
      background-color: $color-primary;
      color: white;
      
      .message-time {
        color: rgba(255, 255, 255, 0.7);
      }
    }
  }
  
  .el-avatar {
    background-color: $color-primary;
    flex-shrink: 0;
  }
}

.message-bubble {
  max-width: 65%;
  background-color: $bg-color;
  border-radius: $border-radius-lg;
  padding: $spacing-sm $spacing-md;
  
  .message-content {
    font-size: $font-size-base;
    line-height: 1.4;
    word-wrap: break-word;
  }
  
  .message-time {
    font-size: $font-size-xs;
    color: $color-text-secondary;
    margin-top: 2px;
  }
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-xs 0;
  color: $color-text-regular;
  font-size: $font-size-sm;
}

.typing-dots {
  display: flex;
  gap: 3px;
  
  span {
    width: 6px;
    height: 6px;
    background-color: $color-text-secondary;
    border-radius: 50%;
    animation: typing 1.4s infinite ease-in-out;
    
    &:nth-child(1) { animation-delay: 0s; }
    &:nth-child(2) { animation-delay: 0.2s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-4px); }
}

.chat-input {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  border-top: 1px solid $border-color;
  
  .el-input {
    flex: 1;
  }
}
</style>
