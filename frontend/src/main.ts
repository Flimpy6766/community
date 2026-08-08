import { createApp } from 'vue'
import { createPinia } from 'pinia'
// Element Plus 的 CSS 变量基础样式（按需引入后每个组件样式自动注入，这里只补变量）
import 'element-plus/theme-chalk/base.css'
// 主题变量：颜色 / 字体 / 图标颜色统一在此管理，切换 html[data-theme] 换肤
import './theme.css'
import './style.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
