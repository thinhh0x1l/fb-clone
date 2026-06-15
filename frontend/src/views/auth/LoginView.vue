<template>
  <div class="login-page">
    <div class="login-container">
      <div class="login-left">
        <h1>facebook</h1>
        <p>Connect with friends and the world around you on Facebook.</p>
      </div>
      <div class="login-right">
        <el-card class="login-card">
          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            label-position="top"
            @submit.prevent="handleLogin"
          >
            <el-form-item label="Email" prop="email">
              <el-input
                v-model="form.email"
                placeholder="Enter your email"
                size="large"
              />
            </el-form-item>
            <el-form-item label="Password" prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="Enter your password"
                size="large"
                show-password
              />
            </el-form-item>
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              Log In
            </el-button>
          </el-form>
          <div class="login-links">
            <el-link type="primary">Forgotten password?</el-link>
            <el-divider />
            <el-button type="success" size="large" @click="$router.push('/register')">
              Create New Account
            </el-button>
          </div>
        </el-card>
      </div>
    </div>
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
  email: '',
  password: '',
})

const rules: FormRules = {
  email: [
    { required: true, message: 'Please enter your email', trigger: 'blur' },
    { type: 'email', message: 'Please enter a valid email', trigger: 'blur' },
  ],
  password: [
    { required: true, message: 'Please enter your password', trigger: 'blur' },
    { min: 8, message: 'Password must be at least 8 characters', trigger: 'blur' },
  ],
}

async function handleLogin() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await authStore.login(form.email, form.password)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background-color: $bg-color;
  @include flex-center;
}

.login-container {
  display: flex;
  align-items: center;
  gap: $spacing-xl;
  max-width: 980px;
  padding: 0 $spacing-md;
}

.login-left {
  flex: 1;
  
  h1 {
    font-size: 56px;
    color: $color-primary;
    margin-bottom: $spacing-md;
  }
  
  p {
    font-size: 24px;
    color: $color-text-primary;
    line-height: 1.3;
  }
}

.login-right {
  width: 396px;
}

.login-card {
  padding: $spacing-md;
  
  :deep(.el-card__body) {
    padding: $spacing-md;
  }
}

.login-btn {
  width: 100%;
  margin-top: $spacing-sm;
}

.login-links {
  text-align: center;
  margin-top: $spacing-md;
  
  .el-divider {
    margin: $spacing-md 0;
  }
}

@media (max-width: $breakpoint-md) {
  .login-container {
    flex-direction: column;
    text-align: center;
  }
  
  .login-left {
    h1 {
      font-size: 40px;
    }
    
    p {
      font-size: 18px;
    }
  }
  
  .login-right {
    width: 100%;
    max-width: 396px;
  }
}
</style>
