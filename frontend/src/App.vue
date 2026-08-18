<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import {
  IconEditFilled,
  IconFileTextFilled,
  IconDashboardFilled,
  IconFlameFilled,
  IconHomeFilled,
  IconLayoutFilled,
  IconLogout2,
  IconSearch,
  IconSettingsFilled,
  IconStarFilled,
  IconUserFilled,
} from '@tabler/icons-vue'
import { useThemeStore } from '@/stores/theme'
import { useUserStore } from '@/stores/user'
import { siteConfig } from '@/config/site'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

// 导航栏搜索关键字
const searchKeyword = ref('')

// 提交搜索：带 keyword 跳主页（主页根据 ?keyword= 展示搜索结果）
function handleSearch() {
  const kw = searchKeyword.value.trim()
  router.push({ path: '/home', query: kw ? { keyword: kw } : {} })
}

// 地址栏变化时同步输入框（比如从搜索结果页切走再切回）
watch(
  () => route.query.keyword,
  (val) => {
    searchKeyword.value = typeof val === 'string' ? val : ''
  },
)

function handleLogout() {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}

// 用户下拉菜单命令
function handleUserCommand(command: string | number | object) {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'admin') {
    router.push('/admin')
  } else if (command === 'my-articles') {
    router.push('/my-articles')
  } else if (command === 'logout') {
    handleLogout()
  }
}
</script>

<template>
  <el-config-provider :locale="zhCn">
    <div
      class="app"
      :class="{ 'app--with-nav': !route.meta.hideNavbar }"
    >
      <!-- 整站背景：SVG 颜色跟随主题，登录/注册页同样生效 -->
      <SiteBackground />
      <!-- 登录/注册页通过 meta.hideNavbar 隐藏导航栏 -->
      <header
        v-if="!route.meta.hideNavbar"
        class="navbar"
      >
        <div class="navbar-inner">
          <!-- 最左：logo（layout 图标） -->
          <router-link
            class="navbar-logo"
            to="/"
          >
            <IconLayoutFilled />
            <span>{{ siteConfig.brand }}</span>
          </router-link>
          <span class="nav-divider" />

          <!-- 左盒子：文章 | 热榜 | 收藏 -->
          <nav class="navbar-box">
            <router-link
              class="nav-item"
              to="/home"
            >
              <IconHomeFilled />
              <span>文章</span>
            </router-link>
            <router-link
              class="nav-item"
              to="/hot"
            >
              <IconFlameFilled />
              <span>热榜</span>
            </router-link>
            <router-link
              class="nav-item"
              to="/favorites"
            >
              <IconStarFilled />
              <span>收藏</span>
            </router-link>
          </nav>

          <!-- 导航栏搜索：回车跳主页展示搜索结果 -->
          <el-input
            v-model="searchKeyword"
            class="navbar-search"
            placeholder="搜索文章"
            clearable
            @keyup.enter="handleSearch"
            @clear="handleSearch"
          >
            <template #prefix>
              <IconSearch :size="16" />
            </template>
          </el-input>

          <!-- 右侧：盒子（发布文章 网站样式）| 用户 -->
          <div class="navbar-right">
            <div class="navbar-box">
              <router-link
                v-if="userStore.userInfo"
                class="nav-item"
                to="/article/create"
              >
                <IconEditFilled />
                <span>发布文章</span>
              </router-link>
              <!-- 网站样式：齿轮图标弹出面板 -->
              <el-popover
                placement="bottom-end"
                :width="300"
                trigger="click"
                popper-class="style-popover"
              >
                <template #reference>
                  <span class="nav-item nav-icon-only">
                    <IconSettingsFilled />
                  </span>
                </template>
                <div class="style-panel">
                  <!-- 1. 颜色模式：浅色 / 深色 / 跟随系统 -->
                  <div class="style-group">
                    <div class="style-label">
                      颜色模式
                    </div>
                    <el-radio-group
                      v-model="themeStore.colorMode"
                      size="small"
                    >
                      <el-radio-button value="light">
                        浅色
                      </el-radio-button>
                      <el-radio-button value="dark">
                        深色
                      </el-radio-button>
                      <el-radio-button value="system">
                        跟随系统
                      </el-radio-button>
                    </el-radio-group>
                  </div>

                  <!-- 2. 主题颜色：蓝 / 红 / 绿 -->
                  <div class="style-group">
                    <div class="style-label">
                      主题颜色
                    </div>
                    <el-radio-group
                      v-model="themeStore.themeColor"
                      size="small"
                    >
                      <el-radio-button value="blue">
                        蓝色
                      </el-radio-button>
                      <el-radio-button value="red">
                        红色
                      </el-radio-button>
                      <el-radio-button value="green">
                        绿色
                      </el-radio-button>
                    </el-radio-group>
                  </div>

                  <!-- 3. 字体大小：14px - 18px 调整整个网站 -->
                  <div class="style-group">
                    <div class="style-label">
                      字体大小
                      <span class="style-value">{{ themeStore.fontSize }}px</span>
                    </div>
                    <el-slider
                      v-model="themeStore.fontSize"
                      :min="14"
                      :max="18"
                      :step="1"
                    />
                  </div>
                </div>
              </el-popover>
            </div>
            <span class="nav-divider" />
            <el-dropdown
              v-if="userStore.userInfo"
              @command="handleUserCommand"
            >
              <span class="nav-item nav-user">
                <IconUserFilled />
                <span>{{ userStore.userInfo.nickname }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <IconUserFilled :size="15" />
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="my-articles">
                    <IconFileTextFilled :size="15" />
                    我的文章
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="userStore.isAdmin"
                    command="admin"
                  >
                    <IconDashboardFilled :size="15" />
                    后台管理
                  </el-dropdown-item>
                  <el-dropdown-item
                    command="logout"
                    divided
                  >
                    <IconLogout2 :size="15" />
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <router-link
              v-else
              class="nav-item nav-user"
              to="/login"
            >
              <IconUserFilled />
              <span>登录</span>
            </router-link>
          </div>
        </div>
      </header>
      <main
        class="app-main"
        :class="{
          'app-main--full': route.name === 'landing',
          'app-main--wide': route.name === 'home',
        }"
      >
        <!-- KeepAlive 只缓存主页：从详情页返回时列表数据和滚动位置都在 -->
        <KeepAlive include="HomeView">
          <router-view />
        </KeepAlive>
      </main>
    </div>
  </el-config-provider>
