import { ref, watch } from 'vue'
import { defineStore } from 'pinia'

export type ThemeColor = 'blue' | 'red' | 'green'
export type ColorMode = 'light' | 'dark' | 'system'

const THEME_COLOR_KEY = 'community_theme_color'
const COLOR_MODE_KEY = 'community_color_mode'
const FONT_SIZE_KEY = 'community_font_size'

const DARK_QUERY = window.matchMedia('(prefers-color-scheme: dark)')

// 把当前设置写入 html：data-theme 换肤 + 内联 CSS 变量控制全站字号
function applyTheme(themeColor: ThemeColor, colorMode: ColorMode, fontSize: number) {
  const dark = colorMode === 'dark' || (colorMode === 'system' && DARK_QUERY.matches)
  document.documentElement.dataset.theme = `${themeColor}-${dark ? 'dark' : 'light'}`
  document.documentElement.style.setProperty('--app-font-size', `${fontSize}px`)

  // Element Plus 字号变量联动，滑块可整体缩放组件字体
  document.documentElement.style.setProperty('--el-font-size-extra-large', `${fontSize + 4}px`)
  document.documentElement.style.setProperty('--el-font-size-large', `${fontSize + 2}px`)
  document.documentElement.style.setProperty('--el-font-size-medium', `${fontSize + 1}px`)
  document.documentElement.style.setProperty('--el-font-size-base', `${fontSize}px`)
  document.documentElement.style.setProperty('--el-font-size-small', `${fontSize - 1}px`)
  document.documentElement.style.setProperty('--el-font-size-extra-small', `${fontSize - 2}px`)
}

// 网站样式：主题颜色（蓝/红/绿）+ 颜色模式（浅色/深色/跟随系统）+ 全站字体大小
export const useThemeStore = defineStore('theme', () => {
  const themeColor = ref<ThemeColor>(
    (localStorage.getItem(THEME_COLOR_KEY) as ThemeColor) || 'blue',
  )
  const colorMode = ref<ColorMode>(
    (localStorage.getItem(COLOR_MODE_KEY) as ColorMode) || 'light',
  )
  const fontSize = ref(Number(localStorage.getItem(FONT_SIZE_KEY)) || 16)

  // 跟随系统时，监听系统深浅变化自动切换
  DARK_QUERY.addEventListener('change', () => {
    if (colorMode.value === 'system') {
      applyTheme(themeColor.value, colorMode.value, fontSize.value)
    }
  })

  // 选中后立即生效，并记住用户选择
  watch([themeColor, colorMode, fontSize], () => {
    applyTheme(themeColor.value, colorMode.value, fontSize.value)
    localStorage.setItem(THEME_COLOR_KEY, themeColor.value)
    localStorage.setItem(COLOR_MODE_KEY, colorMode.value)
    localStorage.setItem(FONT_SIZE_KEY, String(fontSize.value))
  })
  applyTheme(themeColor.value, colorMode.value, fontSize.value)

  return { themeColor, colorMode, fontSize }
})
