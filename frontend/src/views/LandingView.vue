<script setup lang="ts">
import { IconLocationFilled } from '@tabler/icons-vue'
import { useRouter } from 'vue-router'
import bgImage from '@/assets/background-light.webp'
import bgCover from '@/assets/bg-cover-light.webp'
import { siteConfig } from '@/config/site'

defineOptions({ name: 'LandingView' })

const router = useRouter()

function openArticles() {
  router.push('/home')
}
</script>

<template>
  <div class="landing-page">
    <section
      class="hero"
      :style="{
        backgroundImage: `linear-gradient(rgba(255, 255, 255, 0.6), rgba(255, 255, 255, 0.6)), url('${bgImage}')`,
      }"
    >
      <img
        class="hero-cover"
        :src="bgCover"
        alt=""
        aria-hidden="true"
      >
      <div class="hero-inner">
        <p class="hero-kicker">COMMUNITY / KNOWLEDGE / SHARING</p>
        <h1 class="hero-title">
          <span class="hero-line">欢迎来到</span>
          <span class="hero-line"><span class="gradient-text">{{ siteConfig.name }}</span></span>
        </h1>
        <p class="hero-desc">{{ siteConfig.description }} 欢迎分享你的故事。</p>
        <button
          class="explore-btn"
          @click="openArticles"
        >
          浏览文章
          <IconLocationFilled />
        </button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.landing-page {
  width: 100vw;
  margin-left: calc(50% - 50vw);
  min-height: calc(100vh - 60px);
}

.hero {
  position: relative;
  min-height: calc(100vh - 60px);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background-size: cover;
  background-position: center;
}

.hero-cover {
  position: absolute;
  bottom: -390px;
  left: 0;
  width: 100%;
  z-index: 1;
  pointer-events: none;
  user-select: none;
}

.hero-inner {
  position: relative;
  z-index: 2;
  width: min(800px, calc(100% - 32px));
  text-align: center;
}

.hero-kicker {
  margin: 0 0 22px;
  color: #667085;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.18em;
}

.hero-title {
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  color: #1d2129;
  font-size: clamp(46px, 7vw, 72px);
  font-weight: 800;
  letter-spacing: 2px;
}

.hero-line:first-child {
  font-size: clamp(30px, 4vw, 46px);
  font-weight: 600;
}

.gradient-text {
  display: inline-block;
  background-image: linear-gradient(45deg, var(--app-grad-start), var(--app-grad-end));
  background-clip: text;
  color: transparent;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.hero-desc {
  margin: 28px 0 0;
  color: #667085;
  font-size: clamp(16px, 2vw, 24px);
}

.explore-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin-top: 34px;
  padding: 11px 30px;
  color: #fff;
  background: var(--app-primary);
  border: 2px solid var(--el-color-primary-light-3);
  border-radius: 999px;
  font-size: 16px;
  font-weight: 650;
  cursor: pointer;
  box-shadow: 0 4px 14px var(--app-primary-soft);
  transition: transform 0.2s ease, background-color 0.2s ease;
}

.explore-btn:hover {
  transform: translateY(-2px);
  background: var(--app-primary-strong);
}

.explore-btn svg {
  width: 1em;
  height: 1em;
}

@media (max-width: 560px) {
  .hero-cover {
    bottom: -180px;
    width: 180%;
    left: -40%;
  }
}
</style>
