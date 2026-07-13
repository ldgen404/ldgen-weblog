<template>
    <div class="min-h-screen bg-[#f6f7f9]">
        <Header />

        <main class="mx-auto max-w-[1380px] px-3 py-5 lg:px-5 lg:py-7">
            <div class="grid gap-5 lg:grid-cols-[minmax(0,1fr)_280px] xl:grid-cols-[minmax(0,1fr)_320px]">
                <section
                    class="rounded-[22px] border border-[#e9eaec] bg-white px-5 py-6 shadow-[0_10px_30px_rgba(15,23,42,0.03)] lg:px-8 lg:py-8"
                    v-loading="detailLoading"
                >
                    <div
                        v-if="articleDetail"
                        class="overflow-hidden"
                    >
                        <div class="text-xs text-slate-400">首页 / 正文</div>

                        <div class="mt-5 flex flex-wrap gap-2">
                            <button
                                v-for="tag in articleDetail.tags || []"
                                :key="tag.id"
                                type="button"
                                class="rounded-full bg-[#e4f5ea] px-3.5 py-1.5 text-[14px] font-medium text-[#48675b] transition hover:bg-[#d4ebdd]"
                                @click="goTag(tag)"
                            >
                                # {{ tag.name }}
                            </button>
                        </div>

                        <h1 class="mt-7 max-w-[980px] text-[36px] font-bold leading-[1.22] tracking-[-0.02em] text-[#444] lg:text-[56px]">
                            {{ articleDetail.title || '未命名文章' }}
                        </h1>

                        <div class="mt-6 flex flex-wrap items-center gap-x-7 gap-y-3 text-[15px] text-[#8c8c8c]">
                            <span class="inline-flex items-center gap-2">
                                <el-icon><Tickets /></el-icon>
                                {{ articleDetail.wordCount || 0 }}
                            </span>
                            <span class="inline-flex items-center gap-2">
                                <el-icon><Calendar /></el-icon>
                                {{ formatDateTime(articleDetail.createTime) }}
                            </span>
                            <span class="inline-flex items-center gap-2">
                                <el-icon><Tickets /></el-icon>
                                {{ articleDetail.category?.name || '未分类' }}
                            </span>
                            <span class="inline-flex items-center gap-2">
                                <el-icon><View /></el-icon>
                                {{ articleDetail.readNum || 0 }}
                            </span>
                        </div>

                        <p v-if="articleDetail.summary" class="mt-8 max-w-[980px] text-[17px] leading-9 text-[#6b7280]">
                            {{ articleDetail.summary }}
                        </p>

                        <div class="mt-10 rounded-[20px] border border-[#ededed] bg-[#f7f7f7] p-4 md:p-5">
                            <img
                                :src="coverUrl"
                                :alt="articleDetail.title || '文章封面'"
                                class="h-[240px] w-full rounded-[16px] object-cover md:h-[420px]"
                            />
                        </div>

                        <div
                            :key="articleDetail.id"
                            ref="articleContentRef"
                            v-viewer="viewerOptions"
                            class="article-content mt-10"
                            v-html="articleDetail.contentHtml || ''"
                        ></div>

                        <div class="mt-12 grid gap-4 md:grid-cols-2">
                            <button
                                type="button"
                                class="flex min-h-[96px] flex-col justify-center rounded-[16px] border px-5 py-4 text-left transition"
                                :class="articleDetail.preArticle
                                    ? 'border-[#e8edf3] bg-[#fafafa] hover:border-[#d8dee6] hover:bg-white'
                                    : 'cursor-not-allowed border-[#ececec] bg-[#fafafa] text-slate-300'"
                                @click="goAdjacentArticle(articleDetail.preArticle)"
                            >
                                <div class="flex items-center gap-2 text-sm font-medium" :class="articleDetail.preArticle ? 'text-slate-500' : 'text-slate-300'">
                                    <el-icon><ArrowLeft /></el-icon>
                                    上一篇
                                </div>
                                <div class="mt-2 line-clamp-2 text-[15px] font-medium" :class="articleDetail.preArticle ? 'text-slate-800' : 'text-slate-300'">
                                    {{ articleDetail.preArticle?.title || '已经是第一篇了' }}
                                </div>
                            </button>

                            <button
                                type="button"
                                class="flex min-h-[96px] flex-col justify-center rounded-[16px] border px-5 py-4 text-right transition"
                                :class="articleDetail.nextArticle
                                    ? 'border-[#e8edf3] bg-[#fafafa] hover:border-[#d8dee6] hover:bg-white'
                                    : 'cursor-not-allowed border-[#ececec] bg-[#fafafa] text-slate-300'"
                                @click="goAdjacentArticle(articleDetail.nextArticle)"
                            >
                                <div class="flex items-center justify-end gap-2 text-sm font-medium" :class="articleDetail.nextArticle ? 'text-slate-500' : 'text-slate-300'">
                                    下一篇
                                    <el-icon><ArrowRight /></el-icon>
                                </div>
                                <div class="mt-2 line-clamp-2 text-[15px] font-medium" :class="articleDetail.nextArticle ? 'text-slate-800' : 'text-slate-300'">
                                    {{ articleDetail.nextArticle?.title || '已经是最后一篇了' }}
                                </div>
                            </button>
                        </div>
                    </div>

                    <div
                        v-else-if="!detailLoading"
                        class="rounded-2xl border border-dashed border-slate-200 bg-slate-50 px-8 py-20 text-center text-slate-400"
                    >
                        文章不存在或已被删除
                    </div>
                </section>

                <aside class="space-y-3">
                    <BloggerInfoCard
                        :article-total="0"
                        :category-total="categoryList.length"
                        :tag-total="tagList.length"
                        :visit-total="visitTotal"
                    />

                    <CategoryTagCard
                        title="分类"
                        :items="categoryList"
                        empty-text="暂无分类"
                        @select="goCategory"
                    />

                    <CategoryTagCard
                        title="标签"
                        :items="tagList"
                        type="tag"
                        prefix="#"
                        empty-text="暂无标签"
                        @select="goTag"
                    />

                    <TocCard
                        class="lg:sticky lg:top-[88px]"
                        :items="tocItems"
                        :active-id="activeHeadingId"
                        @select="scrollToHeading"
                    />
                </aside>
            </div>
        </main>

        <Footer />
    </div>
