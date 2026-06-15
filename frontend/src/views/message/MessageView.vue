<template>
  <div class="message-page">
    <div class="conversation-list">
      <div class="list-header">
        <h2>Chats</h2>
        <el-button circle>
          <el-icon><Edit /></el-icon>
        </el-button>
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
      <div class="conversations">
        <div
          v-for="conv in conversations"
          :key="conv.id"
          class="conversation-item"
          :class="{ active: selectedConversation?.id === conv.id }"
          @click="selectConversation(conv)"
        >
          <el-avatar :size="56" :src="conv.participants[0]?.avatar">
            {{ conv.participants[0]?.displayName?.charAt(0) }}
          </el-avatar>
          <div class="conversation-info">
            <div class="conversation-name">{{ conv.name || conv.participants[0]?.displayName }}</div>
            <div class="last-message">{{ conv.lastMessage?.content || 'Start a conversation' }}</div>
          </div>
        </div>
      </div>
    </div>
    <div class="chat-area">
      <div v-if="selectedConversation" class="chat-header">
        <el-avatar :size="40" :src="selectedConversation.participants[0]?.avatar">
          {{ selectedConversation.participants[0]?.displayName?.charAt(0) }}
        </el-avatar>
        <div class="chat-user-info">
          <div class="chat-user-name">{{ selectedConversation.participants[0]?.displayName }}</div>
          <div class="chat-user-status">Active now</div>
        </div>
      </div>
      <div v-else class="empty-chat">
        <el-icon :size="64" color="#1877f2"><ChatDotRound /></el-icon>
        <h3>Your Messenger</h3>
        <p>Send private messages to a friend</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useMessageStore } from '@/stores/message'
import { storeToRefs } from 'pinia'

const messageStore = useMessageStore()
const { conversations, currentConversation: selectedConversation } = storeToRefs(messageStore)
const searchQuery = ref('')

onMounted(() => {
  messageStore.fetchConversations()
})

async function selectConversation(conv: typeof conversations.value[0]) {
  await messageStore.selectConversation(conv.id)
}
</script>

<style lang="scss" scoped>
.message-page {
  display: flex;
  height: calc(100vh - #{$header-height});
  margin-top: $header-height;
  background: $bg-color-page;
}

.conversation-list {
  width: 340px;
  border-right: 1px solid $border-color;
  display: flex;
  flex-direction: column;
}

.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md;
  
  h2 {
    font-size: 24px;
  }
}

.search-input {
  padding: 0 $spacing-md $spacing-md;
}

.conversations {
  flex: 1;
  overflow-y: auto;
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
  
  .el-avatar {
    background-color: $color-primary;
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
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-header {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-md;
  border-bottom: 1px solid $border-color;
  
  .el-avatar {
    background-color: $color-primary;
  }
}

.chat-user-name {
  font-weight: 600;
}

.chat-user-status {
  font-size: $font-size-sm;
  color: $color-success;
}

.empty-chat {
  flex: 1;
  @include flex-center;
  @include flex-column;
  gap: $spacing-md;
  
  h3 {
    color: $color-text-primary;
  }
  
  p {
    color: $color-text-regular;
  }
}
</style>
