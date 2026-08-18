<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { IconEyeFilled, IconMessageCircleFilled, IconThumbUpFilled } from '@tabler/icons-vue'
import type { ArticleVO } from '@/api/article'

// 父组件传入文章数据；rank 传了就在标题前显示排名（热榜用）
const props = defineProps<{
  article: ArticleVO
  rank?: number
}>()

const router = useRouter()

function formatTime(time: string) {
  return time.replace('T', ' ').slice(0, 16)
}

// 正文是 Markdown，去掉常用语法符号后作为摘要展示（图片/链接/标题/强调等）
function excerpt(text: string): string {
  return text
    .replace(/!\[[^\]]*\]\([^)]*\)/g, '')
    .replace(/\[([^\]]*)\]\([^)]*\)/g, '$1')
    .replace(/^#{1,6}\s*/gm, '')
    .replace(/^>\s*/gm, '')
    .replace(/^[-*+]\s+/gm, '')
    .replace(/^\d+\.\s+/gm, '')
    .replace(/[`*_~]/g, '')
    .replace(/\s+/g, ' ')
    .trim()
}

// 展示的正文摘要：优先 content，没有就用 summary
const preview = computed(() => excerpt(props.article.content || props.article.summary || ''))
</script>

<template>
  <article
    class="article-card"
    :class="{ 'article-card--with-cover': article.cover }"
    tabindex="0"
    role="link"
    :aria-label="`阅读文章：${article.title}`"
    @click="router.push(`/article/${article.id}`)"
    @keydown.enter="router.push(`/article/${article.id}`)"
  >
    <div class="article-card-body">
      <div class="article-card-head">
        <span
          v-if="rank"
          class="article-rank"
          :class="`rank-${rank}`"
        >
          {{ rank }}
        </span>
        <p class="article-author">
          {{ article.nickname || `用户 ${article.userId}` }}
        </p>
      </div>
      <h2 class="article-title">
        {{ article.title }}
      </h2>
      <p
        v-if="preview"
        class="article-content"
      >
        {{ preview }}
      </p>
      <div
        v-if="article.tags?.length"
        class="article-tags"
      >
        <el-tag
          v-for="tag in article.tags"
          :key="tag"
          class="article-tag"
          size="small"
          effect="plain"
        >
          {{ tag }}
        </el-tag>
      </div>
      <div class="article-stats">
        <span><IconEyeFilled :size="14" />{{ article.viewCount }}</span>
        <span><IconThumbUpFilled :size="14" />{{ article.likeCount }}</span>
        <span><IconMessageCircleFilled :size="14" />{{ article.commentCount }}</span>
        <span class="article-time">{{ formatTime(article.createTime) }}</span>
      </div>
    </div>
    <img
      v-if="article.cover"
      class="article-cover"
      :src="article.cover"
      alt=""
      loading="lazy"
    >
  </article>
</template>

<style scoped>
.article-card {
  position: relative;
  padding: 23px 8px 25px 0;
  border-top: 1px solid var(--app-border);
  cursor: pointer;
  transition:
    padding 0.2s ease,
    background-color 0.2s ease;
}

.article-card:focus-visible {
  outline: 2px solid var(--app-primary);
  outline-offset: -2px;
}

.article-card:hover {
  padding-left: 12px;
  background: var(--app-muted);
}

.article-card-head {
  display: flex;
  align-items: center;
  gap: 9px;
}

.article-author {
  margin: 0;
  font-size: 12px;
  font-weight: 600;
  color: var(--app-text-secondary);
}

.article-rank {
  width: 24px;
  height: 24px;
  line-height: 24px;
  text-align: center;
  border-radius: 4px;
  font-size: calc(var(--app-font-size) - 2px);
  font-weight: 700;
  color: #fff;
  background: #86909c;
  margin-right: 8px;
  flex-shrink: 0;
}

.article-card-body {
  min-width: 0;
}

.article-card--with-cover {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 148px;
  gap: 24px;
  align-items: center;
}

.article-cover {
  width: 148px;
  height: 96px;
  object-fit: cover;
  border-radius: var(--app-radius-md);
  background: var(--app-muted);
}

.rank-1 {
  background: #f5222d;
}

.rank-2 {
  background: #fa8c16;
}

.rank-3 {
  background: #1677ff;
}

.article-title {
  margin: 0;
  font-size: clamp(18px, 2vw, 22px);
  font-weight: 700;
  color: var(--app-text);
  letter-spacing: -0.02em;
  line-height: 1.35;
}

.article-content {
  max-width: 720px;
  margin: 10px 0 13px;
  font-size: 14px;
  color: var(--app-text-secondary);
  line-height: 1.75;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 13px;
}

.article-tag {
  --el-tag-bg-color: var(--app-primary-soft) !important;
  --el-tag-border-color: transparent !important;
  --el-tag-text-color: var(--app-primary-strong) !important;
}

.article-stats {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 12px;
  color: var(--app-text-secondary);
}

.article-stats svg {
  vertical-align: -2px;
  flex-shrink: 0;
}

.article-time {
  margin-left: auto;
}

@media (max-width: 560px) {
  .article-card {
    padding: 20px 0 22px;
  }

  .article-card:hover {
    padding-left: 6px;
  }

  .article-time {
    display: none;
  }

  .article-card--with-cover {
    grid-template-columns: minmax(0, 1fr) 92px;
    gap: 12px;
  }

  .article-cover {
    width: 92px;
    height: 72px;
  }
}
</style>
