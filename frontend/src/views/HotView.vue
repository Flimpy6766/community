<script setup lang="ts">
import { onMounted, ref } from 'vue'
import ArticleCard from '@/components/ArticleCard.vue'
import { getHotArticles, type ArticleVO } from '@/api/article'

const loading = ref(false)
const articles = ref<ArticleVO[]>([])

async function fetchHot() {
  loading.value = true
  try {
    articles.value = await getHotArticles(10)
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
    <h1 class="page-title">
      🔥 热榜
    </h1>
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
</template>

<style scoped>
.hot-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.page-title {
  margin: 0;
  font-size: calc(var(--app-font-size) + 6px);
  color: var(--app-text);
}
</style>
