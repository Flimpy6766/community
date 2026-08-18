<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { createArticle, getArticleDetail, updateArticle } from '@/api/article'
import { useThemeStore } from '@/stores/theme'

const route = useRoute()
const router = useRouter()
const themeStore = useThemeStore()

// 路由有 :id 就是编辑，没有就是新建（/article/create）
const articleId = route.params.id as string | undefined
const isEdit = Boolean(articleId)

const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)

const form = reactive({
  title: '',
  summary: '',
  cover: '',
  content: '',
  tags: [] as string[],
})

const contentLength = computed(() => form.content.replace(/\s/g, '').length)

const rules: FormRules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { max: 100, message: '标题最长 100 字', trigger: 'blur' },
  ],
}

// 编辑器主题跟随网站深浅模式
const editorTheme = computed(() => {
  if (themeStore.colorMode === 'dark') return 'dark'
  if (themeStore.colorMode === 'system') {
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
  }
  return 'light'
})

// 编辑模式：拉取原文章填充表单
async function fetchArticle() {
  if (!isEdit) return
  loading.value = true
  try {
    const article = await getArticleDetail(articleId!)
    form.title = article.title
    form.summary = article.summary || ''
    form.cover = article.cover || ''
    form.content = article.content || ''
    form.tags = article.tags || []
  } catch {
    router.push('/home')
  } finally {
    loading.value = false
  }
}

// status: 0 存草稿，1 发布
async function handleSave(status: number) {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    const params = {
      title: form.title,
      summary: form.summary,
      cover: form.cover || undefined,
      content: form.content,
      tags: form.tags,
      status,
    }
    if (isEdit) {
      await updateArticle(articleId!, params)
      ElMessage.success(status === 1 ? '已发布' : '已保存草稿')
      router.push(`/article/${articleId}`)
    } else {
      const id = await createArticle(params)
      ElMessage.success(status === 1 ? '发布成功' : '草稿已保存')
      router.push(`/article/${id}`)
    }
  } catch {
    // 错误提示已在拦截器统一处理
  } finally {
    saving.value = false
  }
}

onMounted(fetchArticle)
</script>

<template>
  <div
    v-loading="loading"
    class="edit-page"
  >
    <div class="edit-shell">
      <header class="edit-topbar">
        <div class="edit-heading">
          <el-button
            text
            @click="router.back()"
          >
            返回
          </el-button>
          <div>
            <p class="edit-kicker">EDITORIAL WORKSPACE</p>
            <h1 class="edit-title">
              {{ isEdit ? '编辑文章' : '发布文章' }}
            </h1>
          </div>
        </div>
        <span class="edit-status">
          {{ contentLength ? `${contentLength.toLocaleString()} 字` : '准备开始写作' }}
        </span>
        <div class="edit-actions">
          <el-button
            :loading="saving"
            @click="handleSave(0)"
          >
            保存草稿
          </el-button>
          <el-button
            type="primary"
            :loading="saving"
            @click="handleSave(1)"
          >
            发布文章
          </el-button>
        </div>
      </header>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
      >
        <div class="edit-workspace">
          <section class="edit-main">
            <el-form-item
              class="title-field"
              prop="title"
            >
              <el-input
                v-model="form.title"
                class="title-input"
                maxlength="100"
                show-word-limit
                placeholder="输入文章标题"
              />
            </el-form-item>
            <el-form-item
              class="content-field"
              label="正文"
            >
              <MdEditor
                v-model="form.content"
                class="edit-editor"
                :theme="editorTheme"
              />
            </el-form-item>
          </section>

          <aside class="edit-sidebar">
            <div class="sidebar-heading">
              <span>文章设置</span>
              <span class="sidebar-hint">发布前完善信息</span>
            </div>
            <el-form-item label="摘要">
              <el-input
                v-model="form.summary"
                type="textarea"
                :rows="4"
                maxlength="255"
                show-word-limit
                placeholder="一句话简介，列表页会展示"
              />
            </el-form-item>
            <el-form-item label="标签">
              <el-select
                v-model="form.tags"
                multiple
                filterable
                allow-create
                default-first-option
                placeholder="输入后回车创建"
              >
                <el-option
                  v-for="tag in form.tags"
                  :key="tag"
                  :label="tag"
                  :value="tag"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="封面图 URL（选填）">
              <el-input
                v-model="form.cover"
                placeholder="https://example.com/cover.png"
              />
            </el-form-item>
            <p class="sidebar-note">
              Markdown 支持标题、代码块、图片、链接和列表。发布后可以继续编辑。
            </p>
          </aside>
        </div>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.edit-page {
  width: min(1400px, calc(100% - 48px));
  margin: 0 auto;
  padding: 30px 0 72px;
}

