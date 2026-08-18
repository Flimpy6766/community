<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { register } from '@/api/auth'
import { siteConfig } from '@/config/site'

const router = useRouter()

// 表单实例的引用，用于触发整体校验
const formRef = ref<FormInstance>()

// 表单数据（reactive 让对象内的字段都是响应式的）
const form = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
})

// 校验规则：字段名要和 el-form-item 的 prop 对上
const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3-20 个字符', trigger: 'blur' },
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度为 2-20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      // 自定义校验器：验证两次密码是否一致
      validator: (_rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

const loading = ref(false)

async function handleRegister() {
  if (!formRef.value) return
  // validate() 会跑全部规则，全部通过才继续
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await register({
      username: form.username,
      nickname: form.nickname,
      password: form.password,
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch {
    // 失败提示已在 axios 拦截器里统一处理
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <p class="auth-kicker">COMMUNITY / JOIN US</p>
      <h1 class="auth-title">注册 {{ siteConfig.brand }} 账号</h1>
      <p class="auth-description">创建账号，开始发布和收藏你的内容。</p>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
        @submit.prevent="handleRegister"
      >
        <el-form-item
          label="用户名"
          prop="username"
        >
          <el-input
            v-model="form.username"
            placeholder="3-20 个字符"
          />
        </el-form-item>
        <el-form-item
          label="昵称"
          prop="nickname"
        >
          <el-input
            v-model="form.nickname"
            placeholder="展示给其他用户的名字"
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
            placeholder="6-20 个字符"
          />
        </el-form-item>
        <el-form-item
          label="确认密码"
          prop="confirmPassword"
        >
          <el-input
            v-model="form.confirmPassword"
            type="password"
            show-password
            placeholder="再输入一次密码"
          />
        </el-form-item>
        <el-button
          class="auth-submit"
          type="primary"
          size="large"
          native-type="submit"
          :loading="loading"
        >
          注 册
        </el-button>
      </el-form>
      <p class="auth-switch">
        已有账号？
        <router-link to="/login">
          去登录
        </router-link>
      </p>
    </el-card>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  /* 跟随主题的渐变背景，深色模式下自动变深 */
  background: var(--app-auth-bg);
  padding: 24px;
}

.auth-card {
  width: 100%;
  max-width: 430px;
  padding: 12px;
  border: 1px solid var(--app-border);
  border-radius: var(--app-radius-lg);
  background: var(--app-surface-solid);
  box-shadow: var(--app-shadow-popover);
}

.auth-kicker {
  margin: 0 0 12px;
  color: var(--app-primary-strong);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.16em;
}

.auth-title {
  margin: 0;
  color: var(--app-text);
  font-size: 28px;
  font-weight: 750;
  letter-spacing: -0.04em;
}

.auth-description {
  margin: 10px 0 28px;
  color: var(--app-text-secondary);
  font-size: 13px;
}

.auth-submit {
  width: 100%;
  margin-top: 8px;
}

.auth-switch {
  margin: 16px 0 0;
  text-align: center;
  font-size: calc(var(--app-font-size) - 2px);
  color: var(--app-text-secondary);
}

.auth-switch a {
  color: var(--el-color-primary);
}
</style>
