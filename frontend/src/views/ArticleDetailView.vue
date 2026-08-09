<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  IconEyeFilled,
  IconMessageCircleFilled,
  IconStarFilled,
  IconThumbUpFilled,
} from '@tabler/icons-vue'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { useUserStore } from '@/stores/user'
import {
  addComment,
  deleteArticle,
  deleteComment,
  getArticleDetail,
  getComments,
  toggleFavorite,
  toggleLike,
  type ArticleVO,
  type CommentItem,
} from '@/api/article'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const article = ref<ArticleVO | null>(null)

// 点赞/收藏状态（后端暂未返回初始状态，先默认 false，点击后以后端返回为准）
const liked = ref(false)
const likedLoading = ref(false)
const favorited = ref(false)
const favLoading = ref(false)

// 评论相关
const comments = ref<CommentItem[]>([])
const commentsTotal = ref(0)
const commentsPage = ref(1)
const commentContent = ref('')
const replyTo = ref<CommentItem | null>(null)
const commentLoading = ref(false)
const submittingComment = ref(false)

async function fetchDetail() {
  loading.value = true
  try {
    article.value = await getArticleDetail(route.params.id as string)
    liked.value = article.value.liked ?? false
    favorited.value = article.value.favorited ?? false
    await fetchComments()
  } catch {
    article.value = null
  } finally {
    loading.value = false
  }
}

async function fetchComments() {
  if (!article.value) return
  commentLoading.value = true
  try {
    const data = await getComments(article.value.id, commentsPage.value)
    comments.value = data.records
    commentsTotal.value = data.total
  } catch {
    // 错误提示已在拦截器统一处理
  } finally {
    commentLoading.value = false
  }
}

// 未登录则提示并带去登录页，登录完跳回当前页面
function requireLogin(): boolean {
  if (userStore.userInfo) return true
  ElMessage.warning('请先登录')
  router.push({ name: 'login', query: { redirect: route.fullPath } })
  return false
}

async function handleToggleLike() {
  if (!article.value) return
  if (!requireLogin()) return
  likedLoading.value = true
  // 乐观更新：先改界面，失败再回滚
  const prevLiked = liked.value
  const prevCount = article.value.likeCount
  liked.value = !prevLiked
  article.value.likeCount += liked.value ? 1 : -1
  try {
    const data = await toggleLike(article.value.id)
    liked.value = data.liked
    article.value.likeCount = data.likeCount
  } catch {
    liked.value = prevLiked
    article.value.likeCount = prevCount
  } finally {
    likedLoading.value = false
  }
}

async function handleToggleFavorite() {
  if (!article.value) return
  if (!requireLogin()) return
  favLoading.value = true
  const prevFavorited = favorited.value
  const prevCount = article.value.favoriteCount
  favorited.value = !prevFavorited
  article.value.favoriteCount += favorited.value ? 1 : -1
  try {
    const data = await toggleFavorite(article.value.id)
    favorited.value = data.favorited
    article.value.favoriteCount = data.favoriteCount
  } catch {
    favorited.value = prevFavorited
    article.value.favoriteCount = prevCount
  } finally {
    favLoading.value = false
  }
}

function startReply(comment: CommentItem) {
  if (!requireLogin()) return
  replyTo.value = comment
  commentContent.value = `回复 @${comment.nickname}：`
}

function cancelReply() {
  replyTo.value = null
  commentContent.value = ''
}

async function handleSubmitComment() {
  if (!article.value) return
  if (!requireLogin()) return
  const content = commentContent.value.trim()
  if (!content) {
    ElMessage.warning('评论内容不能为空')
    return
  }
  submittingComment.value = true
  try {
    await addComment(article.value.id, {
      content,
      // 回复时带 parentId，顶级评论不传（后端默认 0）
      parentId: replyTo.value?.id ?? 0,
    })
    ElMessage.success('评论成功')
    commentContent.value = ''
    replyTo.value = null
    article.value.commentCount += 1
    await fetchComments()
  } catch {
    // 错误提示已在拦截器统一处理
  } finally {
    submittingComment.value = false
  }
}

