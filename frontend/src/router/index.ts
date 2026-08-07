import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '@/views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    // 需要登录才能访问的页面，在后面加 meta: { requiresAuth: true }
    {
      path: '/register',
      name: 'register',
      // 懒加载：访问到这个路由时才加载对应组件，减小首屏体积
      component: () => import('@/views/RegisterView.vue'),
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
    },
    // 后续按路线补充：/article/:id、/article/create、/admin 等
  ],
})

// 全局路由守卫：每次跳转前先检查登录状态
router.beforeEach((to) => {
  const token = localStorage.getItem('community_token')

  // 想去需要登录的页面但没有 token → 跳到登录页，并记住目标地址
  if (to.meta.requiresAuth && !token) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }

  // 已经登录还去登录/注册页 → 直接回首页
  if ((to.name === 'login' || to.name === 'register') && token) {
    return { name: 'home' }
  }
})

export default router
