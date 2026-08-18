<script setup lang="ts">
import { onMounted, ref } from 'vue'
import ArticleCard from '@/components/ArticleCard.vue'
import { getMyFavorites, type ArticleVO } from '@/api/article'
import { appConfig } from '@/config/app'

const loading = ref(false)
const articles = ref<ArticleVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = appConfig.pagination.defaultPageSize

async function fetchFavorites() {
  loading.value = true
  try {
    const data = await getMyFavorites(currentPage.value, pageSize)
    articles.value = data.records
    total.value = data.total
  } catch {
    // 错误提示已在拦截器统一处理
  } finally {
    loading.value = false
  }
}

onMounted(fetchFavorites)
</script>

<template>
  <div
    v-loading="loading"
    class="favorites-page"
  >
    <header class="page-header">
      <p class="page-kicker">PERSONAL / SAVED</p>
      <h1 class="page-title">收藏</h1>
      <p class="page-description">保存你想稍后继续阅读的文章。</p>
    </header>
    <div class="list-toolbar">
      <span>{{ total }} 篇文章</span>
      <span>我的收藏</span>
    </div>
    <div class="article-list">
      <el-empty
        v-if="!loading && articles.length === 0"
        description="还没有收藏，去给喜欢的文章点个收藏吧"
      />
      <ArticleCard
        v-for="article in articles"
        :key="article.id"
        :article="article"
      />
    </div>
    <el-pagination
      v-if="total > pageSize"
      v-model:current-page="currentPage"
      class="favorites-pagination"
      layout="prev, pager, next"
      :total="total"
      :page-size="pageSize"
      @current-change="fetchFavorites"
    />
  </div>
</template>

<style scoped>
.favorites-page {
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

.favorites-pagination {
  justify-content: center;
}
</style>
