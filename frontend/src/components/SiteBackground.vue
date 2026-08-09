<template>
  <!-- 整站背景：云朵装饰。云朵用不透明色 + 整朵 opacity，内部元素叠加不会变深 -->
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
            style="stop-color: var(--app-bg-start)"
          />
          <stop
            offset="100%"
            style="stop-color: var(--app-bg-end)"
          />
        </linearGradient>

        <!-- 云朵模板：底部扁圆 + 上方几团圆（fill 统一为不透明云朵色） -->
        <g id="cloud">
          <ellipse
            cx="50"
            cy="48"
            rx="36"
            ry="24"
          />
          <circle
            cx="28"
            cy="38"
            r="18"
          />
          <circle
            cx="52"
            cy="26"
            r="26"
          />
          <circle
            cx="78"
            cy="36"
            r="21"
          />
          <circle
            cx="100"
            cy="46"
            r="14"
          />
        </g>
      </defs>

      <!-- 渐变底色 -->
      <rect
        width="2560"
        height="1600"
        style="fill: url(#bgGradient)"
      />

      <!-- 大小相间、深浅错落的云朵：首屏（hero 按钮下方）和中下部都有分布 -->
      <g
        transform="translate(120, 350) scale(2.5)"
        opacity="0.12"
        style="fill: var(--app-bg-cloud)"
      >
        <use href="#cloud" />
      </g>
      <g
        transform="translate(760, 220) scale(1.4)"
        opacity="0.16"
        style="fill: var(--app-bg-cloud)"
      >
        <use href="#cloud" />
      </g>
      <g
        transform="translate(1050, 560) scale(2.0)"
        opacity="0.14"
        style="fill: var(--app-bg-cloud)"
      >
        <use href="#cloud" />
      </g>
      <g
        transform="translate(1600, 300) scale(1.3)"
        opacity="0.2"
        style="fill: var(--app-bg-cloud)"
      >
        <use href="#cloud" />
      </g>
      <g
        transform="translate(1990, 560) scale(2.7)"
        opacity="0.1"
        style="fill: var(--app-bg-cloud)"
      >
        <use href="#cloud" />
      </g>
      <g
        transform="translate(2340, 880) scale(1.4)"
        opacity="0.15"
        style="fill: var(--app-bg-cloud)"
      >
        <use href="#cloud" />
      </g>
      <!-- hero 按钮下方区域 -->
      <g
        transform="translate(560, 760) scale(1.6)"
        opacity="0.14"
        style="fill: var(--app-bg-cloud)"
      >
        <use href="#cloud" />
      </g>
      <g
        transform="translate(1350, 800) scale(2.2)"
        opacity="0.1"
        style="fill: var(--app-bg-cloud)"
      >
        <use href="#cloud" />
      </g>
      <!-- 滚动后可见的中下部 -->
      <g
        transform="translate(300, 1150) scale(1.8)"
        opacity="0.14"
        style="fill: var(--app-bg-cloud)"
      >
        <use href="#cloud" />
      </g>
      <g
        transform="translate(1150, 1250) scale(2.9)"
        opacity="0.09"
        style="fill: var(--app-bg-cloud)"
      >
        <use href="#cloud" />
      </g>
      <g
        transform="translate(1950, 1350) scale(2.0)"
        opacity="0.12"
        style="fill: var(--app-bg-cloud)"
      >
        <use href="#cloud" />
      </g>
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
