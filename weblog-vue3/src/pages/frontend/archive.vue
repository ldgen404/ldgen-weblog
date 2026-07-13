<template>
    <div class="min-h-screen bg-[#f3f5f7]">
        <Header />

        <main class="mx-auto max-w-[1280px] px-3 py-4 lg:px-4 lg:py-5">
            <div class="grid gap-4 lg:grid-cols-[minmax(0,1fr)_252px] xl:grid-cols-[minmax(0,1fr)_300px]">
                <section class="rounded-[28px] border border-slate-200 bg-white p-5 shadow-sm lg:p-7" v-loading="archiveLoading">
                    <div class="flex flex-col gap-4 rounded-[24px] bg-gradient-to-r from-[#eff6ff] via-[#f8fbff] to-[#f3faf7] p-5 lg:flex-row lg:items-end lg:justify-between">
                        <div>
                            <div class="inline-flex items-center rounded-full bg-white/80 px-4 py-1 text-sm font-medium text-[#2563eb]">
                                文章归档
                            </div>
                            <h1 class="mt-4 text-[28px] font-semibold tracking-tight text-slate-900">按时间沉淀所有内容</h1>
                            <p class="mt-3 max-w-2xl text-sm leading-7 text-slate-500 lg:text-base">
                                这里汇总博客中所有已发布文章，按照月份归档展示，便于快速回顾我的大模型应用、Java 底层以及学习复盘内容。
                            </p>
                        </div>

                        <div class="grid grid-cols-2 gap-3 sm:grid-cols-3">
                            <div class="rounded-2xl bg-white px-5 py-4 text-center shadow-sm">
                                <div class="text-2xl font-semibold text-slate-900">{{ total }}</div>
                                <div class="mt-1 text-xs text-slate-400">文章总数</div>
                            </div>
                            <div class="rounded-2xl bg-white px-5 py-4 text-center shadow-sm">
                                <div class="text-2xl font-semibold text-slate-900">{{ archiveGroups.length }}</div>
                                <div class="mt-1 text-xs text-slate-400">当前月份组</div>
                            </div>
                            <div class="rounded-2xl bg-white px-5 py-4 text-center shadow-sm sm:col-span-1 col-span-2">
                                <div class="text-2xl font-semibold text-slate-900">{{ currentPage }}</div>
                                <div class="mt-1 text-xs text-slate-400">当前页码</div>
                            </div>
                        </div>
                    </div>

                    <div
                        v-if="!archiveLoading && archiveGroups.length === 0"
                        class="mt-6 rounded-2xl border border-dashed border-slate-200 bg-slate-50 px-8 py-20 text-center text-slate-400"
                    >
                        暂时还没有归档内容
                    </div>

                    <div v-else class="relative mt-8 pl-0 sm:pl-4">
                        <div class="absolute bottom-0 left-5 top-0 hidden w-px bg-slate-200 sm:block"></div>

                        <section
                            v-for="(group, groupIndex) in archiveGroups"
                            :key="getMonthKey(group.month)"
                            class="relative pb-8 last:pb-0"
                        >
                            <div class="absolute left-4 top-2 hidden h-3 w-3 -translate-x-1/2 rounded-full bg-emerald-400 ring-4 ring-emerald-50 sm:block"></div>

                            <div class="sm:pl-10">
                                <div class="mb-4 flex items-end justify-between gap-3">
                                    <div>
                                        <h2 class="text-[22px] font-semibold text-slate-900">
                                            {{ formatArchiveMonth(group.month) }}
                                        </h2>
                                        <p class="mt-1 text-sm text-slate-400">
                                            共 {{ group.articles?.length || 0 }} 篇文章
                                        </p>
                                    </div>
                                </div>

                                <div class="space-y-3">
                                    <button
                                        v-for="(article, articleIndex) in group.articles"
                                        :key="article.id"
                                        type="button"
                                        class="flex w-full items-center gap-4 rounded-2xl border border-slate-200 bg-white px-4 py-4 text-left transition hover:-translate-y-0.5 hover:border-blue-200 hover:bg-[#f8fbff]"
                                        @click="openArticle(article)"
                                    >
                                        <div class="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-[#eff6ff] text-sm font-semibold text-[#2563eb]">
                                            {{ getArticleOrder(groupIndex, articleIndex) }}
                                        </div>

                                        <div class="flex h-14 w-14 shrink-0 flex-col items-center justify-center rounded-2xl bg-slate-50 text-slate-500">
                                            <div class="text-lg font-semibold leading-none">
                                                {{ getDayText(article.createDate) }}
                                            </div>
                                            <div class="mt-1 text-[11px]">
                                                {{ getMonthDayText(article.createDate) }}
                                            </div>
                                        </div>

                                        <img
                                            v-if="article.cover"
                                            :src="article.cover"
                                            :alt="article.title"
                                            class="hidden h-14 w-20 rounded-xl object-cover md:block"
                                        />
                                        <div
                                            v-else
                                            class="hidden h-14 w-20 items-center justify-center rounded-xl bg-gradient-to-br from-[#eff6ff] to-[#ecfeff] text-xs font-medium text-slate-400 md:flex"
                                        >
                                            NO COVER
                                        </div>

                                        <div class="min-w-0 flex-1">
                                            <div class="truncate text-[17px] font-medium text-slate-900">
                                                {{ article.title || '未命名文章' }}
                                            </div>
                                            <div class="mt-2 flex flex-wrap items-center gap-3 text-sm text-slate-400">
                                                <span>{{ formatFullDate(article.createDate) }}</span>
                                                <span class="hidden md:inline">归档收录</span>
                                            </div>
                                        </div>

                                        <el-icon class="shrink-0 text-slate-300" :size="18"><ArrowRight /></el-icon>
                                    </button>
                                </div>
                            </div>
                        </section>
                    </div>

                    <div class="mt-8 flex justify-center" v-if="total > pageSize">
                        <el-pagination
                            v-model:current-page="currentPage"
                            v-model:page-size="pageSize"
                            :background="true"
                            layout="prev, pager, next"
                            :total="total"
                            @current-change="loadArchivePage"
                        />
                    </div>
                </section>

                <aside class="space-y-4">
                    <div class="space-y-4 lg:sticky lg:top-[92px]">
                        <BloggerInfoCard
                            :article-total="total"
                            :category-total="categoryList.length"
                            :tag-total="tagList.length"
                            :visit-total="visitTotal"
                        />

                        <CategoryTagCard
                            title="分类"
                            :items="categoryList"
                            empty-text="暂无分类"
                            @select="showPending"
                        />

                        <CategoryTagCard
                            title="标签"
                            :items="tagList"
                            type="tag"
                            prefix="#"
                            empty-text="暂无标签"
                            @select="showPending"
                        />
                    </div>
                </aside>
            </div>
        </main>

        <Footer />
    </div>
