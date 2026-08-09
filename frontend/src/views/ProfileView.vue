<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import {
  IconArticleFilled,
  IconEditFilled,
  IconMessageCircleFilled,
  IconStarFilled,
  IconThumbUpFilled,
  IconUserFilled,
} from '@tabler/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { getProfile, updateProfile, type UserProfileVO } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const loading = ref(false)
const profile = ref<UserProfileVO | null>(null)

// 编辑弹窗
const editVisible = ref(false)
const editLoading = ref(false)
const formRef = ref<FormInstance>()
const form = reactive({
  nickname: '',
  bio: '',
  avatar: '',
})

const rules: FormRules = {
  nickname: [{ max: 20, message: '昵称最长 20 个字符', trigger: 'blur' }],
  bio: [{ max: 200, message: '简介最长 200 个字符', trigger: 'blur' }],
  avatar: [{ max: 500, message: '头像 URL 最长 500 个字符', trigger: 'blur' }],
}

async function fetchProfile() {
  loading.value = true
  try {
    profile.value = await getProfile()
  } catch {
    // 错误提示已在拦截器统一处理
  } finally {
    loading.value = false
  }
}

function openEdit() {
  if (!profile.value) return
  form.nickname = profile.value.nickname || ''
  form.bio = profile.value.bio || ''
  form.avatar = profile.value.avatar || ''
  editVisible.value = true
}

async function handleSave() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  editLoading.value = true
  try {
    await updateProfile({
      nickname: form.nickname.trim() || undefined,
      bio: form.bio.trim() || undefined,
      avatar: form.avatar.trim() || undefined,
    })
    ElMessage.success('资料已更新')
    editVisible.value = false
    // 同步导航栏显示的新昵称/头像
    userStore.updateUserInfo({
      nickname: form.nickname.trim(),
      avatar: form.avatar.trim(),
    })
    await fetchProfile()
  } catch {
    // 错误提示已在拦截器统一处理
  } finally {
    editLoading.value = false
  }
}

function formatTime(time: string) {
  return time.replace('T', ' ').slice(0, 10)
}

onMounted(fetchProfile)
</script>

<template>
  <div
    v-loading="loading"
    class="profile-page"
  >
    <el-card
      v-if="profile"
      class="profile-card"
    >
      <div class="profile-head">
        <div class="profile-avatar">
          <img
            v-if="profile.avatar"
            :src="profile.avatar"
            alt=""
          >
          <IconUserFilled
            v-else
            :size="40"
          />
        </div>
        <div class="profile-info">
          <h1 class="profile-nickname">
            {{ profile.nickname }}
          </h1>
          <p class="profile-bio">
            {{ profile.bio || '这个人很懒，还没有写简介' }}
          </p>
          <p class="profile-meta">加入于 {{ formatTime(profile.createTime) }}</p>
        </div>
        <el-button
          class="profile-edit-btn"
          type="primary"
          @click="openEdit"
        >
          <IconEditFilled :size="15" />
          编辑资料
        </el-button>
      </div>

      <!-- 统计：我的文章 / 收藏 / 评论 / 收到的赞 -->
      <div class="profile-stats">
        <div class="stat-item">
          <IconArticleFilled :size="20" />
          <span class="stat-num">{{ profile.articleCount }}</span>
          <span class="stat-label">我的文章</span>
        </div>
        <div class="stat-item">
          <IconStarFilled :size="20" />
          <span class="stat-num">{{ profile.favoriteCount }}</span>
          <span class="stat-label">我的收藏</span>
        </div>
        <div class="stat-item">
          <IconMessageCircleFilled :size="20" />
          <span class="stat-num">{{ profile.commentCount }}</span>
          <span class="stat-label">我的评论</span>
        </div>
        <div class="stat-item">
          <IconThumbUpFilled :size="20" />
          <span class="stat-num">{{ profile.likeReceivedCount }}</span>
          <span class="stat-label">收到的赞</span>
        </div>
      </div>

      <el-divider />

      <div class="profile-links">
        <router-link
          class="profile-link"
          to="/my-articles"
        >
          我的文章（草稿箱）→
        </router-link>
        <router-link
          class="profile-link"
          to="/favorites"
        >
          我的收藏 →
        </router-link>
      </div>
    </el-card>

    <!-- 编辑资料弹窗 -->
    <el-dialog
      v-model="editVisible"
      title="编辑资料"
      width="480px"
    >
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
      >
        <el-form-item
          label="昵称"
          prop="nickname"
        >
          <el-input
            v-model="form.nickname"
            maxlength="20"
            placeholder="请输入昵称"
          />
        </el-form-item>
        <el-form-item
          label="简介"
          prop="bio"
        >
          <el-input
            v-model="form.bio"
            maxlength="200"
            type="textarea"
            :rows="3"
            placeholder="介绍一下自己（最多 200 字）"
          />
        </el-form-item>
        <el-form-item
          label="头像 URL"
          prop="avatar"
        >
          <el-input
            v-model="form.avatar"
            maxlength="500"
            placeholder="图片地址（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="editLoading"
          @click="handleSave"
        >
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.profile-page {
  max-width: 800px;
  margin: 0 auto;
}

.profile-card {
  border-radius: 12px;
}

.profile-head {
  display: flex;
  align-items: center;
  gap: 20px;
}

.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--app-primary-soft);
  color: var(--app-primary);
  overflow: hidden;
  flex-shrink: 0;
}

.profile-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.profile-nickname {
  margin: 0;
  font-size: calc(var(--app-font-size) + 8px);
  color: var(--app-text);
}

.profile-bio {
  margin: 8px 0 4px;
  font-size: calc(var(--app-font-size) - 1px);
  color: var(--app-text-secondary);
}

.profile-meta {
  margin: 0;
  font-size: calc(var(--app-font-size) - 3px);
  color: var(--app-text-secondary);
}

.profile-edit-btn {
  margin-left: auto;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.profile-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-top: 24px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 16px 8px;
  background: var(--app-muted);
  border-radius: 10px;
  color: var(--app-primary);
}

.stat-num {
  font-size: calc(var(--app-font-size) + 6px);
  font-weight: 700;
  color: var(--app-text);
}

.stat-label {
  font-size: calc(var(--app-font-size) - 3px);
  color: var(--app-text-secondary);
}

.profile-links {
  display: flex;
  gap: 16px;
}

.profile-link {
  font-size: calc(var(--app-font-size) - 1px);
  color: var(--app-primary);
}
</style>
