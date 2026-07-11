<template>
    <div class="article-page">
        <el-card shadow="never" class="search-card">
            <div class="toolbar-row">
                <div class="toolbar-field">
                    <span class="toolbar-label">文章标题</span>
                    <el-input
                        v-model="searchTitle"
                        class="toolbar-input"
                        placeholder="请输入（模糊查询）"
                        clearable
                    />
                </div>

                <div class="toolbar-field">
                    <span class="toolbar-label">创建日期</span>
                    <el-date-picker
                        v-model="pickDate"
                        class="toolbar-date"
                        type="daterange"
                        range-separator="至"
                        start-placeholder="开始时间"
                        end-placeholder="结束时间"
                        :shortcuts="shortcuts"
                        value-format="YYYY-MM-DD"
                    />
                </div>

                <div class="toolbar-actions">
                    <el-button type="primary" :icon="Search" :loading="tableLoading" @click="handleQuery">查询</el-button>
                    <el-button :icon="RefreshRight" :disabled="tableLoading" @click="resetFilters">重置</el-button>
                </div>
            </div>
        </el-card>

        <el-card shadow="never" class="table-card">
            <div class="table-toolbar">
                <el-button type="primary" class="write-btn" @click="openPublishDrawer">
                    <el-icon class="mr-1"><EditPen /></el-icon>
                    写文章
                </el-button>
            </div>

            <el-table
                :data="pagedArticles"
                border
                stripe
                style="width: 100%"
                v-loading="tableLoading"
                class="article-table"
                empty-text="暂无文章数据"
            >
                <el-table-column label="标题" min-width="220">
                    <template #default="{ row }">
                        <div class="article-title-cell">
                            <div class="article-title">{{ row.title || '未命名文章' }}</div>
                        </div>
                    </template>
                </el-table-column>

                <el-table-column label="封面" width="110" align="center">
                    <template #default="{ row }">
                        <el-image
                            v-if="row.cover"
                            :src="row.cover"
                            fit="cover"
                            preview-teleported
                            :preview-src-list="[row.cover]"
                            class="cover-image"
                        />
                        <div v-else class="cover-empty">暂无封面</div>
                    </template>
                </el-table-column>

                <el-table-column label="发布时间" width="180" align="center">
                    <template #default="{ row }">
                        {{ formatDateTime(row.createTime) }}
                    </template>
                </el-table-column>

                <el-table-column label="操作" width="170" align="left">
                    <template #default="{ row }">
                        <el-button size="small" plain @click="openEditDrawer(row)">编辑</el-button>
                        <el-button type="danger" size="small" @click="deleteArticleSubmit($event, row)">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>

            <div class="pagination-wrap">
                <el-pagination
                    v-model:current-page="currentPage"
                    v-model:page-size="pageSize"
                    :page-sizes="[5, 10, 20, 50]"
                    :background="true"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="filteredArticles.length"
                />
            </div>
        </el-card>

        <el-dialog
            v-model="editorVisible"
            :width="'92%'"
            top="4vh"
            destroy-on-close
            align-center
            class="article-editor-dialog"
            :close-on-click-modal="false"
        >
            <template #header>
                <div class="dialog-header">
                    <div class="dialog-title">{{ isEditMode ? '编辑文章' : '写文章' }}</div>
                    <div class="dialog-header-actions">
                        <el-button size="small" @click="closeEditorDrawer">取消</el-button>
                        <el-button type="primary" size="small" :loading="submitLoading" @click="submitArticle">
                            {{ isEditMode ? '保存' : '发布' }}
                        </el-button>
                    </div>
                </div>
            </template>

            <el-form ref="formRef" :model="editorForm" :rules="formRules" label-position="top" class="editor-form">
                <el-form-item label="标题" prop="title">
                    <el-input
                        v-model="editorForm.title"
                        maxlength="40"
                        show-word-limit
                        placeholder="请输入文章标题"
                        clearable
                    />
                </el-form-item>

                <el-form-item label="摘要" prop="summary">
                    <el-input
                        v-model="editorForm.summary"
                        type="textarea"
                        :rows="2"
                        maxlength="160"
                        show-word-limit
                        placeholder="请输入文章摘要，方便列表页快速速览"
                    />
                </el-form-item>

                <el-form-item label="内容" prop="content">
                    <MdEditor
                        v-model="editorForm.content"
                        class="md-editor md-editor-large"
                        :preview-theme="'github'"
                        :code-theme="'atom'"
                        placeholder="请输入内容"
                    />
                </el-form-item>

                <div class="editor-setting-panel">
                    <div class="setting-block setting-cover-block">
                        <div class="setting-title">封面</div>
                        <el-form-item prop="cover" class="setting-form-item">
                            <el-upload
                                class="cover-uploader compact"
                                action="#"
                                :show-file-list="false"
                                :http-request="uploadCover"
                                :before-upload="beforeCoverUpload"
                            >
                                <img v-if="editorForm.cover" :src="editorForm.cover" class="cover-preview compact" />
                                <div v-else class="cover-placeholder compact">
                                    <el-icon class="cover-placeholder-icon"><Plus /></el-icon>
                                </div>
                            </el-upload>
                        </el-form-item>
                        <div class="cover-upload-text">{{ editorForm.cover ? '更换封面' : '上传封面' }}</div>
                    </div>

                    <div class="setting-block">
                        <el-form-item label="分类" prop="categoryId" class="setting-form-item">
                            <el-select
                                v-model="editorForm.categoryId"
                                placeholder="请选择文章分类"
                                class="w-full"
                                filterable
                            >
                                <el-option
                                    v-for="item in categoryOptions"
                                    :key="item.value"
                                    :label="item.label"
                                    :value="item.value"
                                />
                            </el-select>
                        </el-form-item>
                    </div>

                    <div class="setting-block">
                        <el-form-item label="标签" prop="tags" class="setting-form-item">
                            <el-select
                                v-model="editorForm.tags"
                                class="w-full"
                                multiple
                                filterable
                                allow-create
                                default-first-option
                                collapse-tags
                                collapse-tags-tooltip
                                placeholder="请选择或直接输入标签"
                            >
                                <el-option
                                    v-for="item in tagOptions"
                                    :key="item.value"
                                    :label="item.label"
                                    :value="item.value"
                                />
                            </el-select>
                        </el-form-item>
                    </div>

                    <div class="setting-block" v-if="isEditMode">
                        <el-form-item label="阅读量" class="setting-form-item">
                            <el-input-number v-model="editorForm.readNum" class="w-full" :min="0" />
                        </el-form-item>
                    </div>
                </div>
            </el-form>
        </el-dialog>
    </div>
