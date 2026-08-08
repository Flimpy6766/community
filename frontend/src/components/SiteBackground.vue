<template>
  <!-- 整站背景：SVG 颜色全部由主题 CSS 变量控制，深浅/主题色切换时自动跟随 -->
  <div
    class="site-bg"
    aria-hidden="true"
  >
    <svg
      xmlns="http://www.w3.org/2000/svg"
      viewBox="0 0 2560 1600"
      preserveAspectRatio="xMidYMid slice"
    >
      <defs>
        <linearGradient
          id="bgGradient"
          x1="0"
          y1="0"
          x2="1"
          y2="1"
        >
          <stop
            offset="0%"
            stop-color="var(--app-bg-start)"
          />
          <stop
            offset="100%"
            stop-color="var(--app-bg-end)"
          />
        </linearGradient>
      </defs>

      <!-- 渐变底色 -->
      <rect
        width="2560"
        height="1600"
        fill="url(#bgGradient)"
      />

      <!-- 柔和的装饰形状 -->
      <circle
        cx="380"
        cy="280"
        r="430"
        fill="var(--app-bg-shape-1)"
      />
      <circle
        cx="2240"
        cy="1350"
        r="540"
        fill="var(--app-bg-shape-2)"
      />
      <circle
        cx="1980"
        cy="220"
        r="240"
        fill="var(--app-bg-shape-3)"
      />
      <path
        d="M0,1350 C 360,1180 760,1480 1180,1360 C 1600,1240 2000,1420 2560,1280 L2560,1600 L0,1600 Z"
        fill="var(--app-bg-shape-4)"
      />
    </svg>
  </div>
</template>

<style scoped>
.site-bg {
  position: fixed;
  inset: 0;
  z-index: -1;
  pointer-events: none;
}

.site-bg svg {
  width: 100%;
  height: 100%;
  display: block;
}
</style>
