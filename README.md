# 鸡舍官方社区（Community）

前后端分离的内容社区项目：支持文章发布（Markdown）、标签、评论回复、点赞、收藏、热榜、搜索、浏览量统计，以及多主题换肤。

- 后端：Spring Boot 3 + MyBatis-Plus + Spring Security（JWT）+ Redis
- 前端：Vue 3 + TypeScript + Vite + Element Plus + Pinia + Vue Router + Axios

## 功能特性

- 认证：注册 / 登录，JWT + Redis 登录态（有效期 7 天，退出即失效）
- 文章：发布 / 编辑 / 删除 / 存草稿，Markdown 编辑与预览，标签自动创建
- 互动：点赞、收藏、评论与回复（均支持取消 / 删除，点赞收藏乐观更新）
- 内容：最新列表、标题 / 正文搜索、热榜（浏览 / 点赞 / 收藏 / 评论加权）、浏览量统计（Redis 计数 + 定时落库）
- 个人中心：资料与统计、修改昵称 / 简介 / 头像、我的文章（草稿箱 / 已发布）、我的收藏
- 后台管理：统计面板、文章管理（管理员角色）
- 体验：整站换肤（蓝 / 红 / 绿 × 浅色 / 深色）、全站字号调节（14-18px）、云朵 SVG 背景、返回列表自动恢复滚动位置

## 技术栈

| 端 | 技术 |
| --- | --- |
| 后端 | Java 21 · Spring Boot 3 · MyBatis-Plus · Spring Security · JWT · Redis · MySQL |
| 前端 | Vue 3 · TypeScript · Vite · Element Plus · Pinia · Vue Router · Axios · md-editor-v3 · Tabler Icons |

## 目录结构

```
community/
├── src/main/java/com/community/   # 后端
│   ├── controller/                # 接口：文章 / 用户 / 后台
│   ├── service/                   # 业务逻辑（点赞计数、热榜、缓存）
│   ├── mapper/ entity/ dto/       # 数据访问 / 实体 / 出入参
│   ├── config/                    # Security、JWT 过滤器、MyBatis-Plus 配置
│   ├── task/                      # 浏览量定时落库、热榜初始化
│   └── util/                      # JWT、当前用户工具
├── frontend/                      # 前端
│   ├── src/api/                   # 接口封装（auth / article / user / admin）
│   ├── src/stores/                # Pinia 状态（用户、主题）
│   ├── src/views/                 # 页面（主页、详情、发布、个人中心、后台等）
│   ├── src/components/            # 通用组件（文章卡片、云朵背景）
│   ├── src/theme.css              # 主题变量（6 套换肤）
│   └── README.md                  # 前端开发命令
├── pom.xml
└── README.md
```

## 快速开始

环境要求：JDK 21、Maven 3.9+、MySQL 8、Redis、Node 18+、pnpm。

### 1. 准备数据库与 Redis

```sql
CREATE DATABASE IF NOT EXISTS community DEFAULT CHARACTER SET utf8mb4;
```

表结构以 `src/main/java/com/community/entity/` 下的实体为准（`user` / `article` / `tag` / `article_tag` / `comment` / `user_like` / `favorite`）。Redis 默认连接 `127.0.0.1:6379`。

### 2. 启动后端

```bash
# 按需修改 src/main/resources/application.yml 中的数据库账号密码
mvn spring-boot:run
```

后端端口 `8080`，接口统一在 `/api` 路径下（如 `POST /api/user/login`）。

### 3. 启动前端

```bash
cd frontend
pnpm install
pnpm dev
```

访问 `http://localhost:5173`，开发服务器会把 `/api` 请求代理到 `http://localhost:8080`。

### 4. 管理员账号

注册的用户默认是普通角色。想启用后台管理，把某个用户改为管理员后重新登录：

```sql
UPDATE user SET role = 'ADMIN' WHERE id = 你的id;
```

## 接口约定

- 统一返回体：`{ "code": 0, "message": "ok", "data": ... }`，`code = 0` 表示成功
- 登录后请求头带 `Authorization: Bearer <token>`
- 未登录返回 401，权限不足返回 403；前端 axios 拦截器统一处理并跳转登录页

## 部署（可选）

```bash
cd frontend && pnpm build     # 产物在 frontend/dist
mvn package                   # 后端打包 jar
```
