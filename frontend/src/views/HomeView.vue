<script setup lang="ts">
import { onMounted, ref } from 'vue'
import ArticleCard from '@/components/ArticleCard.vue'
import { getArticleList, type ArticleVO } from '@/api/article'

const loading = ref(false)
const articles = ref<ArticleVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10

async function fetchArticles() {
  loading.value = true
  try {
    const data = await getArticleList(currentPage.value, pageSize)
    articles.value = data.records
    total.value = data.total
  } catch {
    // 错误提示已在 axios 拦截器统一处理
  } finally {
    loading.value = false
  }
}

onMounted(fetchArticles)
</script>

<template>
  <div class="article-list">
    <div
      v-loading="loading"
      class="article-container"
    >
      <el-empty
        v-if="!loading && articles.length === 0"
        description="还没有文章，去发布第一篇吧"
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
      class="article-pagination"
      layout="prev, pager, next"
      :total="total"
      :page-size="pageSize"
      @current-change="fetchArticles"
    />
  </div>
</template>

<style scoped>
.article-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.article-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 200px;
}

.article-pagination {
  justify-content: center;
}
</style>