.edit-shell {
  overflow: visible;
}

.edit-topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 0 8px 26px;
}

.edit-heading {
  display: flex;
  align-items: center;
  gap: 14px;
}

.edit-kicker {
  margin: 0 0 4px;
  color: var(--app-primary-strong);
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.14em;
}

.edit-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--app-text);
}

.edit-status {
  margin-left: auto;
  color: var(--app-text-secondary);
  font-size: 12px;
}

.edit-editor {
  width: 100%;
  height: 620px;
}

.edit-actions {
  display: flex;
  gap: 12px;
}

.edit-workspace {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 340px;
  align-items: start;
  overflow: hidden;
  border: 1px solid var(--app-border);
  border-radius: var(--app-radius-lg);
  background: color-mix(in srgb, var(--app-surface-solid) 92%, transparent);
  box-shadow: var(--app-shadow-soft);
}

.edit-main {
  min-width: 0;
  padding: 42px 52px 48px;
}

.edit-sidebar {
  min-height: 690px;
  padding: 38px 28px;
  border-left: 1px solid var(--app-border);
  background: color-mix(in srgb, var(--app-muted) 52%, transparent);
}

.title-field {
  margin-bottom: 24px;
}

.title-input :deep(.el-input__wrapper) {
  padding: 0;
  border-radius: 0;
  background: transparent;
  box-shadow: none;
}

.title-input :deep(.el-input__inner) {
  height: 68px;
  color: var(--app-text);
  font-size: clamp(26px, 4vw, 38px);
  font-weight: 750;
  letter-spacing: -0.04em;
}

.edit-editor :deep(.md-editor) {
  border-color: var(--app-border);
  border-radius: var(--app-radius-md);
  background: transparent;
}

.edit-editor :deep(.md-editor-toolbar-wrapper) {
  background: color-mix(in srgb, var(--app-muted) 60%, transparent);
}

.edit-editor :deep(.md-editor-content) {
  background: color-mix(in srgb, var(--app-surface-solid) 58%, transparent);
}

.content-field {
  margin-bottom: 0;
}

.sidebar-heading {
  display: flex;
  flex-direction: column;
  gap: 5px;
  margin-bottom: 24px;
  color: var(--app-text);
  font-size: 16px;
  font-weight: 700;
}

.sidebar-hint {
  color: var(--app-text-secondary);
  font-size: 12px;
  font-weight: 400;
}

.sidebar-note {
  margin: 24px 0 0;
  color: var(--app-text-secondary);
  font-size: 12px;
  line-height: 1.8;
}

@media (max-width: 900px) {
  .edit-page {
    width: min(100% - 28px, 720px);
    padding-top: 12px;
  }

  .edit-workspace {
    display: block;
  }

  .edit-sidebar {
    min-height: 0;
    border-top: 1px solid var(--app-border);
    border-left: 0;
  }

  .edit-status {
    display: none;
  }
}

@media (max-width: 600px) {
  .edit-topbar {
    align-items: flex-start;
    flex-direction: column;
    padding: 8px 0 18px;
  }

  .edit-topbar .edit-actions {
    width: 100%;
  }

  .edit-topbar .edit-actions :deep(.el-button) {
    flex: 1;
  }

  .edit-main,
  .edit-sidebar {
    padding: 22px 18px;
  }

  .edit-editor {
    height: 500px;
  }
}
</style>
