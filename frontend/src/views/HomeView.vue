<script setup lang="ts">
import { onActivated, ref, watch } from 'vue'
import { onBeforeRouteLeave, useRoute } from 'vue-router'
import { IconLocationFilled } from '@tabler/icons-vue'
import ArticleCard from '@/components/ArticleCard.vue'
import { getArticleList, searchArticles, type ArticleVO } from '@/api/article'
import bgImage from '@/assets/background-light.webp'
import bgCover from '@/assets/bg-cover-light.webp'

// 固定组件名，供 App.vue 的 KeepAlive include 匹配（缓存主页）
defineOptions({ name: 'HomeView' })

const route = useRoute()

// 返回时恢复列表位置：离开主页前（导航守卫，早于任何滚动）记录位置并打"待恢复"标记，
// 回来后在"组件激活"或"列表数据加载完成"时恢复，兼容 KeepAlive 缓存 / 未缓存两种情况
const HOME_SCROLL_KEY = 'community_home_scroll'
const HOME_RETURN_FLAG = 'community_home_return'

onBeforeRouteLeave(() => {
  sessionStorage.setItem(HOME_SCROLL_KEY, String(window.scrollY))
  sessionStorage.setItem(HOME_RETURN_FLAG, '1')
})

// 有"待恢复"标记才恢复，执行后清除标记，避免重复/误恢复
function tryRestoreScroll() {
  if (!sessionStorage.getItem(HOME_RETURN_FLAG)) return
  sessionStorage.removeItem(HOME_RETURN_FLAG)
  const saved = Number(sessionStorage.getItem(HOME_SCROLL_KEY) || 0)
  // rAF 确保晚于路由自身的滚动（回顶部）再执行
  requestAnimationFrame(() => {
    window.scrollTo(0, saved)
  })
}

// KeepAlive 缓存路径：返回时组件被激活，此时列表数据还在，直接恢复
onActivated(tryRestoreScroll)

const loading = ref(false)
const articles = ref<ArticleVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10
const listRef = ref<HTMLElement>()

// 搜索状态来自地址栏 ?keyword=，导航栏搜索提交后跳到这里
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
    // 错误提示已在 axios 拦截器统一处理
  } finally {
    loading.value = false
  }
}

// 监听地址栏关键字：进入/离开搜索模式并重新加载列表（immediate 负责首屏）
watch(
  () => route.query.keyword,
  async (val) => {
    keyword.value = typeof val === 'string' ? val : ''
    searchMode.value = keyword.value.trim().length > 0
    currentPage.value = 1
    await fetchArticles()
    // 未走缓存路径时组件重新挂载，等数据加载完页面高度正确后再恢复位置
    tryRestoreScroll()
  },
  { immediate: true },
)

// 逐帧缓动滚动：不依赖浏览器 smooth 支持，保证"缓缓下移"
function smoothScrollTo(targetY: number, duration = 900) {
  const startY = window.scrollY
  const distance = targetY - startY
  if (Math.abs(distance) < 1) return

  const startTime = performance.now()
  function step(now: number) {
    const progress = Math.min((now - startTime) / duration, 1)
    // easeInOutCubic：先慢后快再慢
    const eased =
      progress < 0.5
        ? 4 * progress ** 3
        : 1 - (-2 * progress + 2) ** 3 / 2
    window.scrollTo(0, startY + distance * eased)
    if (progress < 1) requestAnimationFrame(step)
  }
  requestAnimationFrame(step)
}

// 立刻探索：点击后页面缓慢向下滚动到文章列表
function scrollToArticles() {
  const el = listRef.value
  if (!el) return
  // 停在导航栏下方约 120px，而不是贴导航栏
  const top = el.getBoundingClientRect().top + window.scrollY - 120
  smoothScrollTo(top)
}
</script>

<template>
  <div class="home">
    <!-- Hero：整屏背景图 + 欢迎语 + 立刻探索 -->
    <section
      class="hero"
      :style="{
        backgroundImage: `linear-gradient(rgba(255, 255, 255, 0.6), rgba(255, 255, 255, 0.6)), url('${bgImage}')`,
      }"
    >
      <!-- hero 下方的装饰图：位于 hero 背景之上（位置后续再调） -->
      <img
        class="hero-cover"
        :src="bgCover"
        alt=""
        aria-hidden="true"
      >
      <div class="hero-inner">
        <h1 class="hero-title">
          <span class="hero-line">欢迎来到</span>
          <span class="hero-line">
            <span class="gradient-text">鸡舍</span>官方社区
          </span>
        </h1>
        <p class="hero-desc">
          这里记录技术、生活与一切有趣的事，欢迎分享你的故事
        </p>
        <button
          class="explore-btn"
          @click="scrollToArticles"
        >
          立刻探索
          <IconLocationFilled />
        </button>
      </div>
    </section>

    <!-- 过渡区：方案 A —— 单层白色缓动渐隐，向下露出整站背景 -->
    <div
      class="hero-fade"
      aria-hidden="true"
    />

    <!-- 下方：之前的文章列表 -->
    <section
      ref="listRef"
      class="article-section"
    >
      <h2 class="section-title">
        {{ searchMode ? `“${keyword.trim()}” 的搜索结果` : '最新发布' }}
      </h2>
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
    </section>
  </div>
