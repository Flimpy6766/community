<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getHotArticles, type ArticleVO } from '@/api/article'
import { appConfig } from '@/config/app'

const loading = ref(false)
const articles = ref<ArticleVO[]>([])
const trendingTags = computed(() => {
  const counts = new Map<string, number>()
  articles.value.forEach((article) => {
    article.tags?.forEach((tag) => counts.set(tag, (counts.get(tag) || 0) + 1))
  })
  return [...counts.entries()].sort((a, b) => b[1] - a[1]).slice(0, 6).map(([tag]) => tag)
})

async function fetchHotArticles() {
  loading.value = true
  try {
    articles.value = await getHotArticles(appConfig.hot.sidebarLimit)
  } catch {
    // 错误提示由 Axios 统一处理
  } finally {
    loading.value = false
  }
}

onMounted(fetchHotArticles)
</script>

<template>
  <aside
    v-loading="loading"
    class="hot-widget"
  >
    <p class="widget-kicker">TRENDING NOW</p>
    <div class="widget-heading">
      <h2 class="widget-title">热门文章</h2>
      <router-link class="view-all" to="/hot">进入完整热榜 →</router-link>
    </div>
    <ol
      v-if="articles.length"
      class="hot-list"
    >
      <li
        v-for="(article, index) in articles"
        :key="article.id"
        class="hot-item"
      >
        <span
          class="hot-rank"
          :class="{ 'hot-rank--top': index < 3 }"
        >
          {{ index + 1 }}
        </span>
        <router-link
          class="hot-title"
          :to="`/article/${article.id}`"
        >
          {{ article.title }}
        </router-link>
      </li>
    </ol>
    <p
      v-else-if="!loading"
      class="hot-empty"
    >
      暂无热榜数据
    </p>
    <div v-if="trendingTags.length" class="tag-section">
      <p class="section-label">热门标签</p>
      <div class="tag-list">
        <span v-for="tag in trendingTags" :key="tag" class="tag-chip"># {{ tag }}</span>
      </div>
    </div>
    <div class="right-note">
      <span class="right-note-dot" />
      <span>每天都有新的想法值得分享</span>
    </div>
  </aside>
</template>

<style scoped>
.hot-widget {
  position: sticky;
  top: 92px;
  align-self: start;
}

.widget-kicker {
  margin: 0 0 10px;
  color: var(--app-primary-strong);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.16em;
}

.widget-heading {
  margin-bottom: 14px;
}

.widget-title {
  margin: 0;
  color: var(--app-text);
  font-size: 18px;
  letter-spacing: -0.03em;
}

.widget-heading .view-all {
  display: inline-block;
  margin-top: 5px;
  color: var(--app-primary-strong);
  font-size: 11px;
}

.hot-list {
  display: flex;
  flex-direction: column;
  gap: 13px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.hot-item {
  display: grid;
  grid-template-columns: 20px minmax(0, 1fr);
  gap: 8px;
  align-items: start;
}

.hot-rank {
  color: var(--app-text-secondary);
  font-size: 12px;
  font-weight: 700;
}

.hot-rank--top {
  color: var(--app-primary-strong);
}

.hot-title {
  display: -webkit-box;
  overflow: hidden;
  color: var(--app-text-secondary);
  font-size: 12px;
  line-height: 1.5;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.hot-title:hover {
  color: var(--app-primary-strong);
}

.hot-empty {
  margin: 0;
  color: var(--app-text-secondary);
  font-size: 12px;
}

.tag-section {
  margin-top: 28px;
  padding-top: 18px;
  border-top: 1px solid var(--app-border);
}

.section-label {
  margin: 0 0 11px;
  color: var(--app-text-secondary);
  font-size: 11px;
  font-weight: 650;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
}

.tag-chip {
  padding: 5px 8px;
  border: 1px solid var(--app-border);
  border-radius: 999px;
  color: var(--app-text-secondary);
  font-size: 10px;
}

.right-note {
  display: flex;
  align-items: center;
  gap: 7px;
  margin-top: 28px;
  color: var(--app-text-secondary);
  font-size: 11px;
  line-height: 1.5;
}

.right-note-dot {
  width: 6px;
  height: 6px;
  flex: 0 0 auto;
  border-radius: 50%;
  background: var(--app-primary);
  box-shadow: 0 0 0 4px var(--app-primary-soft);
}
</style>