</template>

<script setup>
import developerImage from '@/assets/developer.png'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import { ArrowLeft, ArrowRight, Calendar, Tickets, View } from '@element-plus/icons-vue'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Header from '@/components/frontend/Header.vue'
import Footer from '@/components/frontend/Footer.vue'
import BloggerInfoCard from '@/components/frontend/BloggerInfoCard.vue'
import CategoryTagCard from '@/components/frontend/CategoryTagCard.vue'
import TocCard from '@/components/frontend/TocCard.vue'
import { getFrontendArticleDetail } from '@/api/frontend/article'
import { showMessage } from '@/composables/util'
import { useBlogStore } from '@/stores/blog'

const route = useRoute()
const router = useRouter()
const blogStore = useBlogStore()

const detailLoading = ref(false)
const articleDetail = ref(null)
const articleContentRef = ref(null)
const tocItems = ref([])
const activeHeadingId = ref('')
let scrollSyncRafId = 0

const categoryList = computed(() => blogStore.categoryList)
const tagList = computed(() => blogStore.tagList)
const visitTotal = computed(() => Number(blogStore.blogSettings?.pvTotalCount || 0))
const coverUrl = computed(() => articleDetail.value?.cover || developerImage)
const viewerOptions = {
    toolbar: true,
    navbar: false,
    title: false,
    transition: true,
    keyboard: true,
}

const formatDateTime = (value) => {
    if (!value) {
        return '未知时间'
    }

    const date = new Date(value)
    if (Number.isNaN(date.getTime())) {
        return value
    }

    return `${date.getFullYear()}-${`${date.getMonth() + 1}`.padStart(2, '0')}-${`${date.getDate()}`.padStart(2, '0')} ${`${date.getHours()}`.padStart(2, '0')}:${`${date.getMinutes()}`.padStart(2, '0')}:${`${date.getSeconds()}`.padStart(2, '0')}`
}

const loadArticleDetail = async () => {
    const articleId = Number(route.params.id)
    if (!articleId) {
        router.replace('/404')
        return
    }

    detailLoading.value = true
    try {
        const res = await getFrontendArticleDetail(articleId)
        if (res.code === 0 && res.data) {
            articleDetail.value = res.data
            blogStore.fetchBlogSettings(true).catch(() => {})
            await nextTick()
            enhanceArticleContent()
            return
        }

        articleDetail.value = null
        router.replace('/404')
    } catch (error) {
        articleDetail.value = null
        if (error?.response?.status === 404) {
            router.replace('/404')
            return
        }

        showMessage('获取文章详情失败', 'error')
    } finally {
        detailLoading.value = false
    }
}

