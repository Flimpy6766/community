import request from '@/utils/request'

export interface RegisterParams {
  username: string
  nickname: string
  password: string
}

export interface LoginParams {
  username: string
  password: string
}

// 登录成功返回：token + 用户基本信息
export interface LoginResult {
  token: string
  userId: number
  username: string
  nickname: string
  avatar: string | null
}

// 注册：POST /api/user/register
export function register(data: RegisterParams): Promise<void> {
  // 响应拦截器已经解包成真正的返回值，这里只是补一个类型声明
  return request.post('/user/register', data) as unknown as Promise<void>
}

// 登录：POST /api/user/login
export function login(data: LoginParams): Promise<LoginResult> {
  return request.post('/user/login', data) as unknown as Promise<LoginResult>
}
