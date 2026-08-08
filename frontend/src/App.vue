<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import {
  IconEdit,
  IconEditFilled,
  IconFlame,
  IconFlameFilled,
  IconHome,
  IconHomeFilled,
  IconLayout,
  IconLayoutFilled,
  IconLogout2,
  IconSettings,
  IconSettingsFilled,
  IconStar,
  IconStarFilled,
  IconUser,
  IconUserFilled,
} from '@tabler/icons-vue'
import { useThemeStore } from '@/stores/theme'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const themeStore = useThemeStore()

// 图标风格：线性 / 实心（无 filled 变体的图标保持线性）
const layoutIcon = computed(() =>
  themeStore.iconStyle === 'filled' ? IconLayoutFilled : IconLayout,
)
const homeIcon = computed(() =>
  themeStore.iconStyle === 'filled' ? IconHomeFilled : IconHome,
)
const flameIcon = computed(() =>
  themeStore.iconStyle === 'filled' ? IconFlameFilled : IconFlame,
)
const starIcon = computed(() =>
  themeStore.iconStyle === 'filled' ? IconStarFilled : IconStar,
)
const editIcon = computed(() =>
  themeStore.iconStyle === 'filled' ? IconEditFilled : IconEdit,
)
const settingsIcon = computed(() =>
  themeStore.iconStyle === 'filled' ? IconSettingsFilled : IconSettings,
)
const userIcon = computed(() =>
  themeStore.iconStyle === 'filled' ? IconUserFilled : IconUser,
)

function handleLogout() {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}

// 用户下拉菜单命令
function handleUserCommand(command: string | number | object) {
  if (command === 'logout') {
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
            <component :is="layoutIcon" />
            <span>Community</span>
          </router-link>
          <span class="nav-divider" />

          <!-- 左盒子：主页 | 热榜 | 我的收藏 -->
          <nav class="navbar-box">
            <router-link
              class="nav-item"
              to="/"
            >
              <component :is="homeIcon" />
              <span>主页</span>
            </router-link>
            <router-link
              class="nav-item"
              to="/hot"
            >
              <component :is="flameIcon" />
              <span>热榜</span>
            </router-link>
            <router-link
              v-if="userStore.userInfo"
              class="nav-item"
              to="/favorites"
            >
              <component :is="starIcon" />
              <span>我的收藏</span>
            </router-link>
          </nav>

          <!-- 右侧：盒子（发布文章 网站样式）| 用户 -->
          <div class="navbar-right">
            <div class="navbar-box">
              <router-link
                v-if="userStore.userInfo"
                class="nav-item"
                to="/article/create"
              >
                <component :is="editIcon" />
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
                    <component :is="settingsIcon" />
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

                  <!-- 4. 图标风格：线性 / 实心 -->
                  <div class="style-group">
                    <div class="style-label">
                      <IconLayoutFilled :size="14" />
                      图标风格
                    </div>
                    <el-radio-group
                      v-model="themeStore.iconStyle"
                      size="small"
                    >
                      <el-radio-button value="outline">
                        线性
                      </el-radio-button>
                      <el-radio-button value="filled">
                        实心
                      </el-radio-button>
                    </el-radio-group>
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
                <component :is="userIcon" />
                <span>{{ userStore.userInfo.nickname }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="logout">
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
              <component :is="userIcon" />
              <span>登录</span>
            </router-link>
          </div>
        </div>
      </header>
      <main class="app-main">
        <router-view />
      </main>
    </div>
  </el-config-provider>
</template>

<style scoped>
.app {
  min-height: 100vh;
}

/* 导航栏：铺满全宽，左右不预留内边距，半透明毛玻璃让整站背景透出来 */
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  background: var(--app-surface);
  border-bottom: 1px solid var(--app-border);
  -webkit-backdrop-filter: blur(10px);
  backdrop-filter: blur(10px);
}

.navbar-inner {
  width: 100%;
  padding: 0;
  height: 60px;
  display: flex;
  align-items: center;
}

.navbar-logo {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  font-size: calc(var(--app-font-size) + 6px);
  font-weight: 700;
  color: var(--app-text);
  white-space: nowrap;
}

/* 竖线：只放在 logo 和用户两侧，上下各加长 5px（高 34px），不设 margin */
.nav-divider {
  width: 1px;
  height: 34px;
  background: var(--app-divider);
}

/* 导航项盒子：主页/热榜/我的收藏，以及右侧的 发布文章/网站样式 */
.navbar-box {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 10px;
  background: var(--app-surface-soft);
  border-radius: 10px;
}

.nav-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  font-size: calc(var(--app-font-size) + 2px);
  color: var(--app-text);
  border-radius: 8px;
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
  width: 1.15em;
  height: 1.15em;
  flex-shrink: 0;
}

.navbar-box a.router-link-active {
  color: var(--app-primary);
  font-weight: 600;
}

.navbar-right {
  display: flex;
  align-items: center;
  margin-left: auto;
}

.app-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 16px;
}

.app--with-nav .app-main {
  padding-top: 60px;
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
