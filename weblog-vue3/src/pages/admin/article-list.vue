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
                <el-button class="import-btn" @click="openImportDialog">
                    <el-icon class="mr-1"><UploadFilled /></el-icon>
                    批量导入 .md
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
                <el-table-column label="序号" width="80" align="center" type="index" :index="getRowIndex" />
                <el-table-column label="ID" width="90" align="center">
                    <template #default="{ row }">
                        {{ row.id || '-' }}
                    </template>
                </el-table-column>

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

                <el-table-column label="操作" width="240" align="left">
                    <template #default="{ row }">
                        <el-button size="small" plain @click="previewArticle(row)">预览</el-button>
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

        <el-dialog
            v-model="importVisible"
            width="760px"
            destroy-on-close
            class="article-import-dialog"
            :close-on-click-modal="false"
        >
            <template #header>
                <div class="dialog-header">
                    <div class="dialog-title">批量导入 Markdown</div>
                    <div class="dialog-header-actions">
                        <el-button size="small" @click="closeImportDialog">取消</el-button>
                        <el-button type="primary" size="small" :loading="importLoading" @click="submitImportArticles">
                            开始导入
                        </el-button>
                    </div>
                </div>
            </template>

            <div class="import-tip">
                支持一次选择多个 `.md` 文件。若 Markdown 中引用了本地图片，建议直接选择整个文章目录，系统会自动上传正文图片并替换为线上地址。
            </div>

            <input
                ref="markdownInputRef"
                class="hidden-file-input"
                type="file"
                accept=".md,text/markdown"
                multiple
                @change="handleMarkdownFilesChange"
            />
            <input
                ref="markdownDirectoryInputRef"
                class="hidden-file-input"
                type="file"
                multiple
                webkitdirectory
                @change="handleMarkdownDirectoryChange"
            />

            <div class="import-toolbar">
                <el-button type="primary" plain @click="triggerMarkdownSelect">选择 Markdown 文件</el-button>
                <el-button plain @click="triggerMarkdownDirectorySelect">选择目录（含图片）</el-button>
                <span class="import-count">已选择 {{ importFileList.length }} 个 Markdown 文件</span>
                <span v-if="importAssetCount > 0" class="import-count">已识别 {{ importAssetCount }} 张正文图片</span>
                <el-button link type="danger" :disabled="importLoading || importFileList.length === 0" @click="clearImportFiles">
                    清空文件
                </el-button>
            </div>

            <el-form
                ref="importFormRef"
                :model="importForm"
                :rules="importFormRules"
                label-position="top"
                class="import-form"
            >
                <div class="import-setting-grid">
                    <div class="setting-block setting-cover-block">
                        <div class="setting-title">统一封面</div>
                        <el-form-item prop="cover" class="setting-form-item">
                            <el-upload
                                class="cover-uploader compact"
                                action="#"
                                :show-file-list="false"
                                :http-request="uploadImportCover"
                                :before-upload="beforeCoverUpload"
                            >
                                <img v-if="importForm.cover" :src="importForm.cover" class="cover-preview compact" />
                                <div v-else class="cover-placeholder compact">
                                    <el-icon class="cover-placeholder-icon"><Plus /></el-icon>
                                </div>
                            </el-upload>
                        </el-form-item>
                        <div class="cover-upload-text">{{ importForm.cover ? '更换封面' : '上传封面' }}</div>
                    </div>

                    <div class="setting-block">
                        <el-form-item label="统一分类" prop="categoryId" class="setting-form-item">
                            <el-select
                                v-model="importForm.categoryId"
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
                        <el-form-item label="统一标签" prop="tags" class="setting-form-item">
                            <el-select
                                v-model="importForm.tags"
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
                </div>
            </el-form>

            <div class="import-list-wrap">
                <div class="import-list-header">
                    <span>待导入文件</span>
                    <span v-if="importResultText" class="import-result-text">{{ importResultText }}</span>
                </div>

                <div v-if="importFileList.length === 0" class="import-empty">
                    暂未选择 Markdown 文件
                </div>

                <div v-else class="import-file-list">
                    <div v-for="item in importFileList" :key="item.uid" class="import-file-item">
                        <div class="import-file-main">
                            <div class="import-file-name">{{ item.fileName }}</div>
                            <div class="import-file-title">标题：{{ item.title || '未解析到标题，将使用文件名' }}</div>
                            <div class="import-file-summary">
                                摘要：{{ item.summary || '未解析到摘要，将自动留空' }}
                            </div>
                        </div>

                        <div class="import-file-side">
                            <el-tag
                                :type="item.status === 'success' ? 'success' : item.status === 'error' ? 'danger' : 'info'"
                                effect="plain"
                            >
                                {{ item.status === 'success' ? '已导入' : item.status === 'error' ? '失败' : '待导入' }}
                            </el-tag>
                            <el-button
                                link
                                type="danger"
                                :disabled="importLoading"
                                @click="removeImportFile(item.uid)"
                            >
                                移除
                            </el-button>
                        </div>
                    </div>
                </div>
            </div>
        </el-dialog>
    </div>
