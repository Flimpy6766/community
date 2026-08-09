import request from '@/utils/request'

// 个人中心资料（与后端 UserProfileVO 对应）
export interface UserProfileVO {
  id: number
  nickname: string
  avatar: string | null
  bio: string | null
  createTime: string
  articleCount: number
  favoriteCount: number
  commentCount: number
  likeReceivedCount: number
}

// 修改个人资料（部分更新：传什么改什么，不传的字段不变）
export interface UpdateProfileParams {
  nickname?: string
  avatar?: string
  bio?: string
}

// 个人中心资料：GET /api/user/profile（登录）
export function getProfile(): Promise<UserProfileVO> {
  return request.get('/user/profile') as unknown as Promise<UserProfileVO>
}

// 修改个人资料：PUT /api/user/profile（登录）
export function updateProfile(data: UpdateProfileParams): Promise<void> {
  return request.put('/user/profile', data) as unknown as Promise<void>
}
