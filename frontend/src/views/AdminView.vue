<script setup lang="ts">
import { onMounted, ref } from 'vue'
import {
  IconArticleFilled,
  IconMessageCircleFilled,
  IconTagFilled,
  IconTrashFilled,
  IconUserFilled,
} from '@tabler/icons-vue'
import {
  deleteAdminArticle,
  getAdminArticles,
  getAdminOverview,
  type AdminArticleVO,
  type AdminOverview,
} from '@/api/admin'

const overview = ref<AdminOverview | null>(null)
const loading = ref(false)
const articles = ref<AdminArticleVO[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = 10

async function fetchOverview() {
  try {
    overview.value = await getAdminOverview()
  } catch {
    // 错误提示已在拦截器统一处理
  }
}

async function fetchArticles() {
  loading.value = true
  try {
    const data = await getAdminArticles(currentPage.value, pageSize)
    articles.value = data.records
    total.value = data.total
  } catch {
    // 错误提示已在拦截器统一处理
  } finally {
    loading.value = false
  }
}

async function handleDelete(row: AdminArticleVO) {
  const confirmed = await ElMessageBox.confirm(
    `确定删除文章《${row.title}》吗？删除后不可恢复`,
    '删除确认',
    { type: 'warning' },
  ).catch(() => false)
  if (!confirmed) return
  try {
    await deleteAdminArticle(row.id)
    ElMessage.success('已删除')
    fetchOverview()
    fetchArticles()
  } catch {
    // 错误提示已在拦截器统一处理
  }
}

function formatTime(time: string) {
  return time.replace('T', ' ').slice(0, 16)
}

onMounted(() => {
  fetchOverview()
  fetchArticles()
})
</script>

<template>
  <div class="admin-page">
    <h1 class="page-title">
      后台管理
    </h1>

    <!-- 统计概览 -->
    <div
      v-if="overview"
      class="admin-stats"
    >
      <div class="stat-card">
        <IconArticleFilled :size="22" />
        <span class="stat-num">{{ overview.articleCount }}</span>
        <span class="stat-label">文章总数</span>
      </div>
      <div class="stat-card">
        <IconUserFilled :size="22" />
        <span class="stat-num">{{ overview.userCount }}</span>
        <span class="stat-label">用户总数</span>
      </div>
      <div class="stat-card">
        <IconMessageCircleFilled :size="22" />
        <span class="stat-num">{{ overview.commentCount }}</span>
        <span class="stat-label">评论总数</span>
      </div>
      <div class="stat-card">
        <IconTagFilled :size="22" />
        <span class="stat-num">{{ overview.tagCount }}</span>
        <span class="stat-label">标签总数</span>
      </div>
    </div>

    <!-- 文章管理 -->
    <el-card class="admin-articles-card">
      <h2 class="card-title">
        文章管理
      </h2>
      <el-table
        v-loading="loading"
        :data="articles"
        stripe
      >
        <el-table-column
          prop="id"
          label="ID"
          width="70"
        />
        <el-table-column
          prop="title"
          label="标题"
          min-width="200"
          show-overflow-tooltip
        />
        <el-table-column
          prop="authorName"
          label="作者"
          width="120"
        />
        <el-table-column
          label="状态"
          width="90"
        >
          <template #default="{ row }">
            <el-tag
              :type="row.status === 1 ? 'success' : 'info'"
              size="small"
            >
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="viewCount"
          label="浏览"
          width="80"
        />
        <el-table-column
          prop="likeCount"
          label="点赞"
          width="80"
        />
        <el-table-column
          prop="commentCount"
          label="评论"
          width="80"
        />
        <el-table-column
          label="创建时间"
          width="150"
        >
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column
          label="操作"
          width="90"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              type="danger"
              link
              @click="handleDelete(row)"
            >
              <IconTrashFilled :size="15" />
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-if="total > pageSize"
        v-model:current-page="currentPage"
        class="admin-pagination"
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        @current-change="fetchArticles"
      />
    </el-card>
  </div>
</template>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-title {
  margin: 0;
  font-size: calc(var(--app-font-size) + 8px);
  color: var(--app-text);
}

.admin-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px 12px;
  background: var(--app-surface-solid);
  border: 1px solid var(--app-border);
  border-radius: 10px;
  color: var(--app-primary);
}

.stat-num {
  font-size: calc(var(--app-font-size) + 10px);
  font-weight: 700;
  color: var(--app-text);
}

.stat-label {
  font-size: calc(var(--app-font-size) - 3px);
  color: var(--app-text-secondary);
}

.admin-articles-card {
  border-radius: 12px;
}

.card-title {
  margin: 0 0 12px;
  font-size: calc(var(--app-font-size) + 4px);
  color: var(--app-text);
}

.admin-pagination {
  justify-content: center;
  margin-top: 12px;
}
</style>