</template>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
}

/* Hero：背景图铺满首屏，内容居中，不设多余边距 */
.hero {
  position: relative;
  min-height: calc(100vh - 60px);
  display: flex;
  align-items: center;
  justify-content: center;
  background-size: cover, cover;
  background-position: center, center;
  padding: 0;
}

/* 装饰图：全宽铺满 hero 底部，图层在 hero 背景之上、文字按钮之下 */
.hero-cover {
  position: absolute;
  bottom: -390px;
  left: 0;
  width: 100%;
  height: auto;
  z-index: 1;
  pointer-events: none;
  user-select: none;
}

/* 过渡带：方案 A —— 单层白色缓动渐隐（密集色标模拟缓动曲线）。
   不做不透明底色层，底部完全透明直接露出整站背景，与文章区域背景完全一致，杜绝分界线。
   margin-top 390px 让过渡带从图片底部开始，400px 全部可见，图片位置不变 */
.hero-fade {
  position: relative;
  height: 400px;
  margin-top: 390px;
  overflow: hidden;
  background: linear-gradient(
    to bottom,
    #ffffff 0%,
    #ffffff 10%,
    rgba(255, 255, 255, 0.97) 16%,
    rgba(255, 255, 255, 0.9) 24%,
    rgba(255, 255, 255, 0.78) 33%,
    rgba(255, 255, 255, 0.6) 42%,
    rgba(255, 255, 255, 0.42) 51%,
    rgba(255, 255, 255, 0.27) 60%,
    rgba(255, 255, 255, 0.15) 69%,
    rgba(255, 255, 255, 0.07) 78%,
    rgba(255, 255, 255, 0.03) 87%,
    rgba(255, 255, 255, 0) 95%,
    rgba(255, 255, 255, 0) 100%
  );
}

.hero-inner {
  position: relative;
  z-index: 2;
  width: 100%;
  max-width: 800px;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 24px;
}

.hero-title {
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  /* 先给固定值兜底，再用滑块变量覆盖，保证字号一定生效 */
  font-size: 72px;
  font-size: calc(var(--app-font-size) + 56px);
  font-weight: 800;
  /* Hero 背景图固定浅色，标题用固定深色保证可读；渐变字和按钮仍跟随主题 */
  color: #1d2129;
  letter-spacing: 2px;
}

/* 第一行小一些，第二行大标题 */
.hero-line:first-child {
  font-size: 46px;
  font-size: calc(var(--app-font-size) + 30px);
  font-weight: 600;
}

/* 鸡舍两字：主题色渐变（左下 → 右上），蓝→紫 / 红→橙 / 绿→黄，切主题跟随变化 */
.gradient-text {
  display: inline-block;
  background-image: linear-gradient(45deg, var(--app-grad-start), var(--app-grad-end));
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  -webkit-text-fill-color: transparent;
}

.hero-desc {
  margin: 0;
  font-size: 24px;
  font-size: calc(var(--app-font-size) + 8px);
  color: #667085;
}

/* 立刻探索：与上方隔开一点，加一圈淡色边框，hover 缓缓放大，点击缓慢下移 */
.explore-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 11px 36px;
  font-size: 26px;
  font-size: calc(var(--app-font-size) + 10px);
  font-weight: 600;
  color: #fff;
  background: var(--app-primary);
  border: 2px solid var(--el-color-primary-light-3);
  border-radius: 999px;
  cursor: pointer;
  box-shadow: 0 4px 14px var(--app-primary-soft);
  transition:
    transform 0.3s ease,
    background-color 0.3s ease,
    box-shadow 0.3s ease,
    border-color 0.3s ease;
}

.explore-btn:hover {
  transform: scale(1.06);
  background: var(--app-primary-strong);
  box-shadow: 0 6px 22px var(--app-primary-soft);
}

/* 按下时缓慢向下移动 */
.explore-btn:active {
  transform: translateY(4px) scale(1.02);
}

.explore-btn svg {
  width: 0.85em;
  height: 0.85em;
}

/* 下方文章列表 */
.article-section {
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
  margin-top: 10px;
  padding: 0 16px 48px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-title {
  margin: 0;
  font-size: calc(var(--app-font-size) + 6px);
  color: var(--app-text);
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