</template>

<script setup>
import { computed, nextTick, reactive, ref } from 'vue'
import { Search, RefreshRight } from '@element-plus/icons-vue'
import moment from 'moment'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { showMessage, showModel } from '@/composables/util'
import { uploadFile } from '@/api/admin/blogsettings'
import { getCategorySelectList } from '@/api/admin/category'
import { getTagSelectList } from '@/api/admin/tag'
import {
    deleteArticle,
    getArticleDetail,
    getArticleList,
    publishArticle,
    updateArticle,
} from '@/api/admin/article'

const searchTitle = ref('')
const pickDate = ref([])

const tableLoading = ref(false)
const submitLoading = ref(false)
const optionLoading = ref(false)
const editorVisible = ref(false)
const isEditMode = ref(false)
const formRef = ref(null)

const currentPage = ref(1)
const pageSize = ref(10)

const articleList = ref([])
const categoryOptions = ref([])
const tagOptions = ref([])

const editorForm = reactive({
    id: null,
    title: '',
    cover: '',
    summary: '',
    content: '',
    categoryId: null,
    tags: [],
    readNum: 0,
    createTime: '',
    updateTime: '',
    isDeleted: 0,
})

const formRules = {
    title: [
        { required: true, message: '请输入文章标题', trigger: 'blur' },
        { min: 1, max: 40, message: '文章标题长度需在 1 到 40 个字符之间', trigger: 'blur' },
    ],
    cover: [{ required: true, message: '请上传文章封面', trigger: 'change' }],
    content: [{
        validator: (_, value, callback) => {
            if (!value || !String(value).trim()) {
                callback(new Error('请输入文章正文'))
                return
            }
            callback()
        },
        trigger: 'blur',
    }],
    categoryId: [{
        validator: (_, value, callback) => {
            if (value) {
                callback()
                return
            }
            callback(new Error('请选择文章分类'))
        },
        trigger: 'change',
    }],
    tags: [{
        validator: (_, value, callback) => {
            if (Array.isArray(value) && value.length > 0) {
                callback()
                return
            }
            callback(new Error('请至少选择一个标签'))
        },
        trigger: 'change',
    }],
}

