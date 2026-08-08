<script setup lang="ts">
import { useRouter } from 'vue-router'
import { ChatDotRound, Pointer, View } from '@element-plus/icons-vue'
import type { ArticleVO } from '@/api/article'

// 父组件传入文章数据；rank 传了就在标题前显示排名（热榜用）
defineProps<{
  article: ArticleVO
  rank?: number
}>()

const router = useRouter()

function formatTime(time: string) {
  return time.replace('T', ' ').slice(0, 16)
}
</script>

<template>
  <div
    class="article-card"
    @click="router.push(`/article/${article.id}`)"
  >
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
      v-if="article.summary"
      class="article-summary"
    >
      {{ article.summary }}
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
        <el-icon><View /></el-icon>
        {{ article.viewCount }}
      </span>
      <span>
        <el-icon><Pointer /></el-icon>
        {{ article.likeCount }}
      </span>
      <span>
        <el-icon><ChatDotRound /></el-icon>
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
  font-size: calc(var(--app-font-size) + 2px);
  color: var(--app-text);
}

.article-summary {
  margin: 8px 0 12px;
  font-size: calc(var(--app-font-size) - 2px);
  color: var(--app-text-secondary);
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.article-tags {
  display: flex;
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

.article-stats .el-icon {
  vertical-align: -2px;
}

.article-time {
  margin-left: auto;
}
</style>
