<template>
  <div class="create-post">
    <el-card class="post-card">
      <div class="post-input-row">
        <el-avatar :size="40" :src="authStore.user?.avatar">
          {{ authStore.user?.displayName?.charAt(0) }}
        </el-avatar>
        <el-button
          class="post-input-btn"
          size="large"
          @click="showDialog = true"
        >
          What's on your mind, {{ authStore.user?.displayName?.split(' ')[0] }}?
        </el-button>
      </div>
      
      <el-divider />
      
      <div class="post-actions">
        <el-button class="action-btn" size="large">
          <el-icon :size="24" color="#f3425f"><VideoPlay /></el-icon>
          <span>Live video</span>
        </el-button>
        <el-button class="action-btn" size="large" @click="showDialog = true">
          <el-icon :size="24" color="#45bd62"><Image /></el-icon>
          <span>Photo/video</span>
        </el-button>
        <el-button class="action-btn" size="large" @click="showDialog = true">
          <el-icon :size="24" color="#f7b928"><Smile /></el-icon>
          <span>Feeling/activity</span>
        </el-button>
      </div>
    </el-card>
    
    <el-dialog
      v-model="showDialog"
      title="Create post"
      width="500px"
      :before-close="handleClose"
    >
      <div class="dialog-header">
        <el-avatar :size="40" :src="authStore.user?.avatar">
          {{ authStore.user?.displayName?.charAt(0) }}
        </el-avatar>
        <div>
          <div class="user-name">{{ authStore.user?.displayName }}</div>
          <el-select v-model="visibility" size="small" style="width: 120px">
            <el-option label="Public" value="PUBLIC" />
            <el-option label="Friends" value="FRIENDS" />
            <el-option label="Only me" value="ONLY_ME" />
          </el-select>
        </div>
      </div>
      
      <el-input
        v-model="content"
        type="textarea"
        :rows="4"
        placeholder="What's on your mind?"
        resize="none"
      />
      
      <template #footer>
        <el-button
          type="primary"
          :disabled="!content.trim()"
          :loading="loading"
          @click="handlePost"
        >
          Post
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { usePostStore } from '@/stores/post'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const postStore = usePostStore()
const showDialog = ref(false)
const content = ref('')
const visibility = ref('PUBLIC')
const loading = ref(false)

function handleClose() {
  content.value = ''
  visibility.value = 'PUBLIC'
  showDialog.value = false
}

async function handlePost() {
  if (!content.value.trim()) return
  
  loading.value = true
  try {
    await postStore.createPost({ content: content.value, visibility: visibility.value })
    ElMessage.success('Post created successfully')
    handleClose()
  } catch (error) {
    ElMessage.error('Failed to create post')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.create-post {
  margin-bottom: $spacing-md;
}

.post-card {
  :deep(.el-card__body) {
    padding: $spacing-md;
  }
}

.post-input-row {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  
  .el-avatar {
    background-color: $color-primary;
    flex-shrink: 0;
  }
  
  .post-input-btn {
    flex: 1;
    justify-content: flex-start;
    background-color: $bg-color;
    border: none;
    border-radius: 50px;
    color: $color-text-regular;
    font-size: $font-size-lg;
    
    &:hover {
      background-color: $border-color-light;
    }
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
    
    &:hover {
      background-color: $bg-color;
      border-radius: $border-radius-base;
    }
    
    span {
      @include respond-to(md) {
        display: none;
      }
    }
  }
}

.dialog-header {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-bottom: $spacing-md;
  
  .el-avatar {
    background-color: $color-primary;
  }
  
  .user-name {
    font-weight: 600;
    font-size: $font-size-lg;
  }
}
</style>
