<template>
  <div class="settings-page">
    <h2>Settings</h2>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="General" name="general">
        <el-card>
          <el-form label-position="top">
            <el-form-item label="Language">
              <el-select v-model="language" style="width: 100%">
                <el-option label="English" value="en" />
                <el-option label="Tiếng Việt" value="vi" />
              </el-select>
            </el-form-item>
            <el-form-item label="Theme">
              <el-switch v-model="darkMode" active-text="Dark" inactive-text="Light" />
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
      <el-tab-pane label="Account" name="account">
        <el-card>
          <el-form label-position="top">
            <el-form-item label="Email">
              <el-input :model-value="authStore.user?.email" disabled />
            </el-form-item>
            <el-form-item label="Username">
              <el-input :model-value="authStore.user?.username" disabled />
            </el-form-item>
            <el-form-item label="Change Password">
              <el-button>Change Password</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
      <el-tab-pane label="Privacy" name="privacy">
        <el-card>
          <el-form label-position="top">
            <el-form-item label="Who can see your posts?">
              <el-select v-model="postPrivacy" style="width: 100%">
                <el-option label="Public" value="PUBLIC" />
                <el-option label="Friends" value="FRIENDS" />
                <el-option label="Only me" value="ONLY_ME" />
              </el-select>
            </el-form-item>
            <el-form-item label="Who can send you friend requests?">
              <el-select v-model="friendRequests" style="width: 100%">
                <el-option label="Everyone" value="EVERYONE" />
                <el-option label="Friends of friends" value="FRIENDS_OF_FRIENDS" />
              </el-select>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
      <el-tab-pane label="Notifications" name="notifications">
        <el-card>
          <el-form label-position="top">
            <el-form-item label="Push Notifications">
              <el-switch v-model="pushNotifications" />
            </el-form-item>
            <el-form-item label="Email Notifications">
              <el-switch v-model="emailNotifications" />
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const activeTab = ref('general')
const language = ref('en')
const darkMode = ref(false)
const postPrivacy = ref('PUBLIC')
const friendRequests = ref('EVERYONE')
const pushNotifications = ref(true)
const emailNotifications = ref(false)
</script>

<style lang="scss" scoped>
.settings-page {
  max-width: 680px;
  margin: 0 auto;
  padding: $spacing-md;
  margin-top: $header-height;
  
  h2 {
    font-size: 24px;
    margin-bottom: $spacing-md;
  }
}
</style>
