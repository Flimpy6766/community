import { ref } from 'vue'
import { defineStore } from 'pinia'
import { login as loginApi, type LoginParams, type LoginResult } from '@/api/auth'

// 登录信息存 localStorage，刷新页面后依然保留
const TOKEN_KEY = 'community_token'
const USER_KEY = 'community_user'

// 用户状态：token + 用户信息
export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const userInfo = ref<LoginResult | null>(
    JSON.parse(localStorage.getItem(USER_KEY) || 'null'),
  )

  // 登录：调接口拿 token 和用户信息，同时写入内存和 localStorage
  async function login(params: LoginParams) {
    const data = await loginApi(params)
    token.value = data.token
    userInfo.value = data
    localStorage.setItem(TOKEN_KEY, data.token)
    localStorage.setItem(USER_KEY, JSON.stringify(data))
  }

  // 退出：清空内存和 localStorage
  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  return { token, userInfo, login, logout }
})
