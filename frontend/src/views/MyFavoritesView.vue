<script setup lang="ts">
import { onMounted, ref } from 'vue'
import ArticleCard from '@/components/ArticleCard.vue'
import { getMyFavorites, type ArticleVO } from '@/api/article'

const loading = ref(false)
const articles = ref<ArticleVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10

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
    <h1 class="page-title">
      我的收藏
    </h1>
    <el-empty
      v-if="!loading && articles.length === 0"
      description="还没有收藏，去给喜欢的文章点个收藏吧"
    />
    <ArticleCard
      v-for="article in articles"
      :key="article.id"
      :article="article"
    />
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
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.page-title {
  margin: 0;
  font-size: calc(var(--app-font-size) + 6px);
  color: var(--app-text);
}

.favorites-pagination {
  justify-content: center;
}
</style>