const shortcuts = [
    {
        text: '最近一周',
        value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
            return [start, end]
        },
    },
    {
        text: '最近一个月',
        value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
            return [start, end]
        },
    },
    {
        text: '最近三个月',
        value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
            return [start, end]
        },
    },
]

const filteredArticles = computed(() => {
    const keyword = searchTitle.value.trim().toLowerCase()
    const start = pickDate.value?.[0] ? moment(pickDate.value[0]).startOf('day') : null
    const end = pickDate.value?.[1] ? moment(pickDate.value[1]).endOf('day') : null

    return articleList.value.filter((item) => {
        const title = String(item.title || '').toLowerCase()
        const matchTitle = !keyword || title.includes(keyword)

        if (!matchTitle) {
            return false
        }

        if (!start || !end) {
            return true
        }

        if (!item.createTime) {
            return false
        }

        const createTime = moment(item.createTime)
        return createTime.isBetween(start, end, null, '[]')
    })
})

const pagedArticles = computed(() => {
    const start = (currentPage.value - 1) * pageSize.value
    const end = start + pageSize.value
    return filteredArticles.value.slice(start, end)
})

const resetEditorForm = () => {
    editorForm.id = null
    editorForm.title = ''
    editorForm.cover = ''
    editorForm.summary = ''
    editorForm.content = ''
    editorForm.categoryId = null
    editorForm.tags = []
    editorForm.readNum = 0
    editorForm.createTime = ''
    editorForm.updateTime = ''
    editorForm.isDeleted = 0
}

const formatDateTime = (value) => {
    if (!value) return '-'
    const time = moment(value)
    return time.isValid() ? time.format('YYYY-MM-DD HH:mm:ss') : value
}

const sortByCreateTime = (list) => {
    return [...list].sort((a, b) => {
        const timeA = new Date(a.createTime || 0).getTime()
        const timeB = new Date(b.createTime || 0).getTime()
        return timeB - timeA
    })
}

const getUploadedUrl = (res) => {
    const data = res?.data
    if (!data) return ''
    if (typeof data === 'string') return data
    if (typeof data === 'object') return data.url || data.fileUrl || data.path || data.fullPath || ''
    return ''
}

const fetchArticleList = () => {
    tableLoading.value = true
    getArticleList()
        .then((res) => {
            if (res.code === 0) {
                articleList.value = sortByCreateTime(Array.isArray(res.data) ? res.data : [])
                const maxPage = Math.max(1, Math.ceil(filteredArticles.value.length / pageSize.value))
                if (currentPage.value > maxPage) {
                    currentPage.value = maxPage
                }
            } else {
                showMessage(res.message || '获取文章列表失败', 'error')
            }
        })
        .catch(() => {
            showMessage('获取文章列表失败', 'error')
        })
        .finally(() => {
            tableLoading.value = false
        })
}

const fetchOptions = () => {
    optionLoading.value = true
    Promise.all([getCategorySelectList(), getTagSelectList()])
        .then(([categoryRes, tagRes]) => {
            if (categoryRes.code === 0) {
                categoryOptions.value = Array.isArray(categoryRes.data) ? categoryRes.data : []
            }
            if (tagRes.code === 0) {
                tagOptions.value = Array.isArray(tagRes.data)
                    ? tagRes.data.map((item) => ({
                        label: item.label,
                        value: item.label,
                    }))
                    : []
            }
        })
        .finally(() => {
            optionLoading.value = false
        })
}

const handleQuery = () => {
    currentPage.value = 1
    fetchArticleList()
}

const resetFilters = () => {
    searchTitle.value = ''
    pickDate.value = []
    currentPage.value = 1
    fetchArticleList()
}

const closeEditorDrawer = () => {
    editorVisible.value = false
}

const openPublishDrawer = () => {
    isEditMode.value = false
    resetEditorForm()
    editorVisible.value = true
    nextTick(() => formRef.value?.clearValidate())
}

