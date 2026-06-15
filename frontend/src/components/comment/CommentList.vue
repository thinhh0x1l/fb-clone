<template>
  <div class="comment-list">
    <div class="comment-input">
      <el-avatar :size="32" :src="authStore.user?.avatar">
        {{ authStore.user?.displayName?.charAt(0) }}
      </el-avatar>
      <el-input
        v-model="newComment"
        placeholder="Write a comment..."
        size="large"
        @keyup.enter="submitComment"
      />
    </div>
    
    <div v-if="loading" class="loading">
      <el-icon class="is-loading"><Loading /></el-icon>
    </div>
    
    <div v-else class="comments">
      <CommentItem
        v-for="comment in comments"
        :key="comment.id"
        :comment="comment"
        @reply="handleReply"
        @delete="handleDelete"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import CommentItem from './CommentItem.vue'
import type { Comment } from '@/types'

const props = defineProps<{
  postId: string
}>()

const authStore = useAuthStore()
const loading = ref(true)
const comments = ref<Comment[]>([])
const newComment = ref('')

onMounted(() => {
  fetchComments()
})

async function fetchComments() {
  loading.value = true
  try {
    // TODO: Call API to fetch comments
    comments.value = []
  } finally {
    loading.value = false
  }
}

async function submitComment() {
  if (!newComment.value.trim()) return
  // TODO: Call API to submit comment
  newComment.value = ''
}

function handleReply(parentId: string, content: string) {
  // TODO: Call API to reply to comment
}

function handleDelete(commentId: string) {
  // TODO: Call API to delete comment
}
</script>

<style lang="scss" scoped>
.comment-list {
  @include flex-column;
  gap: $spacing-sm;
}

.comment-input {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-bottom: $spacing-sm;
  
  .el-avatar {
    background-color: $color-primary;
    flex-shrink: 0;
  }
  
  .el-input {
    flex: 1;
    
    :deep(.el-input__wrapper) {
      border-radius: 50px;
      background-color: $bg-color;
    }
  }
}

.loading {
  @include flex-center;
  padding: $spacing-md;
}

.comments {
  @include flex-column;
  gap: $spacing-sm;
}
</style>