</template>

<script setup>
import { ArrowRight } from '@element-plus/icons-vue'
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import Header from '@/components/frontend/Header.vue'
import Footer from '@/components/frontend/Footer.vue'
import BloggerInfoCard from '@/components/frontend/BloggerInfoCard.vue'
import CategoryTagCard from '@/components/frontend/CategoryTagCard.vue'
import { getArchiveArticlePageList } from '@/api/frontend/archive'
import { showMessage } from '@/composables/util'
import { useBlogStore } from '@/stores/blog'

const blogStore = useBlogStore()
const router = useRouter()

const archiveLoading = ref(false)
const archiveGroups = ref([])
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

const categoryList = computed(() => blogStore.categoryList)
const tagList = computed(() => blogStore.tagList)

const visitTotal = computed(() => Number(blogStore.blogSettings?.pvTotalCount || 0))

const getDateParts = (value) => {
    if (Array.isArray(value)) {
        return value.map(item => Number(item))
    }

    if (typeof value === 'string') {
        return value
            .split(/[^0-9]/)
            .filter(Boolean)
            .map(item => Number(item))
    }

    if (value && typeof value === 'object') {
        const year = Number(value.year)
        const month = Number(value.monthValue || value.month)
        const day = Number(value.dayOfMonth || value.day)
        return [year, month, day].filter(item => !Number.isNaN(item) && item > 0)
    }

    return []
}

const padText = (value) => String(value).padStart(2, '0')

const formatArchiveMonth = (value) => {
    const [year, month] = getDateParts(value)

    if (!year || !month) {
        return '未知时间'
    }

    return `${year}年${month}月`
}

const formatFullDate = (value) => {
    const [year, month, day] = getDateParts(value)

    if (!year || !month || !day) {
        return '日期未知'
    }

    return `${year}-${padText(month)}-${padText(day)}`
}

const getDayText = (value) => {
    const [, , day] = getDateParts(value)
    return day ? padText(day) : '--'
}

const getMonthDayText = (value) => {
    const [, month] = getDateParts(value)
    return month ? `${padText(month)}月` : '--'
}

const getMonthKey = (value) => {
    const [year, month] = getDateParts(value)
    return `${year || '0'}-${month || '0'}`
}

const getArticleOrder = (groupIndex, articleIndex) => {
    const pageOffset = (currentPage.value - 1) * pageSize.value
    const previousCount = archiveGroups.value
        .slice(0, groupIndex)
        .reduce((totalCount, group) => totalCount + (group.articles?.length || 0), 0)

    return pageOffset + previousCount + articleIndex + 1
}

const loadArchivePage = async () => {
    archiveLoading.value = true
    try {
        const res = await getArchiveArticlePageList({
            pageNum: currentPage.value,
            pageSize: pageSize.value,
        })

        if (res.code === 0 && res.data) {
            archiveGroups.value = res.data.records || []
            currentPage.value = res.data.pageNumber ?? res.data.pageNum ?? currentPage.value
            total.value = res.data.totalRow ?? res.data.total ?? 0
            return
        }

        archiveGroups.value = []
        total.value = 0
        showMessage(res.message || '获取归档列表失败', 'error')
    } catch (error) {
        archiveGroups.value = []
        total.value = 0
        showMessage('获取归档列表失败', 'error')
    } finally {
        archiveLoading.value = false
    }
}

const openArticle = (article) => {
    if (!article?.id) {
        return
    }

    router.push({
        path: `/article/${article.id}`,
    })
}

const showPending = (item) => {
    showMessage(`${item.name}页面开发中`, 'info')
}

onMounted(async () => {
    await Promise.allSettled([
        blogStore.fetchBlogSettings(),
        blogStore.fetchTaxonomy(),
        loadArchivePage(),
    ])
})
</script>
