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
  <div
    class="article-card"
    @click="router.push(`/article/${article.id}`)"
  >
    <p class="article-author">
      {{ article.nickname || `用户 ${article.userId}` }}
    </p>
    <div class="article-card-head">
      <span
        v-if="rank"
        class="article-rank"
        :class="`rank-${rank}`"
      >
        {{ rank }}
      </span>
      <h2 class="article-title">
        {{ article.title }}
      </h2>
    </div>
    <p
      v-if="preview"
      class="article-content"
    >
      {{ preview }}
    </p>
    <div class="article-tags">
      <el-tag
        v-for="tag in article.tags"
        :key="tag"
        size="small"
        type="info"
        effect="plain"
      >
        {{ tag }}
      </el-tag>
    </div>
    <div class="article-stats">
      <span>
        <IconEyeFilled :size="15" />
        {{ article.viewCount }}
      </span>
      <span>
        <IconThumbUpFilled :size="15" />
        {{ article.likeCount }}
      </span>
      <span>
        <IconMessageCircleFilled :size="15" />
        {{ article.commentCount }}
      </span>
      <span class="article-time">{{ formatTime(article.createTime) }}</span>
    </div>
  </div>
</template>

<style scoped>
.article-card {
  background: var(--app-surface-solid);
  border: 1px solid var(--app-border);
  border-radius: 8px;
  padding: 16px 20px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.article-card:hover {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.article-card-head {
  display: flex;
  align-items: center;
}

.article-author {
  margin: 0 0 6px;
  font-size: calc(var(--app-font-size) - 3px);
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
  font-size: calc(var(--app-font-size) + 4px);
  font-weight: 700;
  color: var(--app-text);
  line-height: 1.4;
}

.article-content {
  margin: 8px 0 10px;
  font-size: calc(var(--app-font-size) - 1px);
  color: var(--app-text-secondary);
  line-height: 1.6;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.article-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}

.article-stats {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: calc(var(--app-font-size) - 3px);
  color: var(--app-text-secondary);
}

.article-stats svg {
  vertical-align: -2px;
  flex-shrink: 0;
}

.article-time {
  margin-left: auto;
}
</style>
