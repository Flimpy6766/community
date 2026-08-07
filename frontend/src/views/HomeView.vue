<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

function handleLogout() {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<template>
  <div class="home">
    <h1>Community 内容社区</h1>
    <p
      v-if="userStore.userInfo"
      class="home-user"
    >
      你好，{{ userStore.userInfo.nickname }}
    </p>
    <div class="home-actions">
      <el-button
        type="primary"
        @click="router.push('/register')"
      >
        去注册
      </el-button>
      <el-button
        v-if="!userStore.userInfo"
        type="success"
        @click="router.push('/login')"
      >
        去登录
      </el-button>
      <el-button
        v-else
        type="danger"
        @click="handleLogout"
      >
        退出登录
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 20vh;
}

.home-actions {
  display: flex;
  gap: 12px;
}

.home-user {
  color: #666;
}
</style>
