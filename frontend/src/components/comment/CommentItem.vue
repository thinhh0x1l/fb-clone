<template>
  <div class="comment-item">
    <el-avatar :size="32" :src="comment.user.avatar">
      {{ comment.user.displayName.charAt(0) }}
    </el-avatar>
    <div class="comment-content">
      <div class="comment-bubble">
        <router-link :to="`/profile/${comment.user.id}`" class="comment-author">
          {{ comment.user.displayName }}
        </router-link>
        <p class="comment-text">{{ comment.content }}</p>
      </div>
      <div class="comment-actions">
        <el-button link size="small" @click="toggleLike">
          Like{{ comment.likesCount > 0 ? ` (${comment.likesCount})` : '' }}
        </el-button>
        <el-button link size="small" @click="showReplyInput = !showReplyInput">
          Reply
        </el-button>
        <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
      </div>
      
      <div v-if="showReplyInput" class="reply-input">
        <el-input
          v-model="replyContent"
          placeholder="Write a reply..."
          size="small"
          @keyup.enter="submitReply"
        />
      </div>
      
      <div v-if="comment.replies && comment.replies.length > 0" class="replies">
        <CommentItem
          v-for="reply in comment.replies"
          :key="reply.id"
          :comment="reply"
          @reply="(e: any) => emit('reply', reply.id, e)"
          @delete="(e: any) => emit('delete', e)"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { Comment } from '@/types'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'

dayjs.extend(relativeTime)

const props = defineProps<{
  comment: Comment
}>()

const emit = defineEmits<{
  reply: [parentId: string, content: string]
  delete: [commentId: string]
}>()

const showReplyInput = ref(false)
const replyContent = ref('')

function formatDate(date: string) {
  return dayjs(date).fromNow()
}

function toggleLike() {
  // TODO: Call API to toggle like
}

function submitReply() {
  if (!replyContent.value.trim()) return
  emit('reply', props.comment.id, replyContent.value)
  replyContent.value = ''
  showReplyInput.value = false
}
</script>

<style lang="scss" scoped>
.comment-item {
  display: flex;
  gap: $spacing-sm;
  
  .el-avatar {
    background-color: $color-primary;
    flex-shrink: 0;
  }
}

.comment-content {
  flex: 1;
}

.comment-bubble {
  background-color: $bg-color;
  border-radius: $border-radius-lg;
  padding: $spacing-sm $spacing-md;
  display: inline-block;
  max-width: 100%;
}

.comment-author {
  font-weight: 600;
  font-size: $font-size-sm;
  color: $color-text-primary;
  text-decoration: none;
  
  &:hover {
    text-decoration: underline;
  }
}

.comment-text {
  font-size: $font-size-base;
  margin-top: 2px;
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-top: $spacing-xs;
  padding-left: $spacing-sm;
  
  .comment-time {
    font-size: $font-size-xs;
    color: $color-text-regular;
  }
}

.reply-input {
  margin-top: $spacing-sm;
}

.replies {
  margin-top: $spacing-sm;
  padding-left: $spacing-md;
  
  .comment-item {
    margin-bottom: $spacing-sm;
  }
}
</style>