const enhanceArticleContent = () => {
    const container = articleContentRef.value
    if (!container) {
        return
    }

    const codeBlocks = container.querySelectorAll('pre code')
    codeBlocks.forEach((block) => {
        const rawCode = block.textContent || ''
        hljs.highlightElement(block)

        const className = block.className || ''
        const languageMatch = className.match(/language-([\w-]+)/)
        const language = languageMatch?.[1] || ''
        const pre = block.parentElement

        if (pre) {
            pre.dataset.language = language || 'code'

            if (!pre.querySelector('.code-toolbar')) {
                const toolbar = document.createElement('div')
                toolbar.className = 'code-toolbar'

                const collapseButton = document.createElement('button')
                collapseButton.type = 'button'
                collapseButton.className = 'code-toolbar__collapse'
                collapseButton.setAttribute('aria-label', '折叠代码块')
                collapseButton.innerHTML = `
                    <span class="code-toolbar__arrow"></span>
                    <span class="code-toolbar__language">${pre.dataset.language}</span>
                `
                collapseButton.addEventListener('click', () => {
                    pre.classList.toggle('is-collapsed')
                })

                const copyButton = document.createElement('button')
                copyButton.type = 'button'
                copyButton.className = 'code-toolbar__copy'
                copyButton.setAttribute('aria-label', '复制代码')
                copyButton.addEventListener('click', async () => {
                    try {
                        await navigator.clipboard.writeText(rawCode)
                        showMessage('代码已复制')
                    } catch (error) {
                        showMessage('复制失败，请手动复制', 'error')
                    }
                })

                toolbar.appendChild(collapseButton)
                toolbar.appendChild(copyButton)
                pre.prepend(toolbar)
            }
        }
    })

    const links = container.querySelectorAll('a')
    links.forEach((link) => {
        link.setAttribute('target', '_blank')
        link.setAttribute('rel', 'noreferrer noopener')
    })

    const images = container.querySelectorAll('img')
    images.forEach((image) => {
        image.classList.add('zoomable-image')
    })

    buildToc()
    scheduleSyncActiveHeading()
}

const buildToc = () => {
    const container = articleContentRef.value
    if (!container) {
        tocItems.value = []
        activeHeadingId.value = ''
        return
    }

    const headings = Array.from(container.querySelectorAll('h2, h3'))
    tocItems.value = headings.map((heading, index) => {
        const id = `toc-heading-${index + 1}`
        heading.id = id

        return {
            id,
            text: heading.textContent?.trim() || `标题 ${index + 1}`,
            level: Number(heading.tagName?.replace('H', '') || 2),
        }
    })

    activeHeadingId.value = tocItems.value[0]?.id || ''
}

const syncActiveHeading = () => {
    const container = articleContentRef.value
    if (!container || tocItems.value.length === 0) {
        activeHeadingId.value = ''
        return
    }

    const headings = tocItems.value
        .map(item => document.getElementById(item.id))
        .filter(Boolean)

    if (headings.length === 0) {
        activeHeadingId.value = ''
        return
    }

    const activationOffset = 140
    let currentId = tocItems.value[0].id

    headings.forEach((heading) => {
        if (heading.getBoundingClientRect().top <= activationOffset) {
            currentId = heading.id
        }
    })

    activeHeadingId.value = currentId
}

const scheduleSyncActiveHeading = () => {
    if (scrollSyncRafId) {
        return
    }

    scrollSyncRafId = window.requestAnimationFrame(() => {
        scrollSyncRafId = 0
        syncActiveHeading()
    })
}

const scrollToHeading = (item) => {
    const target = document.getElementById(item?.id || '')
    if (!target) {
        return
    }

    activeHeadingId.value = item.id
    window.scrollTo({
        top: target.getBoundingClientRect().top + window.scrollY - 96,
        behavior: 'smooth',
    })
}

const goCategory = (item) => {
    if (!item?.id) {
        return
    }

    router.push({
        path: '/category',
        query: { id: String(item.id) },
    })
}

const goTag = (item) => {
    if (!item?.id) {
        return
    }

    router.push({
        path: '/tag',
        query: { id: String(item.id) },
    })
}

const goAdjacentArticle = (item) => {
    if (!item?.id) {
        return
    }

    router.push({
        path: `/article/${item.id}`,
    })
}

watch(() => route.params.id, () => {
    loadArticleDetail()
})

onMounted(async () => {
    await Promise.allSettled([
        blogStore.fetchBlogSettings(),
        blogStore.fetchTaxonomy(),
    ])

    await loadArticleDetail()
    window.addEventListener('scroll', scheduleSyncActiveHeading, { passive: true })
})

onBeforeUnmount(() => {
    window.removeEventListener('scroll', scheduleSyncActiveHeading)
    if (scrollSyncRafId) {
        window.cancelAnimationFrame(scrollSyncRafId)
        scrollSyncRafId = 0
    }
})
</script>

<style scoped>
.article-content {
    color: #4b5563;
}

.article-content :deep(h1),
.article-content :deep(h2),
.article-content :deep(h3),
.article-content :deep(h4),
.article-content :deep(h5),
.article-content :deep(h6) {
    color: #1f2937;
    font-weight: 700;
    letter-spacing: -0.02em;
    line-height: 1.4;
    margin: 1.9em 0 0.95em;
}

