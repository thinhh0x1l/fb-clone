<template>
  <div class="friends-page">
    <h2>Friends</h2>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="All Friends" name="all">
        <div v-if="friendStore.friends.length === 0" class="empty-state">
          <el-empty description="No friends yet" />
        </div>
        <div v-else class="friends-grid">
          <el-card v-for="f in friendStore.friends" :key="f.id" class="friend-card" shadow="hover">
            <router-link :to="`/profile/${f.id}`">
              <el-avatar :size="80" :src="f.avatar">{{ f.displayName.charAt(0) }}</el-avatar>
              <p class="friend-name">{{ f.displayName }}</p>
            </router-link>
            <el-button size="small" @click="friendStore.unfriend(f.id)">Unfriend</el-button>
          </el-card>
        </div>
      </el-tab-pane>
      <el-tab-pane label="Requests" name="requests">
        <div v-if="friendStore.pendingRequests.length === 0" class="empty-state">
          <el-empty description="No pending requests" />
        </div>
        <div v-else class="friends-grid">
          <el-card v-for="r in friendStore.pendingRequests" :key="r.id" class="friend-card" shadow="hover">
            <router-link :to="`/profile/${r.requester.id}`">
              <el-avatar :size="80" :src="r.requester.avatar">{{ r.requester.displayName.charAt(0) }}</el-avatar>
              <p class="friend-name">{{ r.requester.displayName }}</p>
            </router-link>
            <div class="request-actions">
              <el-button type="primary" size="small" @click="friendStore.acceptRequest(r.id)">Accept</el-button>
              <el-button size="small" @click="friendStore.rejectRequest(r.id)">Reject</el-button>
            </div>
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useFriendStore } from '@/stores/friend'

const friendStore = useFriendStore()
const activeTab = ref('all')

onMounted(() => {
  friendStore.fetchFriends()
  friendStore.fetchPendingRequests()
})
</script>

<style lang="scss" scoped>
.friends-page {
  max-width: 940px;
  margin: 0 auto;
  padding: $spacing-md;
  margin-top: $header-height;

  h2 {
    font-size: 24px;
    margin-bottom: $spacing-md;
  }
}

.friends-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: $spacing-md;
}

.friend-card {
  text-align: center;

  a {
    text-decoration: none;
    color: inherit;
  }

  .friend-name {
    margin: $spacing-sm 0;
    font-weight: 600;
  }
}

.request-actions {
  display: flex;
  gap: $spacing-xs;
  justify-content: center;
}

.empty-state {
  padding: $spacing-xl;
}
</style>
