<script setup lang="ts">
import {
  IconEditFilled,
  IconFlameFilled,
  IconLayoutFilled,
  IconStarFilled,
} from '@tabler/icons-vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
defineProps<{ articleTotal: number }>()
</script>

<template>
  <aside class="quick-nav">
    <p class="widget-kicker">EXPLORE</p>
    <h2 class="widget-title">探索社区</h2>
    <nav class="quick-links" aria-label="社区导航">
      <router-link class="quick-link" to="/home">
        <IconLayoutFilled :size="16" />
        <span>全部文章</span>
      </router-link>
      <router-link class="quick-link" to="/hot">
        <IconFlameFilled :size="16" />
        <span>热榜</span>
      </router-link>
      <router-link class="quick-link" to="/favorites">
        <IconStarFilled :size="16" />
        <span>我的收藏</span>
      </router-link>
    </nav>
    <router-link
      v-if="userStore.userInfo"
      class="create-link"
      to="/article/create"
    >
      <IconEditFilled :size="15" />
      <span>开始创作</span>
    </router-link>
    <div class="community-card">
      <p class="community-card-label">COMMUNITY SNAPSHOT</p>
      <strong>{{ articleTotal.toLocaleString() }}</strong>
      <span>篇内容正在社区中沉淀</span>
    </div>
    <div class="prompt-card">
      <span class="prompt-mark">“</span>
      <p>把今天学到的一点东西，写下来。</p>
    </div>
  </aside>
</template>

<style scoped>
.quick-nav {
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

.widget-title {
  margin: 0 0 16px;
  color: var(--app-text);
  font-size: 18px;
  letter-spacing: -0.03em;
}

.quick-links {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.quick-link,
.create-link {
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 9px 10px;
  border-radius: var(--app-radius-sm);
  color: var(--app-text-secondary);
  font-size: 13px;
  transition: background-color 0.2s, color 0.2s;
}

.quick-link:hover,
.quick-link.router-link-active {
  color: var(--app-primary-strong);
  background: var(--app-primary-soft);
}

.create-link {
  margin-top: 20px;
  color: var(--app-primary-strong);
  border: 1px solid var(--app-border);
}

.create-link:hover {
  border-color: var(--app-primary);
  background: var(--app-primary-soft);
}

.community-card,
.prompt-card {
  margin-top: 28px;
  padding-top: 18px;
  border-top: 1px solid var(--app-border);
}

.community-card-label {
  margin: 0 0 8px;
  color: var(--app-text-secondary);
  font-size: 9px;
  letter-spacing: 0.12em;
}

.community-card strong {
  display: block;
  color: var(--app-text);
  font-size: 24px;
  letter-spacing: -0.04em;
}

.community-card span {
  color: var(--app-text-secondary);
  font-size: 11px;
  line-height: 1.5;
}

.prompt-card {
  position: relative;
  padding-left: 18px;
}

.prompt-mark {
  position: absolute;
  top: 12px;
  left: 0;
  color: var(--app-primary);
  font-family: Georgia, serif;
  font-size: 28px;
  line-height: 1;
}

.prompt-card p {
  margin: 0;
  color: var(--app-text-secondary);
  font-size: 12px;
  line-height: 1.65;
}
</style>