</template>

<script setup>
import { computed, nextTick, reactive, ref } from 'vue'
import { Search, RefreshRight, UploadFilled } from '@element-plus/icons-vue'
import moment from 'moment'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { showMessage, showModel } from '@/composables/util'
import { uploadFile } from '@/api/admin/blogsettings'
import { getCategorySelectList } from '@/api/admin/category'
import { getTagSelectList } from '@/api/admin/tag'
import router from '@/router'
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
const importVisible = ref(false)
const importLoading = ref(false)
const formRef = ref(null)
const importFormRef = ref(null)
const markdownInputRef = ref(null)
const markdownDirectoryInputRef = ref(null)

const currentPage = ref(1)
const pageSize = ref(10)

const articleList = ref([])
const categoryOptions = ref([])
const tagOptions = ref([])
const importFileList = ref([])
const importResultText = ref('')
const importAssetCount = ref(0)
const importAssetFileMap = ref(new Map())
const importAssetNameMap = ref(new Map())
const uploadedAssetUrlMap = ref(new Map())

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

const importForm = reactive({
    cover: '',
    categoryId: null,
    tags: [],
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

const importFormRules = {
    cover: [{ required: true, message: '请上传统一封面', trigger: 'change' }],
    categoryId: [{
        validator: (_, value, callback) => {
            if (value) {
                callback()
                return
            }
            callback(new Error('请选择统一分类'))
        },
        trigger: 'change',
    }],
    tags: [{
        validator: (_, value, callback) => {
            if (Array.isArray(value) && value.length > 0) {
                callback()
                return
            }
            callback(new Error('请至少选择一个统一标签'))
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

const getRowIndex = (index) => {
    return (currentPage.value - 1) * pageSize.value + index + 1
}

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

const resetImportForm = () => {
    importForm.cover = ''
    importForm.categoryId = null
    importForm.tags = []
    importFileList.value = []
    importResultText.value = ''
    importAssetCount.value = 0
    importAssetFileMap.value = new Map()
    importAssetNameMap.value = new Map()
    uploadedAssetUrlMap.value = new Map()
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

const closeImportDialog = () => {
    importVisible.value = false
}

const openPublishDrawer = () => {
    isEditMode.value = false
    resetEditorForm()
    editorVisible.value = true
    nextTick(() => formRef.value?.clearValidate())
}

const openImportDialog = () => {
    resetImportForm()
    importVisible.value = true
    nextTick(() => importFormRef.value?.clearValidate())
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

const uploadImportCover = (options) => {
    const formData = new FormData()
    formData.append('file', options.file)
    uploadFile(formData)
        .then((res) => {
            if (res.code === 0) {
                const url = getUploadedUrl(res)
                if (url) {
                    importForm.cover = url
                    importFormRef.value?.validateField?.('cover')
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

const triggerMarkdownSelect = () => {
    markdownInputRef.value?.click()
}

const triggerMarkdownDirectorySelect = () => {
    markdownDirectoryInputRef.value?.click()
}

const normalizeMarkdownText = (content = '') => {
    return String(content).replace(/^\uFEFF/, '').replace(/\r\n/g, '\n')
}

const IMAGE_FILE_EXTENSIONS = ['.png', '.jpg', '.jpeg', '.gif', '.webp', '.bmp', '.svg', '.avif']

const isMarkdownFile = (file) => /\.md$/i.test(file?.name || '')

const isImageFile = (file) => {
    const fileName = String(file?.name || '').toLowerCase()
    return file?.type?.startsWith?.('image/') || IMAGE_FILE_EXTENSIONS.some(ext => fileName.endsWith(ext))
}

const normalizeImportPath = (path = '') => {
    return decodeURIComponent(String(path || ''))
        .replace(/\\/g, '/')
        .replace(/^\.\//, '')
        .replace(/^\/+/, '')
        .trim()
}

const getDirname = (path = '') => {
    const normalized = normalizeImportPath(path)
    const lastSlashIndex = normalized.lastIndexOf('/')
    return lastSlashIndex === -1 ? '' : normalized.slice(0, lastSlashIndex)
}

const resolveRelativePath = (baseDir, targetPath) => {
    const normalizedTarget = normalizeImportPath(targetPath)
    const normalizedBaseDir = normalizeImportPath(baseDir)
    const segments = `${normalizedBaseDir ? `${normalizedBaseDir}/` : ''}${normalizedTarget}`.split('/')
    const resolved = []

    segments.forEach((segment) => {
        if (!segment || segment === '.') {
            return
        }

        if (segment === '..') {
            resolved.pop()
            return
        }

        resolved.push(segment)
    })

    return resolved.join('/')
}

const isRemoteResourcePath = (path = '') => {
    const value = String(path || '').trim().toLowerCase()
    return !value || /^(https?:)?\/\//.test(value) || value.startsWith('data:') || value.startsWith('blob:')
}

const extractMarkdownLinkPath = (rawTarget = '') => {
    const target = String(rawTarget || '').trim()
    if (!target) {
        return ''
    }

    if (target.startsWith('<')) {
        const endIndex = target.indexOf('>')
        if (endIndex > 0) {
            return target.slice(1, endIndex).trim()
        }
    }

    const match = target.match(/^(.+?)(?:\s+["'][^"']*["'])?$/)
    return match ? match[1].trim() : target
}

const replaceMarkdownLinkPath = (rawTarget, oldPath, newPath) => {
    if (!rawTarget || !oldPath) {
        return rawTarget
    }

    if (String(rawTarget).trim().startsWith('<')) {
        return rawTarget.replace(`<${oldPath}>`, `<${newPath}>`)
    }

    return rawTarget.replace(oldPath, newPath)
}

const getFileRelativePath = (file) => {
    return normalizeImportPath(file?.webkitRelativePath || file?.name || '')
}

const appendImportAssets = (files) => {
    const nextFileMap = new Map(importAssetFileMap.value)
    const nextNameMap = new Map(importAssetNameMap.value)

    files.forEach((file) => {
        if (!isImageFile(file)) {
            return
        }

        const relativePath = getFileRelativePath(file)
        if (!relativePath) {
            return
        }

        nextFileMap.set(relativePath.toLowerCase(), file)

        const fileName = String(file.name || '').toLowerCase()
        const sameNameFiles = nextNameMap.get(fileName) || []
        sameNameFiles.push(file)
        nextNameMap.set(fileName, sameNameFiles)
    })

    importAssetFileMap.value = nextFileMap
    importAssetNameMap.value = nextNameMap
    importAssetCount.value = nextFileMap.size
}

const stripFrontMatter = (content) => {
    if (!content.startsWith('---\n')) {
        return content
    }

    const endIndex = content.indexOf('\n---\n', 4)
    if (endIndex === -1) {
        return content
    }

    return content.slice(endIndex + 5)
}

const removeMarkdownSyntax = (text = '') => {
    return String(text)
        .replace(/!\[[^\]]*]\([^)]*\)/g, '')
        .replace(/\[([^\]]+)\]\(([^)]*)\)/g, '$1')
        .replace(/[`>#*_~-]/g, '')
        .replace(/\s+/g, ' ')
        .trim()
}

const parseMarkdownFile = async (file) => {
    const content = normalizeMarkdownText(await file.text())
    const body = stripFrontMatter(content)
    const lines = body.split('\n')

    let title = ''
    let titleIndex = -1
    let inCodeBlock = false

    for (let index = 0; index < lines.length; index += 1) {
        const line = lines[index]
        const trimmed = line.trim()

        if (trimmed.startsWith('```')) {
            inCodeBlock = !inCodeBlock
        }

        if (inCodeBlock) {
            continue
        }

        const headingMatch = trimmed.match(/^#\s+(.+)/)
        if (headingMatch) {
            title = removeMarkdownSyntax(headingMatch[1])
            titleIndex = index
            break
        }
    }

    if (!title) {
        title = file.name.replace(/\.md$/i, '')
    }

    const contentLines = [...lines]
    if (titleIndex !== -1) {
        contentLines.splice(titleIndex, 1)
    }

    const markdownBody = contentLines.join('\n').trim()

    let summary = ''
    inCodeBlock = false
    for (const line of contentLines) {
        const trimmed = line.trim()

        if (trimmed.startsWith('```')) {
            inCodeBlock = !inCodeBlock
            continue
        }

        if (
            inCodeBlock
            || !trimmed
            || trimmed.startsWith('#')
            || trimmed.startsWith('>')
            || /^[-*+]\s/.test(trimmed)
            || /^\d+\.\s/.test(trimmed)
            || trimmed.startsWith('![')
        ) {
            continue
        }

        summary = removeMarkdownSyntax(trimmed).slice(0, 160)
        if (summary) {
            break
        }
    }

    return {
        title: title.slice(0, 40),
        summary,
        content: markdownBody || body.trim(),
        relativePath: getFileRelativePath(file),
    }
}

const appendParsedMarkdownFiles = async (files) => {
    const markdownFiles = files.filter(isMarkdownFile)
    if (markdownFiles.length === 0) {
        return
    }

    const parsedList = await Promise.all(
        markdownFiles.map(async (file, index) => {
            const relativePath = getFileRelativePath(file)
            try {
                const parsed = await parseMarkdownFile(file)
                return {
                    uid: `${relativePath || file.name}-${file.size}-${file.lastModified}-${index}`,
                    fileName: file.name,
                    relativePath,
                    raw: file,
                    ...parsed,
                    status: 'pending',
                }
            } catch (error) {
                return {
                    uid: `${relativePath || file.name}-${file.size}-${file.lastModified}-${index}`,
                    fileName: file.name,
                    relativePath,
                    raw: file,
                    title: file.name.replace(/\.md$/i, ''),
                    summary: '',
                    content: '',
                    status: 'error',
                    errorMessage: '文件解析失败',
                }
            }
        })
    )

    const uidSet = new Set(importFileList.value.map(item => item.uid))
    const nextList = parsedList.filter(item => !uidSet.has(item.uid))
    importFileList.value = [...importFileList.value, ...nextList]
    importResultText.value = ''
}

const handleMarkdownFilesChange = async (event) => {
    const files = Array.from(event.target?.files || [])
    if (files.length === 0) {
        return
    }

    await appendParsedMarkdownFiles(files)
    event.target.value = ''
}

const handleMarkdownDirectoryChange = async (event) => {
    const files = Array.from(event.target?.files || [])
    if (files.length === 0) {
        return
    }

    appendImportAssets(files)
    await appendParsedMarkdownFiles(files)
    event.target.value = ''
}

const clearImportFiles = () => {
    importFileList.value = []
    importResultText.value = ''
    importAssetCount.value = 0
    importAssetFileMap.value = new Map()
    importAssetNameMap.value = new Map()
    uploadedAssetUrlMap.value = new Map()
}

const removeImportFile = (uid) => {
    importFileList.value = importFileList.value.filter(item => item.uid !== uid)
}

const uploadInlineImage = async (file) => {
    const formData = new FormData()
    formData.append('file', file)
    const res = await uploadFile(formData)

    if (res.code !== 0) {
        throw new Error(res.message || '正文图片上传失败')
    }

    const url = getUploadedUrl(res)
    if (!url) {
        throw new Error('正文图片上传成功，但未返回访问地址')
    }

    return url
}

const resolveImportAssetFile = (targetPath, markdownRelativePath = '') => {
    const cleanTargetPath = normalizeImportPath(targetPath)
    if (!cleanTargetPath) {
        return null
    }

    const markdownDir = getDirname(markdownRelativePath)
    const resolvedPath = resolveRelativePath(markdownDir, cleanTargetPath)
    const exactFile = importAssetFileMap.value.get(resolvedPath.toLowerCase())
    if (exactFile) {
        return {
            cacheKey: resolvedPath.toLowerCase(),
            file: exactFile,
        }
    }

    const fileName = cleanTargetPath.split('/').pop()?.toLowerCase() || ''
    const sameNameFiles = importAssetNameMap.value.get(fileName) || []
    if (sameNameFiles.length === 1) {
        return {
            cacheKey: getFileRelativePath(sameNameFiles[0]).toLowerCase(),
            file: sameNameFiles[0],
        }
    }

    return null
}

const replaceLocalImagesInMarkdown = async (content, markdownRelativePath = '') => {
    let nextContent = content
    const markdownImageRegex = /!\[[^\]]*]\(([^)\n]+)\)/g
    const htmlImageRegex = /<img\b[^>]*\bsrc=["']([^"']+)["'][^>]*>/gi

    const markdownTargets = new Map()
    Array.from(content.matchAll(markdownImageRegex)).forEach((match) => {
        const rawTarget = match[1]
        const resourcePath = extractMarkdownLinkPath(rawTarget)
        if (!isRemoteResourcePath(resourcePath)) {
            markdownTargets.set(rawTarget, resourcePath)
        }
    })

    for (const [rawTarget, resourcePath] of markdownTargets.entries()) {
        const resolvedAsset = resolveImportAssetFile(resourcePath, markdownRelativePath)
        if (!resolvedAsset?.file) {
            throw new Error(`未找到正文图片资源：${resourcePath}。请使用“选择目录（含图片）”导入文章目录。`)
        }

        let uploadedUrl = uploadedAssetUrlMap.value.get(resolvedAsset.cacheKey)
        if (!uploadedUrl) {
            uploadedUrl = await uploadInlineImage(resolvedAsset.file)
            uploadedAssetUrlMap.value.set(resolvedAsset.cacheKey, uploadedUrl)
        }

        const replacedTarget = replaceMarkdownLinkPath(rawTarget, resourcePath, uploadedUrl)
        nextContent = nextContent.replaceAll(rawTarget, replacedTarget)
    }

    const htmlTargets = new Map()
    Array.from(content.matchAll(htmlImageRegex)).forEach((match) => {
        const resourcePath = match[1]
        if (!isRemoteResourcePath(resourcePath)) {
            htmlTargets.set(resourcePath, resourcePath)
        }
    })

    for (const resourcePath of htmlTargets.keys()) {
        const resolvedAsset = resolveImportAssetFile(resourcePath, markdownRelativePath)
        if (!resolvedAsset?.file) {
            throw new Error(`未找到正文图片资源：${resourcePath}。请使用“选择目录（含图片）”导入文章目录。`)
        }

        let uploadedUrl = uploadedAssetUrlMap.value.get(resolvedAsset.cacheKey)
        if (!uploadedUrl) {
            uploadedUrl = await uploadInlineImage(resolvedAsset.file)
            uploadedAssetUrlMap.value.set(resolvedAsset.cacheKey, uploadedUrl)
        }

        nextContent = nextContent.replaceAll(`src="${resourcePath}"`, `src="${uploadedUrl}"`)
        nextContent = nextContent.replaceAll(`src='${resourcePath}'`, `src='${uploadedUrl}'`)
    }

    return nextContent
}

const submitImportArticles = async () => {
    if (importFileList.value.length === 0) {
        showMessage('请先选择 Markdown 文件', 'warning')
        return
    }

    const hasInvalidFile = importFileList.value.some(item => !item.content)
    if (hasInvalidFile) {
        showMessage('存在解析失败的 Markdown 文件，请先移除后再导入', 'warning')
        return
    }

    const valid = await importFormRef.value.validate().catch(() => false)
    if (!valid) {
        return
    }

    importLoading.value = true
    let successCount = 0
    let failCount = 0

    for (const item of importFileList.value) {
        try {
            const content = await replaceLocalImagesInMarkdown(item.content, item.relativePath)
            const res = await publishArticle({
                title: item.title,
                content,
                cover: importForm.cover,
                summary: item.summary,
                categoryId: importForm.categoryId,
                tags: importForm.tags,
            })

            if (res.code === 0) {
                item.status = 'success'
                successCount += 1
            } else {
                item.status = 'error'
                item.errorMessage = res.message || '导入失败'
                failCount += 1
            }
        } catch (error) {
            item.status = 'error'
            item.errorMessage = '导入失败'
            failCount += 1
        }

        importResultText.value = `已完成 ${successCount + failCount} / ${importFileList.value.length}`
    }

    importLoading.value = false

    if (successCount > 0) {
        fetchArticleList()
    }

    showMessage(`导入完成，成功 ${successCount} 篇，失败 ${failCount} 篇`, failCount > 0 ? 'warning' : 'success')
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

const previewArticle = (row) => {
    if (!row?.id) {
        return
    }

    const previewUrl = router.resolve({
        path: `/article/${row.id}`,
    }).href

    window.open(previewUrl, '_blank')
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
    gap: 12px;
    margin-bottom: 14px;
}

.write-btn {
    padding: 8px 14px;
}

.import-btn {
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

.hidden-file-input {
    display: none;
}

.import-tip {
    margin-bottom: 14px;
    padding: 10px 14px;
    border-radius: 8px;
    background: #f4f8ff;
    color: #4b6ba8;
    font-size: 13px;
    line-height: 1.7;
}

.import-toolbar {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 18px;
}

.import-count {
    font-size: 13px;
    color: #909399;
}

.import-form {
    margin-bottom: 18px;
}

.import-setting-grid {
    display: grid;
    grid-template-columns: 140px minmax(0, 1fr) minmax(0, 1fr);
    gap: 20px;
    align-items: start;
}

.import-list-wrap {
    border: 1px solid #ebeef5;
    border-radius: 10px;
    overflow: hidden;
}

.import-list-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    border-bottom: 1px solid #ebeef5;
    background: #fafafa;
    font-size: 14px;
    font-weight: 500;
    color: #303133;
}

.import-result-text {
    font-size: 12px;
    font-weight: 400;
    color: #909399;
}

.import-empty {
    padding: 28px 16px;
    text-align: center;
    color: #909399;
    font-size: 13px;
}

.import-file-list {
    max-height: 320px;
    overflow-y: auto;
}

.import-file-item {
    display: flex;
    justify-content: space-between;
    gap: 16px;
    padding: 14px 16px;
    border-top: 1px solid #f2f3f5;
}

.import-file-item:first-child {
    border-top: 0;
}

.import-file-main {
    min-width: 0;
    flex: 1;
}

.import-file-name {
    font-size: 14px;
    font-weight: 500;
    color: #303133;
    word-break: break-all;
}

.import-file-title,
.import-file-summary {
    margin-top: 6px;
    font-size: 12px;
    line-height: 1.7;
    color: #909399;
    word-break: break-all;
}

.import-file-side {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    justify-content: space-between;
    gap: 10px;
}

@media (max-width: 1200px) {
    .editor-setting-panel {
        grid-template-columns: 1fr 1fr;
    }

    .setting-cover-block {
        grid-column: 1 / -1;
    }

    .import-setting-grid {
        grid-template-columns: 1fr 1fr;
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

    .import-toolbar {
        align-items: flex-start;
        flex-wrap: wrap;
    }

    .import-setting-grid {
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

:deep(.article-import-dialog .el-dialog) {
    border-radius: 10px;
    overflow: hidden;
}

:deep(.article-import-dialog .el-dialog__header) {
    margin-right: 0;
    padding: 18px 24px 10px;
    border-bottom: 1px solid #ebeef5;
}

:deep(.article-import-dialog .el-dialog__body) {
    padding: 18px 24px 24px;
}

:deep(.article-import-dialog .el-dialog__headerbtn) {
    top: 18px;
    right: 18px;
}
</style>
