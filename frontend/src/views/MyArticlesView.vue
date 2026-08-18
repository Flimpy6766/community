<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import ArticleCard from '@/components/ArticleCard.vue'
import { getMyArticles, type ArticleVO } from '@/api/article'
import { appConfig } from '@/config/app'

type StatusTab = 'all' | 'draft' | 'published'

const router = useRouter()

const loading = ref(false)
const articles = ref<ArticleVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = appConfig.pagination.defaultPageSize
const activeTab = ref<StatusTab>('all')

// tab → 后端 status 参数（undefined = 全部）
function statusParam(tab: StatusTab): number | undefined {
  if (tab === 'draft') return 0
  if (tab === 'published') return 1
  return undefined
}

const emptyText = computed(() => {
  if (activeTab.value === 'draft') return '草稿箱是空的'
  if (activeTab.value === 'published') return '还没有已发布的文章'
  return '还没有写过文章，去发布第一篇吧'
})

async function fetchArticles() {
  loading.value = true
  try {
    const data = await getMyArticles(statusParam(activeTab.value), currentPage.value, pageSize)
    articles.value = data.records
    total.value = data.total
  } catch {
    // 错误提示已在拦截器统一处理
  } finally {
    loading.value = false
  }
}

// 切换 tab：回到第一页重新拉取
watch(activeTab, () => {
  currentPage.value = 1
  fetchArticles()
})

onMounted(fetchArticles)
</script>

<template>
  <div class="my-articles-page">
    <div class="page-head">
      <h1 class="page-title">
        我的文章
      </h1>
      <el-tabs v-model="activeTab">
        <el-tab-pane
          label="全部"
          name="all"
        />
        <el-tab-pane
          label="草稿箱"
          name="draft"
        />
        <el-tab-pane
          label="已发布"
          name="published"
        />
      </el-tabs>
      <el-button
        class="publish-btn"
        type="primary"
        @click="router.push('/article/create')"
      >
        发布新文章
      </el-button>
    </div>

    <div
      v-loading="loading"
      class="article-container"
    >
      <el-empty
        v-if="!loading && articles.length === 0"
        :description="emptyText"
      />
      <ArticleCard
        v-for="article in articles"
        :key="article.id"
        :article="article"
      />
      <el-pagination
        v-if="total > pageSize"
        v-model:current-page="currentPage"
        class="my-articles-pagination"
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        @current-change="fetchArticles"
      />
    </div>
  </div>
</template>

<style scoped>
.my-articles-page {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.page-head {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}

.page-title {
  margin: 0;
  font-size: calc(var(--app-font-size) + 6px);
  color: var(--app-text);
}

.publish-btn {
  margin-left: auto;
}

.article-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 200px;
}

.my-articles-pagination {
  justify-content: center;
}
</style>