const openEditDrawer = (row) => {
    tableLoading.value = true
    getArticleDetail(row.id)
        .then((res) => {
            if (res.code !== 0 || !res.data) {
                showMessage(res.message || '获取文章详情失败', 'error')
                return
            }

            const data = res.data
            isEditMode.value = true
            resetEditorForm()
            editorForm.id = data.id
            editorForm.title = data.title || ''
            editorForm.cover = data.cover || ''
            editorForm.summary = data.summary || ''
            editorForm.content = data.content || ''
            editorForm.categoryId = data.categoryId ?? null
            editorForm.tags = Array.isArray(data.tags) ? data.tags : []
            editorForm.readNum = Number(data.readNum || 0)
            editorForm.createTime = data.createTime || ''
            editorForm.updateTime = data.updateTime || ''
            editorForm.isDeleted = Number(data.isDeleted || 0)
            editorVisible.value = true
            nextTick(() => formRef.value?.clearValidate())
        })
        .catch(() => {
            showMessage('获取文章详情失败', 'error')
        })
        .finally(() => {
            tableLoading.value = false
        })
}

const beforeCoverUpload = (rawFile) => {
    const isImage = rawFile.type?.startsWith('image/')
    if (!isImage) {
        showMessage('只能上传图片文件', 'error')
        return false
    }

    const isLt2M = rawFile.size / 1024 / 1024 < 2
    if (!isLt2M) {
        showMessage('图片大小不能超过 2MB', 'error')
        return false
    }

    return true
}

const uploadCover = (options) => {
    const formData = new FormData()
    formData.append('file', options.file)
    uploadFile(formData)
        .then((res) => {
            if (res.code === 0) {
                const url = getUploadedUrl(res)
                if (url) {
                    editorForm.cover = url
                    formRef.value?.validateField?.('cover')
                }
                options.onSuccess(res)
            } else {
                showMessage(res.message || '封面上传失败', 'error')
                options.onError(new Error(res.message || '封面上传失败'))
            }
        })
        .catch((err) => {
            showMessage('封面上传失败', 'error')
            options.onError(err)
        })
}

const submitArticle = () => {
    formRef.value.validate((valid) => {
        if (!valid) {
            return false
        }

        submitLoading.value = true

        const request = isEditMode.value
            ? updateArticle({
                id: editorForm.id,
                title: editorForm.title,
                content: editorForm.content,
                cover: editorForm.cover,
                summary: editorForm.summary,
                categoryId: editorForm.categoryId,
                tags: editorForm.tags,
                readNum: Number(editorForm.readNum || 0),
            })
            : publishArticle({
                title: editorForm.title,
                content: editorForm.content,
                cover: editorForm.cover,
                summary: editorForm.summary,
                categoryId: editorForm.categoryId,
                tags: editorForm.tags,
            })

        request
            .then((res) => {
                if (res.code === 0) {
                    showMessage(isEditMode.value ? '文章更新成功' : '文章发布成功')
                    closeEditorDrawer()
                    fetchArticleList()
                } else {
                    showMessage(res.message || '操作失败', 'error')
                }
            })
            .catch(() => {
                showMessage('请求失败', 'error')
            })
            .finally(() => {
                submitLoading.value = false
            })
    })
}

const deleteArticleSubmit = (e, row) => {
    e?.currentTarget?.blur?.()
    showModel(`是否确定删除文章《${row.title || '未命名文章'}》？`)
        .then(() => deleteArticle(row.id))
        .then((res) => {
            if (res.code === 0) {
                showMessage('删除成功')
                fetchArticleList()
            } else {
                showMessage(res.message || '删除失败', 'error')
            }
        })
        .catch((err) => {
            if (err !== 'cancel') {
                showMessage('删除失败', 'error')
            }
        })
}

fetchOptions()
fetchArticleList()
</script>

<style scoped>
.article-page {
    min-height: 100%;
}

.search-card,
.table-card {
    margin-bottom: 16px;
}

.search-card :deep(.el-card__body),
.table-card :deep(.el-card__body) {
    padding: 16px;
}

.toolbar-row {
    display: flex;
    align-items: center;
    gap: 16px;
    flex-wrap: wrap;
}

