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
    router.push('/')
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
    <el-card class="edit-card">
      <h1 class="edit-title">
        {{ isEdit ? '编辑文章' : '发布文章' }}
      </h1>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
      >
        <el-form-item
          label="标题"
          prop="title"
        >
          <el-input
            v-model="form.title"
            maxlength="100"
            show-word-limit
            placeholder="文章标题"
          />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="2"
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
            placeholder="输入标签名后回车创建"
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
        <el-form-item label="正文（支持 Markdown）">
          <MdEditor
            v-model="form.content"
            class="edit-editor"
            :theme="editorTheme"
          />
        </el-form-item>
        <div class="edit-actions">
          <el-button
            :loading="saving"
            @click="handleSave(0)"
          >
            存草稿
          </el-button>
          <el-button
            type="primary"
            :loading="saving"
            @click="handleSave(1)"
          >
            发布
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.edit-page {
  max-width: 900px;
  margin: 0 auto;
}

.edit-card {
  border-radius: 8px;
}

.edit-title {
  margin: 0 0 20px;
  font-size: calc(var(--app-font-size) + 6px);
  color: var(--app-text);
}

.edit-editor {
  width: 100%;
  height: 480px;
}

.edit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