async function handleDeleteComment(comment: CommentItem) {
  const confirmed = await ElMessageBox.confirm('确定删除这条评论吗？', '删除确认', {
    type: 'warning',
  }).catch(() => false)
  if (!confirmed) return
  try {
    await deleteComment(comment.id)
    ElMessage.success('已删除')
    if (article.value) article.value.commentCount -= 1
    await fetchComments()
  } catch {
    // 错误提示已在拦截器统一处理
  }
}

async function handleDelete() {
  const confirmed = await ElMessageBox.confirm(
    '确定删除这篇文章吗？删除后不可恢复',
    '删除确认',
    { type: 'warning' },
  ).catch(() => false)
  if (!confirmed || !article.value) return
  try {
    await deleteArticle(article.value.id)
    ElMessage.success('已删除')
    router.push('/')
  } catch {
    // 错误提示已在拦截器统一处理
  }
}

function formatTime(time: string) {
  return time.replace('T', ' ').slice(0, 16)
}

onMounted(fetchDetail)
</script>

<template>
  <div
    v-loading="loading"
    class="detail-page"
  >
    <template v-if="article">
      <el-card class="detail-card">
        <h1 class="detail-title">
          {{ article.title }}
        </h1>
        <div class="detail-meta">
          <span
            v-if="article.nickname"
            class="detail-author"
          >
            {{ article.nickname }}
          </span>
          <el-tag
            v-for="tag in article.tags"
            :key="tag"
            size="small"
            type="info"
            effect="plain"
          >
            {{ tag }}
          </el-tag>
          <span class="detail-time">{{ formatTime(article.createTime) }}</span>
          <template v-if="userStore.userInfo?.userId === article.userId">
            <el-button
              size="small"
              @click="router.push(`/article/${article.id}/edit`)"
            >
              编辑
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="handleDelete"
            >
              删除
            </el-button>
          </template>
        </div>
        <div class="detail-stats">
          <span>
            <IconEyeFilled :size="15" />
            浏览 {{ article.viewCount }}
          </span>
          <span>
            <IconThumbUpFilled :size="15" />
            点赞 {{ article.likeCount }}
          </span>
          <span>
            <IconStarFilled :size="15" />
            收藏 {{ article.favoriteCount }}
          </span>
          <span>
            <IconMessageCircleFilled :size="15" />
            评论 {{ article.commentCount }}
          </span>
        </div>
        <MdPreview
          class="detail-content"
          :model-value="article.content"
        />
        <!-- 互动区：点赞 / 收藏 -->
        <div class="detail-interactions">
          <el-button
            :type="liked ? 'primary' : 'default'"
            :loading="likedLoading"
            @click="handleToggleLike"
          >
            <IconThumbUpFilled :size="16" />
            {{ liked ? '已点赞' : '点赞' }} {{ article.likeCount }}
          </el-button>
          <el-button
            :type="favorited ? 'warning' : 'default'"
            :loading="favLoading"
            @click="handleToggleFavorite"
          >
            <IconStarFilled :size="16" />
            {{ favorited ? '已收藏' : '收藏' }} {{ article.favoriteCount }}
          </el-button>
        </div>
      </el-card>

      <!-- 评论区 -->
      <el-card class="comment-card">
        <h3 class="comment-title">
          评论（{{ commentsTotal }}）
        </h3>
        <div class="comment-input">
          <el-input
            v-model="commentContent"
            type="textarea"
            :rows="3"
            maxlength="2000"
            show-word-limit
            :placeholder="replyTo ? `回复 @${replyTo.nickname}` : '写下你的评论…'"
          />
          <div class="comment-input-actions">
            <el-button
              v-if="replyTo"
              size="small"
              @click="cancelReply"
            >
              取消回复
            </el-button>
            <el-button
              size="small"
              type="primary"
              :loading="submittingComment"
              @click="handleSubmitComment"
            >
              发表评论
            </el-button>
          </div>
        </div>
        <div
          v-loading="commentLoading"
          class="comment-list"
        >
          <el-empty
            v-if="!commentLoading && comments.length === 0"
            description="还没有评论，来抢沙发"
            :image-size="60"
          />
          <div
            v-for="comment in comments"
            :key="comment.id"
            class="comment-item"
          >
            <div class="comment-head">
              <span class="comment-nickname">{{ comment.nickname }}</span>
              <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
              <el-button
                v-if="userStore.userInfo?.userId === comment.userId"
                size="small"
                text
                type="danger"
                @click="handleDeleteComment(comment)"
              >
                删除
              </el-button>
            </div>
            <p class="comment-content">
              {{ comment.content }}
            </p>
            <el-button
              size="small"
              text
              type="primary"
              @click="startReply(comment)"
            >
              回复
            </el-button>
            <div
              v-if="comment.replies && comment.replies.length"
              class="comment-replies"
            >
              <div
                v-for="reply in comment.replies"
                :key="reply.id"
                class="comment-reply"
              >
                <span class="comment-nickname">{{ reply.nickname }}</span>
                <span class="comment-time">{{ formatTime(reply.createTime) }}</span>
                <span class="comment-reply-content">{{ reply.content }}</span>
                <el-button
                  v-if="userStore.userInfo?.userId === reply.userId"
                  size="small"
                  text
                  type="danger"
                  @click="handleDeleteComment(reply)"
                >
                  删除
                </el-button>
              </div>
            </div>
          </div>
          <el-pagination
            v-if="commentsTotal > 20"
            v-model:current-page="commentsPage"
            layout="prev, pager, next"
            :total="commentsTotal"
            :page-size="20"
            @current-change="fetchComments"
          />
        </div>
      </el-card>

      <div class="detail-actions">
        <el-button @click="router.back()">
          返回列表
        </el-button>
      </div>
    </template>
    <el-empty
      v-else-if="!loading"
      description="文章不存在或已被删除"
    />
  </div>