</template>

<style scoped>
.app {
  min-height: 100vh;
}

/* 导航栏：轻量固定层，内容不使用厚重卡片和分隔盒子 */
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: color-mix(in srgb, var(--app-surface-solid) 86%, transparent);
  border-bottom: 1px solid var(--app-border);
  box-shadow: 0 1px 12px rgba(24, 34, 48, 0.035);
  -webkit-backdrop-filter: blur(18px) saturate(1.15);
  backdrop-filter: blur(18px) saturate(1.15);
}

.navbar-inner {
  width: 100%;
  padding: 0 20px;
  height: 64px;
  display: flex;
  align-items: center;
}

.navbar-logo {
  display: inline-flex;
  align-items: center;
  gap: 9px;
  padding: 8px 10px;
  border-radius: var(--app-radius-sm);
  font-size: calc(var(--app-font-size) + 4px);
  font-weight: 750;
  letter-spacing: -0.02em;
  color: var(--app-text);
  white-space: nowrap;
  transition: background-color 0.2s, color 0.2s;
}

.navbar-logo:hover {
  background: var(--app-primary-soft);
  color: var(--app-primary-strong);
}

/* 竖线：只放在 logo 和用户两侧，上下各加长 5px（高 34px），不设 margin */
.nav-divider {
  width: 1px;
  height: 24px;
  background: var(--app-divider);
  margin: 0 10px;
}

/* 导航栏搜索：紧凑胶囊输入框，auto 外边距让它大致居中 */
.navbar-search {
  margin-left: auto;
  margin-right: 24px;
  width: min(360px, 30vw);
  flex-shrink: 1;
}

.navbar-search :deep(.el-input__wrapper) {
  min-height: 36px;
  border: 1px solid transparent;
  border-radius: 999px;
  background: var(--app-muted);
  box-shadow: none;
  transition: background-color 0.2s;
}

.navbar-search :deep(.el-input__wrapper.is-focus) {
  background: var(--app-surface-solid);
  border-color: var(--app-border);
  box-shadow: 0 0 0 3px var(--app-primary-soft);
}

@media (max-width: 768px) {
  .navbar-search {
    width: 150px;
    margin-right: 8px;
  }
}

/* 导航项盒子：主页/热榜/我的收藏，以及右侧的 发布文章/网站样式 */
.navbar-box {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 0;
  background: transparent;
  border-radius: 10px;
}

.nav-item {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 9px 11px;
  font-size: calc(var(--app-font-size) - 1px);
  font-weight: 550;
  color: var(--app-text-secondary);
  border-radius: var(--app-radius-sm);
  cursor: pointer;
  white-space: nowrap;
  transition:
    background-color 0.2s,
    color 0.2s;
}

.nav-item:hover {
  background: var(--app-primary-soft);
  color: var(--app-primary);
}

/* 只放图标的按钮（网站样式）收紧内边距 */
.nav-icon-only {
  padding: 8px;
}

/* 图标跟随文字颜色（默认黑色，hover/激活变主题色），相对字号自动缩放 */
.nav-item svg,
.navbar-logo svg {
  width: 1.1em;
  height: 1.1em;
  flex-shrink: 0;
}

.navbar-box a.router-link-active {
  color: var(--app-primary-strong);
  font-weight: 650;
  background: var(--app-primary-soft);
}

.navbar-right {
  display: flex;
  align-items: center;
  margin-left: auto;
}

.app-main {
  max-width: var(--app-content-width);
  margin: 0 auto;
  padding: 28px 24px 64px;
}

.app--with-nav .app-main {
  padding-top: 92px;
}

/* 主页：hero 铺满全屏，内容区不限制宽度 */
.app-main--full {
  max-width: none;
  padding: 0;
}

/* 文章流使用左右功能栏，需要释放全局内容宽度限制 */
.app-main--wide {
  max-width: none;
  padding-left: 0;
  padding-right: 0;
}

.app--with-nav .app-main--full {
  padding-top: 64px;
}

@media (max-width: 900px) {
  .navbar-inner {
    padding: 0 12px;
  }

  .navbar-search {
    margin-right: 10px;
    width: min(260px, 28vw);
  }

  .nav-item {
    padding-inline: 8px;
  }
}

@media (max-width: 680px) {
  .navbar-logo span,
  .nav-item span {
    display: none;
  }

  .navbar-logo,
  .nav-item {
    padding: 9px;
  }

  .navbar-search {
    width: 150px;
  }

  .navbar-right .nav-divider {
    margin-inline: 4px;
  }

  .app-main {
    padding-inline: 14px;
  }
}
</style>

<!-- 网站样式面板（el-popover 挂载到 body，需要非 scoped 样式） -->
<style>
.style-popover .style-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.style-popover .style-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.style-popover .style-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: calc(var(--app-font-size) - 2px);
  font-weight: 600;
  color: var(--app-text);
}

.style-popover .style-value {
  margin-left: auto;
  font-weight: 400;
  color: var(--app-text-secondary);
}

.style-popover .style-label svg {
  color: var(--app-icon);
}
</style>