.toolbar-field {
    display: flex;
    align-items: center;
    gap: 12px;
}

.toolbar-label {
    font-size: 14px;
    color: #606266;
    white-space: nowrap;
}

.toolbar-input {
    width: 220px;
}

.toolbar-date {
    width: 340px;
}

.toolbar-actions {
    display: flex;
    align-items: center;
    gap: 12px;
    padding-left: 4px;
}

.table-toolbar {
    display: flex;
    align-items: center;
    margin-bottom: 14px;
}

.write-btn {
    padding: 8px 14px;
}

.article-title-cell {
    display: flex;
    flex-direction: column;
}

.article-title {
    color: #606266;
}

.cover-image {
    width: 44px;
    height: 24px;
    border-radius: 2px;
}

.cover-empty {
    font-size: 12px;
    color: #c0c4cc;
}

.article-table :deep(.el-table__header th) {
    background: #fafafa;
    color: #606266;
    font-weight: 500;
}

.article-table :deep(.el-table__cell) {
    padding: 10px 0;
}

.pagination-wrap {
    display: flex;
    justify-content: center;
    margin-top: 22px;
}

.drawer-tip {
    margin-bottom: 16px;
    padding: 10px 14px;
    border-radius: 8px;
    background: #fff7e6;
    color: #a05a00;
    font-size: 13px;
    line-height: 1.6;
}

.md-editor {
    height: 560px;
}

.md-editor-large {
    height: 420px;
}

.editor-form {
    min-width: 0;
}

.dialog-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding-right: 28px;
}

.dialog-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
}

.dialog-header-actions {
    display: flex;
    align-items: center;
    gap: 8px;
}

.editor-setting-panel {
    display: grid;
    grid-template-columns: 140px minmax(0, 1fr) minmax(0, 1fr);
    gap: 20px;
    align-items: start;
    margin-top: 8px;
    padding-top: 16px;
    border-top: 1px solid #ebeef5;
}

.setting-block {
    min-width: 0;
}

.setting-cover-block {
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.setting-title {
    font-size: 13px;
    color: #606266;
}

.setting-form-item {
    margin-bottom: 0;
}

.cover-uploader :deep(.el-upload) {
    width: 100%;
    display: block;
    border: 1px dashed var(--el-border-color);
    border-radius: 8px;
    overflow: hidden;
    transition: border-color .2s ease;
}

.cover-uploader :deep(.el-upload:hover) {
    border-color: var(--el-color-primary);
}

.cover-preview {
    width: 100%;
    height: 180px;
    object-fit: cover;
    display: block;
}

.cover-uploader.compact :deep(.el-upload) {
    width: 92px;
    height: 92px;
}

.cover-preview.compact {
    width: 92px;
    height: 92px;
    object-fit: cover;
}

.cover-placeholder {
    height: 180px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-direction: column;
    gap: 10px;
    color: #909399;
    background: #fafafa;
}

.cover-placeholder.compact {
    width: 92px;
    height: 92px;
    gap: 0;
}

.cover-placeholder-icon {
    font-size: 28px;
}

.cover-upload-text {
    font-size: 12px;
    color: #909399;
}

@media (max-width: 1200px) {
    .editor-setting-panel {
        grid-template-columns: 1fr 1fr;
    }

    .setting-cover-block {
        grid-column: 1 / -1;
    }
}

@media (max-width: 992px) {
    .toolbar-row {
        align-items: flex-start;
    }

    .toolbar-actions {
        padding-left: 0;
    }

    .dialog-header {
        flex-direction: column;
        align-items: flex-start;
        padding-right: 0;
    }

    .editor-setting-panel {
        grid-template-columns: 1fr;
        gap: 14px;
    }

    .md-editor-large {
        height: 340px;
    }
}

:deep(.article-editor-dialog .el-dialog) {
    border-radius: 10px;
    overflow: hidden;
}

:deep(.article-editor-dialog .el-dialog__header) {
    margin-right: 0;
    padding: 18px 24px 10px;
    border-bottom: 1px solid #ebeef5;
}

:deep(.article-editor-dialog .el-dialog__body) {
    padding: 18px 24px 24px;
}

:deep(.article-editor-dialog .el-dialog__headerbtn) {
    top: 18px;
    right: 18px;
}
</style>