</template>

<style scoped>
.detail-page {
  max-width: 800px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-card,
.comment-card {
  border-radius: 8px;
}

.detail-title {
  margin: 8px 0 16px;
  font-size: calc(var(--app-font-size) + 10px);
  color: var(--app-text);
}

.detail-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
  font-size: calc(var(--app-font-size) - 3px);
  color: var(--app-text-secondary);
}

.detail-author {
  color: var(--el-color-primary);
  font-weight: 600;
}

.detail-time {
  margin-left: auto;
}

.detail-stats {
  display: flex;
  gap: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--app-border);
  font-size: calc(var(--app-font-size) - 3px);
  color: var(--app-text-secondary);
}

.detail-stats svg {
  vertical-align: -2px;
}

.detail-content {
  padding-top: 8px;
}

.detail-interactions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--app-border);
}

.detail-interactions svg {
  margin-right: 4px;
  vertical-align: -2px;
}

.comment-title {
  margin: 0 0 12px;
  font-size: calc(var(--app-font-size) + 2px);
  color: var(--app-text);
}

.comment-input {
  margin-bottom: 16px;
}

.comment-input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 8px;
}

.comment-item {
  padding: 12px 0;
  border-top: 1px solid var(--app-border);
}

.comment-head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.comment-nickname {
  font-weight: 600;
  font-size: calc(var(--app-font-size) - 2px);
  color: var(--app-text-secondary);
}

.comment-time {
  font-size: calc(var(--app-font-size) - 4px);
  color: var(--app-text-secondary);
}

.comment-content {
  margin: 6px 0;
  font-size: calc(var(--app-font-size) - 2px);
  color: var(--app-text);
  white-space: pre-wrap;
}

.comment-replies {
  margin-top: 8px;
  padding: 8px 12px;
  background: var(--app-muted);
  border-radius: 6px;
}

.comment-reply {
  display: flex;
  align-items: baseline;
  gap: 8px;
  padding: 4px 0;
  font-size: calc(var(--app-font-size) - 3px);
}

.comment-reply-content {
  flex: 1;
  color: var(--app-text-secondary);
  white-space: pre-wrap;
}
</style>
