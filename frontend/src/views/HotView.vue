<script setup lang="ts">
import { onMounted, ref } from 'vue'
import ArticleCard from '@/components/ArticleCard.vue'
import { getHotArticles, type ArticleVO } from '@/api/article'
import { appConfig } from '@/config/app'

const loading = ref(false)
const articles = ref<ArticleVO[]>([])

async function fetchHot() {
  loading.value = true
  try {
    articles.value = await getHotArticles(appConfig.hot.defaultLimit)
  } catch {
    // 错误提示已在拦截器统一处理
  } finally {
    loading.value = false
  }
}

onMounted(fetchHot)
</script>

<template>
  <div
    v-loading="loading"
    class="hot-page"
  >
    <header class="page-header">
      <p class="page-kicker">COMMUNITY / TRENDING</p>
      <h1 class="page-title">热榜</h1>
      <p class="page-description">看看社区里最近最受关注的文章。</p>
    </header>
    <div class="list-toolbar">
      <span>{{ articles.length }} 篇文章</span>
      <span>按热度排序</span>
    </div>
    <div class="article-list">
      <el-empty
        v-if="!loading && articles.length === 0"
        description="还没有热度数据"
      />
      <ArticleCard
        v-for="(article, index) in articles"
        :key="article.id"
        :article="article"
        :rank="index + 1"
      />
    </div>
  </div>
</template>

<style scoped>
.hot-page {
  width: min(860px, 100%);
  margin: 0 auto;
  padding: 36px 0 72px;
  display: flex;
  flex-direction: column;
}

.page-header {
  padding-bottom: 28px;
  border-bottom: 1px solid var(--app-border);
}

.page-kicker {
  margin: 0 0 12px;
  color: var(--app-primary-strong);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.16em;
}

.page-title {
  margin: 0;
  font-size: clamp(32px, 4vw, 46px);
  font-weight: 750;
  letter-spacing: -0.04em;
  color: var(--app-text);
}

.page-description {
  margin: 14px 0 0;
  color: var(--app-text-secondary);
  font-size: 15px;
}

.list-toolbar {
  display: flex;
  justify-content: space-between;
  padding: 20px 0 12px;
  color: var(--app-text-secondary);
  font-size: 12px;
}

.list-toolbar span:first-child {
  color: var(--app-text);
  font-weight: 650;
}

.article-list {
  display: flex;
  flex-direction: column;
}
</style>
