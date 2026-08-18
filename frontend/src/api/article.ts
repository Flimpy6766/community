import request from '@/utils/request'
import { appConfig } from '@/config/app'

// 与后端 ArticleVO 对应
export interface ArticleVO {
  id: number
  userId: number
  title: string
  summary: string | null
  content: string
  cover: string | null
  status: number
  viewCount: number
  likeCount: number
  favoriteCount: number
  commentCount: number
  createTime: string
  updateTime: string
  tags: string[]
  // 后端暂未返回作者昵称，先留可选字段，后端加上后前端自动显示
  nickname?: string
  // 后端暂未返回"我是否已赞/已收藏"，先留可选字段（登录状态下后端加上后自动生效）
  liked?: boolean
  favorited?: boolean
}

// 与后端 MyBatis-Plus IPage 对应
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 与后端 CreateArticleDTO 对应
export interface CreateArticleParams {
  title: string
  summary?: string
  content?: string
  cover?: string
  status: number // 0 草稿 1 发布
  tags: string[]
}

export interface LikeResult {
  liked: boolean
  likeCount: number
}

export interface FavoriteResult {
  favorited: boolean
  favoriteCount: number
}

export interface CommentItem {
  id: number
  userId: number
  nickname: string
  content: string
  createTime: string
  replies: CommentItem[]
}

export interface CreateCommentParams {
  content: string
  parentId?: number
}

// 文章列表：GET /api/article/list?page=1&size=10
export function getArticleList(page: number, size: number): Promise<PageResult<ArticleVO>> {
  return request.get('/article/list', { params: { page, size } }) as unknown as Promise<
    PageResult<ArticleVO>
  >
}

// 搜索文章：GET /api/article/search?keyword=xxx&page=1&size=10（标题或正文模糊匹配）
export function searchArticles(
  keyword: string,
  page: number,
  size: number,
): Promise<PageResult<ArticleVO>> {
  return request.get('/article/search', {
    params: { keyword, page, size },
  }) as unknown as Promise<PageResult<ArticleVO>>
}

// 文章详情：GET /api/article/{id}
export function getArticleDetail(id: number | string): Promise<ArticleVO> {
  return request.get(`/article/${id}`) as unknown as Promise<ArticleVO>
}

// 发布/存草稿：POST /api/article
export function createArticle(data: CreateArticleParams): Promise<number> {
  return request.post('/article', data) as unknown as Promise<number>
}

// 更新文章：PUT /api/article/{id}
export function updateArticle(id: number | string, data: CreateArticleParams): Promise<void> {
  return request.put(`/article/${id}`, data) as unknown as Promise<void>
}

// 删除文章：DELETE /api/article/{id}
export function deleteArticle(id: number | string): Promise<void> {
  return request.delete(`/article/${id}`) as unknown as Promise<void>
}

// 点赞/取消点赞：POST /api/article/{id}/like（toggle）
export function toggleLike(id: number | string): Promise<LikeResult> {
  return request.post(`/article/${id}/like`) as unknown as Promise<LikeResult>
}

// 收藏/取消收藏：POST /api/article/{id}/favorite（toggle）
export function toggleFavorite(id: number | string): Promise<FavoriteResult> {
  return request.post(`/article/${id}/favorite`) as unknown as Promise<FavoriteResult>
}

// 评论列表：GET /api/article/{id}/comments
export function getComments(
  id: number | string,
  page: number,
  size = appConfig.pagination.commentPageSize,
): Promise<PageResult<CommentItem>> {
  return request.get(`/article/${id}/comments`, { params: { page, size } }) as unknown as Promise<
    PageResult<CommentItem>
  >
}

// 发表评论/回复：POST /api/article/{id}/comment
export function addComment(id: number | string, data: CreateCommentParams): Promise<void> {
  return request.post(`/article/${id}/comment`, data) as unknown as Promise<void>
}

// 删除评论：DELETE /api/article/comment/{id}
export function deleteComment(commentId: number | string): Promise<void> {
  return request.delete(`/article/comment/${commentId}`) as unknown as Promise<void>
}

// 热榜：GET /api/article/hot?limit=10（无分页，直接返回数组）
export function getHotArticles(
  limit: number = appConfig.hot.defaultLimit,
): Promise<ArticleVO[]> {
  return request.get('/article/hot', { params: { limit } }) as unknown as Promise<ArticleVO[]>
}

// 我的收藏：GET /api/article/favorite/my
export function getMyFavorites(page: number, size: number): Promise<PageResult<ArticleVO>> {
  return request.get('/article/favorite/my', { params: { page, size } }) as unknown as Promise<
    PageResult<ArticleVO>
  >
}

// 我的文章：GET /api/article/my?status=&page=&size=（status 不传=全部，0=草稿，1=已发布）
export function getMyArticles(
  status: number | undefined,
  page: number,
  size: number,
): Promise<PageResult<ArticleVO>> {
  return request.get('/article/my', {
    params: { status, page, size },
  }) as unknown as Promise<PageResult<ArticleVO>>
}
