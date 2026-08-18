import { createRouter, createWebHistory } from 'vue-router'
import LandingView from '@/views/LandingView.vue'
import HomeView from '@/views/HomeView.vue'
import { getTokenRole } from '@/stores/user'
import { pageTitle } from '@/config/site'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  // 普通跳转回到顶部；主页返回时的位置由 HomeView 自己保存/恢复（KeepAlive 场景更可靠）
  scrollBehavior() {
    return { top: 0 }
  },
  routes: [
    {
      path: '/',
      name: 'landing',
      component: LandingView,
      meta: { title: pageTitle('首页') },
    },
    {
      path: '/home',
      name: 'home',
      component: HomeView,
      meta: { title: pageTitle('文章') },
    },
    {
      path: '/article/:id',
      name: 'article-detail',
      component: () => import('@/views/ArticleDetailView.vue'),
      meta: { title: pageTitle('文章详情') },
    },
    {
      path: '/article/create',
      name: 'article-create',
      component: () => import('@/views/ArticleEditView.vue'),
      meta: { requiresAuth: true, title: pageTitle('发布文章') },
    },
    {
      path: '/article/:id/edit',
      name: 'article-edit',
      component: () => import('@/views/ArticleEditView.vue'),
      meta: { requiresAuth: true, title: pageTitle('编辑文章') },
    },
    {
      path: '/hot',
      name: 'hot',
      component: () => import('@/views/HotView.vue'),
      meta: { title: pageTitle('热榜') },
    },
    {
      path: '/favorites',
      name: 'favorites',
      component: () => import('@/views/MyFavoritesView.vue'),
      meta: { requiresAuth: true, title: pageTitle('收藏') },
    },
    {
      path: '/my-articles',
      name: 'my-articles',
      component: () => import('@/views/MyArticlesView.vue'),
      meta: { requiresAuth: true, title: pageTitle('我的文章') },
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/views/ProfileView.vue'),
      meta: { requiresAuth: true, title: pageTitle('个人中心') },
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('@/views/AdminView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true, title: pageTitle('后台管理') },
    },
    // 需要登录才能访问的页面，在后面加 meta: { requiresAuth: true }
    {
      path: '/register',
      name: 'register',
      // 懒加载：访问到这个路由时才加载对应组件，减小首屏体积
      component: () => import('@/views/RegisterView.vue'),
      meta: { hideNavbar: true, title: pageTitle('注册') },
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { hideNavbar: true, title: pageTitle('登录') },
    },
    // 后续按路线补充：/article/:id、/article/create、/admin 等
  ],
})

router.afterEach((to) => {
  document.title = typeof to.meta.title === 'string' ? to.meta.title : pageTitle()
})

// 全局路由守卫：每次跳转前先检查登录状态
router.beforeEach((to) => {
  const token = localStorage.getItem('community_token')

  // 想去需要登录的页面但没有 token → 跳到登录页，并记住目标地址
  if (to.meta.requiresAuth && !token) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  // 后台管理：需要管理员角色（解码 JWT 判断，仅用于前端入口；真正的权限由后端拦截）
  if (to.meta.requiresAdmin && getTokenRole(token || '') !== 'ADMIN') {
    ElMessage.warning('没有权限访问后台')
    return { name: 'landing' }
  }

  // 已经登录还去登录/注册页 → 直接回首页
  if ((to.name === 'login' || to.name === 'register') && token) {
    return { name: 'landing' }
  }
})

export default router