.article-content :deep(h1) {
    font-size: 2.15rem;
}

.article-content :deep(h2) {
    font-size: 2rem;
}

.article-content :deep(h3) {
    font-size: 1.55rem;
}

.article-content :deep(h4) {
    font-size: 1.2rem;
}

.article-content :deep(p),
.article-content :deep(li),
.article-content :deep(blockquote) {
    color: #4b5563;
    font-size: 17px;
    line-height: 2;
}

.article-content :deep(p) {
    margin: 1.1em 0;
}

.article-content :deep(a) {
    color: #2563eb;
    text-decoration: none;
    word-break: break-word;
}

.article-content :deep(a:hover) {
    text-decoration: underline;
}

.article-content :deep(img) {
    border-radius: 18px;
    box-shadow: 0 10px 24px rgba(15, 23, 42, 0.06);
    cursor: zoom-in;
    display: block;
    margin: 1.8rem 0;
    max-width: 100%;
}

.article-content :deep(pre) {
    background: #f8fafc;
    border: 1px solid #e5e7eb;
    border-radius: 16px;
    color: #1f2937;
    margin: 1.5rem 0;
    overflow-x: auto;
    padding: 0;
    position: relative;
}

.article-content :deep(pre.is-collapsed) {
    max-height: 56px;
    overflow: hidden;
}

.article-content :deep(.code-toolbar) {
    align-items: center;
    background: #f3f4f6;
    border-bottom: 1px solid #e5e7eb;
    border-radius: 16px 16px 0 0;
    display: flex;
    justify-content: space-between;
    min-height: 56px;
    padding: 0 18px;
    position: relative;
    z-index: 1;
}

.article-content :deep(.code-toolbar__collapse),
.article-content :deep(.code-toolbar__copy) {
    align-items: center;
    background: transparent;
    border: 0;
    cursor: pointer;
    display: inline-flex;
    padding: 0;
}

.article-content :deep(.code-toolbar__collapse) {
    color: #111827;
    gap: 12px;
}

.article-content :deep(.code-toolbar__collapse:hover) {
    color: #000;
}

.article-content :deep(.code-toolbar__arrow) {
    border-bottom: 7px solid transparent;
    border-left: 10px solid #737373;
    border-top: 7px solid transparent;
    display: inline-block;
    height: 0;
    transform: rotate(90deg);
    transition: transform 0.2s ease;
    width: 0;
}

.article-content :deep(pre.is-collapsed .code-toolbar__arrow) {
    transform: rotate(0deg);
}

.article-content :deep(.code-toolbar__language) {
    font-size: 15px;
    font-weight: 500;
    text-transform: lowercase;
}

.article-content :deep(.code-toolbar__copy) {
    color: #a3a3a3;
    font-size: 15px;
    font-weight: 500;
    transition: color 0.2s ease;
}

.article-content :deep(.code-toolbar__copy::before) {
    content: '复制代码';
}

.article-content :deep(.code-toolbar__copy:hover) {
    color: #6b7280;
}

.article-content :deep(pre.is-collapsed code) {
    display: none;
    pointer-events: none;
}

.article-content :deep(code) {
    background: #f1f5f9;
    border-radius: 6px;
    color: #0f172a;
    font-size: 0.92em;
    padding: 0.15rem 0.35rem;
}

.article-content :deep(pre code) {
    background: transparent;
    color: inherit;
    display: block;
    font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
    font-size: 14px;
    line-height: 1.9;
    overflow-x: auto;
    padding: 20px 24px 22px;
}

.article-content :deep(blockquote) {
    background: #f8fafc;
    border-left: 4px solid #94a3b8;
    border-radius: 16px;
    margin: 1.5rem 0;
    padding: 1rem 1.3rem;
}

.article-content :deep(table) {
    border-collapse: collapse;
    margin: 1.5rem 0;
    overflow: hidden;
    width: 100%;
}

.article-content :deep(th),
.article-content :deep(td) {
    border: 1px solid #e2e8f0;
    padding: 0.8rem 1rem;
    text-align: left;
}

.article-content :deep(th) {
    background: #f8fafc;
    color: #0f172a;
}

.article-content :deep(thead tr) {
    background: #f8fafc;
}

.article-content :deep(ul),
.article-content :deep(ol) {
    margin: 1.1rem 0 1.1rem 1.4rem;
}

.article-content :deep(li + li) {
    margin-top: 0.45rem;
}

.article-content :deep(hr) {
    border: 0;
    border-top: 1px solid #e2e8f0;
    margin: 2rem 0;
}

.article-content :deep(strong) {
    color: #0f172a;
    font-weight: 700;
}

.article-content :deep(em) {
    color: #475569;
    font-style: italic;
}

.article-content :deep(.zoomable-image) {
    cursor: zoom-in;
}
</style>
