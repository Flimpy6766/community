import request from '@/utils/request'
import type { PageResult } from './article'

// 后台统计面板（与后端 AdminOverviewVO 对应）
export interface AdminOverview {
  articleCount: number
  userCount: number
  commentCount: number
  tagCount: number
}

// 后台文章列表项（与后端 AdminArticleVO 对应）
export interface AdminArticleVO {
  id: number
  title: string
  authorName: string
  status: number
  viewCount: number
  likeCount: number
  favoriteCount: number
  commentCount: number
  createTime: string
}

// 统计面板：GET /api/admin/overview（仅管理员）
export function getAdminOverview(): Promise<AdminOverview> {
  return request.get('/admin/overview') as unknown as Promise<AdminOverview>
}

// 所有文章列表：GET /api/admin/articles?page=&size=（仅管理员）
export function getAdminArticles(page: number, size: number): Promise<PageResult<AdminArticleVO>> {
  return request.get('/admin/articles', {
    params: { page, size },
  }) as unknown as Promise<PageResult<AdminArticleVO>>
}

// 删除任意文章：DELETE /api/admin/articles/{id}（仅管理员）
export function deleteAdminArticle(id: number | string): Promise<void> {
  return request.delete(`/admin/articles/${id}`) as unknown as Promise<void>
}
