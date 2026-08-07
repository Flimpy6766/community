<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const formRef = ref<FormInstance>()

const form = reactive({
  username: '',
  password: '',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const loading = ref(false)

async function handleLogin() {
  if (!formRef.value) return
  // 全部校验通过才发请求
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login({ username: form.username, password: form.password })
    ElMessage.success('登录成功')
    // 登录前被守卫拦下时会把目标地址存在 ?redirect= 里，登录后跳回去
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
    router.push(redirect)
  } catch {
    // 错误提示已在 axios 拦截器里统一处理
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <h1 class="auth-title">
        登录 Community
      </h1>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
        @submit.prevent="handleLogin"
      >
        <el-form-item
          label="用户名"
          prop="username"
        >
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
          />
        </el-form-item>
        <el-form-item
          label="密码"
          prop="password"
        >
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="请输入密码"
          />
        </el-form-item>
        <el-button
          class="auth-submit"
          type="primary"
          size="large"
          native-type="submit"
          :loading="loading"
        >
          登 录
        </el-button>
      </el-form>
      <p class="auth-switch">
        还没有账号？
        <router-link to="/register">
          去注册
        </router-link>
      </p>
    </el-card>
  </div>
</template>

<style scoped>
/* 和注册页同一套样式，保持视觉一致 */
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #eef2ff 0%, #fdf4ff 100%);
  padding: 24px;
}

.auth-card {
  width: 100%;
  max-width: 420px;
  border-radius: 12px;
}

.auth-title {
  margin: 0 0 24px;
  text-align: center;
  font-size: 24px;
}

.auth-submit {
  width: 100%;
  margin-top: 8px;
}

.auth-switch {
  margin: 16px 0 0;
  text-align: center;
  font-size: 14px;
  color: #666;
}

.auth-switch a {
  color: var(--el-color-primary);
}
</style>
