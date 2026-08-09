import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { login as loginApi, type LoginParams, type LoginResult } from '@/api/auth'

// 登录信息存 localStorage，刷新页面后依然保留
const TOKEN_KEY = 'community_token'
const USER_KEY = 'community_user'

// 从 JWT 里解出角色（仅用于前端判断是否显示后台入口；真正的权限控制在后端）
export function getTokenRole(token: string): string | null {
  try {
    const payload = token.split('.')[1]
    const json = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/')))
    return json.role || null
  } catch {
    return null
  }
}

// 用户状态：token + 用户信息
export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const userInfo = ref<LoginResult | null>(
    JSON.parse(localStorage.getItem(USER_KEY) || 'null'),
  )

  // 是否管理员（解码 JWT 的 role 字段）
  const isAdmin = computed(() => getTokenRole(token.value) === 'ADMIN')

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

  // 更新本地用户信息（个人中心改昵称/头像后同步导航栏）
  function updateUserInfo(partial: Partial<Pick<LoginResult, 'nickname' | 'avatar'>>) {
    if (!userInfo.value) return
    userInfo.value = { ...userInfo.value, ...partial }
    localStorage.setItem(USER_KEY, JSON.stringify(userInfo.value))
  }

  return { token, userInfo, isAdmin, login, logout, updateUserInfo }
})
