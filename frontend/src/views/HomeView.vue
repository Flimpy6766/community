<script setup lang="ts">
import { onActivated, ref, watch } from 'vue'
import { onBeforeRouteLeave, useRoute } from 'vue-router'
import ArticleCard from '@/components/ArticleCard.vue'
import HomeQuickNav from '@/components/HomeQuickNav.vue'
import HotArticleWidget from '@/components/HotArticleWidget.vue'
import { getArticleList, searchArticles, type ArticleVO } from '@/api/article'
import { appConfig } from '@/config/app'

defineOptions({ name: 'HomeView' })

const route = useRoute()
const HOME_SCROLL_KEY = 'community_home_scroll'
const HOME_RETURN_FLAG = 'community_home_return'

onBeforeRouteLeave(() => {
  sessionStorage.setItem(HOME_SCROLL_KEY, String(window.scrollY))
  sessionStorage.setItem(HOME_RETURN_FLAG, '1')
})

function tryRestoreScroll() {
  if (!sessionStorage.getItem(HOME_RETURN_FLAG)) return
  sessionStorage.removeItem(HOME_RETURN_FLAG)
  const saved = Number(sessionStorage.getItem(HOME_SCROLL_KEY) || 0)
  requestAnimationFrame(() => window.scrollTo(0, saved))
}

onActivated(tryRestoreScroll)

const loading = ref(false)
const articles = ref<ArticleVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = appConfig.pagination.defaultPageSize
const keyword = ref('')
const searchMode = ref(false)

async function fetchArticles() {
  loading.value = true
  try {
    const data = searchMode.value
      ? await searchArticles(keyword.value.trim(), currentPage.value, pageSize)
      : await getArticleList(currentPage.value, pageSize)
    articles.value = data.records
    total.value = data.total
  } catch {
    // 请求错误由 Axios 统一处理
  } finally {
    loading.value = false
  }
}

watch(
  () => route.query.keyword,
  async (val) => {
    keyword.value = typeof val === 'string' ? val : ''
    searchMode.value = keyword.value.trim().length > 0
    currentPage.value = 1
    await fetchArticles()
    tryRestoreScroll()
  },
  { immediate: true },
)
</script>

<template>
  <div class="home">
    <section class="article-page">
      <HomeQuickNav
        class="article-sidebar article-sidebar--left"
        :article-total="total"
      />
      <div class="article-page-main">
        <header class="page-header">
          <p class="page-kicker">COMMUNITY / ARTICLES</p>
          <h1>{{ searchMode ? `搜索「${keyword.trim()}」` : '最新文章' }}</h1>
          <p class="page-description">
            {{ searchMode ? '按关键词浏览社区中的文章。' : '从这里开始，阅读社区最近发布的内容。' }}
          </p>
        </header>

        <div class="list-toolbar">
          <span class="result-count">{{ total }} 篇文章</span>
          <span class="toolbar-hint">点击文章查看全文</span>
        </div>

        <div
          v-loading="loading"
          class="article-container"
        >
          <el-empty
            v-if="!loading && articles.length === 0"
            :description="searchMode ? '没有找到相关文章，换个关键词试试' : '还没有文章，去发布第一篇吧'"
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
      <HotArticleWidget class="article-sidebar article-sidebar--right" />
    </section>
  </div>
</template>

<style scoped>
.home {
  min-height: 100vh;
}

.article-page {
  width: min(1320px, calc(100% - 56px));
  margin: 0 auto;
  padding: 64px 0 80px;
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr) 280px;
  gap: 56px;
}

.article-page-main {
  min-width: 0;
}

.page-header {
  padding-bottom: 34px;
  border-bottom: 1px solid var(--app-border);
}

.page-kicker,
.aside-label {
  margin: 0 0 14px;
  color: var(--app-primary-strong);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.16em;
}

.page-header h1 {
  margin: 0;
  color: var(--app-text);
  font-size: clamp(32px, 4vw, 48px);
  font-weight: 750;
  letter-spacing: -0.04em;
  line-height: 1.1;
}

.page-description {
  max-width: 520px;
  margin: 16px 0 0;
  color: var(--app-text-secondary);
  font-size: 15px;
  line-height: 1.7;
}

.list-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 0 12px;
}

.result-count {
  color: var(--app-text);
  font-size: 13px;
  font-weight: 650;
}

.toolbar-hint {
  color: var(--app-text-secondary);
  font-size: 12px;
}

.article-container {
  display: flex;
  flex-direction: column;
  gap: 0;
  min-height: 240px;
}

.article-pagination {
  margin-top: 28px;
  justify-content: center;
}

@media (max-width: 800px) {
  .article-page {
    width: min(100% - 28px, 680px);
    padding: 36px 0 56px;
    display: block;
  }

  .article-sidebar {
    display: none;
  }

  .page-header {
    padding-bottom: 26px;
  }

  .list-toolbar {
    padding-top: 18px;
  }
}
</style>
