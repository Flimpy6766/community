# Community 前端

内容社区前端，Vue 3 + TypeScript + Vite，配套 Element Plus、Pinia、Vue Router。

## 开发

```bash
pnpm install      # 安装依赖
pnpm dev          # 启动开发服务器 (http://localhost:5173)
pnpm build        # 类型检查 + 打包
pnpm lint         # ESLint 检查
pnpm format       # Prettier 格式化
```

开发服务器已配置代理：`/api` 请求转发到 `http://localhost:8080`（后端 Spring Boot）。
