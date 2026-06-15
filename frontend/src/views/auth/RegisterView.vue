<template>
  <div class="register-page">
    <el-card class="register-card">
      <template #header>
        <div class="register-header">
          <h2>Sign Up</h2>
          <p>It's quick and easy.</p>
        </div>
      </template>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent="handleRegister"
      >
        <el-form-item prop="displayName">
          <el-input
            v-model="form.displayName"
            placeholder="Full name"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="email">
          <el-input
            v-model="form.email"
            placeholder="Email address"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="Username"
            size="large"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="New password"
            size="large"
            show-password
          />
        </el-form-item>
        
        <div class="form-actions">
          <el-button size="large" @click="$router.push('/login')">
            Cancel
          </el-button>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleRegister"
          >
            Sign Up
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useAuthStore } from '@/stores/auth'
import type { FormInstance, FormRules } from 'element-plus'

const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  displayName: '',
  email: '',
  username: '',
  password: '',
})

const rules: FormRules = {
  displayName: [
    { required: true, message: 'Please enter your name', trigger: 'blur' },
    { min: 3, max: 100, message: 'Name must be 3-100 characters', trigger: 'blur' },
  ],
  email: [
    { required: true, message: 'Please enter your email', trigger: 'blur' },
    { type: 'email', message: 'Please enter a valid email', trigger: 'blur' },
  ],
  username: [
    { required: true, message: 'Please enter a username', trigger: 'blur' },
    { min: 3, max: 50, message: 'Username must be 3-50 characters', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: 'Username can only contain letters, numbers and underscores', trigger: 'blur' },
  ],
  password: [
    { required: true, message: 'Please enter a password', trigger: 'blur' },
    { min: 8, message: 'Password must be at least 8 characters', trigger: 'blur' },
  ],
}

async function handleRegister() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await authStore.register(form)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  background-color: $bg-color;
  @include flex-center;
  padding: $spacing-md;
}

.register-card {
  width: 100%;
  max-width: 432px;
  
  :deep(.el-card__header) {
    padding: $spacing-md $spacing-md $spacing-sm;
    border-bottom: none;
  }
  
  :deep(.el-card__body) {
    padding: 0 $spacing-md $spacing-md;
  }
}

.register-header {
  h2 {
    font-size: 32px;
    margin-bottom: $spacing-xs;
  }
  
  p {
    color: $color-text-regular;
    font-size: $font-size-base;
  }
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-sm;
  margin-top: $spacing-md;
}
</style>
