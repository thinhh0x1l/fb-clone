<template>
  <aside class="right-sidebar">
    <div class="sidebar-section">
      <h3 class="section-title">Sponsored</h3>
      <div class="ad-item">
        <el-image src="https://via.placeholder.com/130x130" fit="cover" class="ad-image" />
        <div class="ad-info">
          <div class="ad-title">Ad Title</div>
          <div class="ad-url">ad-url.com</div>
        </div>
      </div>
    </div>
    
    <el-divider />
    
    <div class="sidebar-section">
      <h3 class="section-title">Contacts</h3>
      <div class="contacts-list">
        <div v-for="contact in contacts" :key="contact.id" class="contact-item">
          <el-avatar :size="32" :src="contact.avatar">
            {{ contact.name.charAt(0) }}
          </el-avatar>
          <span>{{ contact.name }}</span>
        </div>
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
const contacts = [
  { id: 1, name: 'John Doe', avatar: '' },
  { id: 2, name: 'Jane Smith', avatar: '' },
  { id: 3, name: 'Bob Wilson', avatar: '' },
]
</script>

<style lang="scss" scoped>
.right-sidebar {
  position: fixed;
  right: 0;
  top: $header-height;
  width: $sidebar-width;
  height: calc(100vh - #{$header-height});
  padding: $spacing-lg $spacing-md;
  overflow-y: auto;
  @include scrollbar;
}

.sidebar-section {
  margin-bottom: $spacing-md;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: 600;
  color: $color-text-primary;
  margin-bottom: $spacing-md;
}

.ad-item {
  display: flex;
  gap: $spacing-sm;
  margin-bottom: $spacing-md;
  cursor: pointer;
  
  &:hover {
    opacity: 0.9;
  }
}

.ad-image {
  width: 130px;
  height: 130px;
  border-radius: $border-radius-base;
  flex-shrink: 0;
}

.ad-info {
  @include flex-column;
  justify-content: center;
}

.ad-title {
  font-weight: 500;
  font-size: $font-size-base;
}

.ad-url {
  font-size: $font-size-sm;
  color: $color-text-regular;
}

.contacts-list {
  @include flex-column;
  gap: $spacing-sm;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-xs;
  border-radius: $border-radius-base;
  cursor: pointer;
  
  &:hover {
    background-color: $bg-color;
  }
  
  .el-avatar {
    background-color: $color-primary;
  }
}

@media (max-width: $breakpoint-xl) {
  .right-sidebar {
    display: none;
  }
}
</style>
