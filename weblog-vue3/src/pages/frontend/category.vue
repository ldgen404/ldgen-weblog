<template>
    <div class="min-h-screen bg-[#f3f5f7]">
        <Header />

        <main class="mx-auto max-w-[1280px] px-3 py-4 lg:px-4 lg:py-5">
            <div class="grid gap-4 lg:grid-cols-[minmax(0,1fr)_252px] xl:grid-cols-[minmax(0,1fr)_300px]">
                <section class="rounded-[28px] border border-slate-200 bg-white p-5 shadow-sm lg:p-7">
                    <div class="rounded-[24px] bg-gradient-to-r from-[#eff6ff] via-[#f8fbff] to-[#f3faf7] p-5">
                        <div class="inline-flex items-center rounded-full bg-white/80 px-4 py-1 text-sm font-medium text-[#2563eb]">
                            文章分类
                        </div>
                        <h1 class="mt-4 text-[28px] font-semibold tracking-tight text-slate-900">
                            {{ activeCategory ? `${activeCategory.name} 分类文章` : '分类文章列表' }}
                        </h1>
                        <p class="mt-3 max-w-2xl text-sm leading-7 text-slate-500 lg:text-base">
                            按分类查看博客内容，聚合大模型应用、Java 底层、工程实践和学习复盘等文章，方便按主题快速阅读。
                        </p>

                        <div class="mt-5 grid grid-cols-2 gap-3 sm:grid-cols-3">
                            <div class="rounded-2xl bg-white px-5 py-4 text-center shadow-sm">
                                <div class="text-2xl font-semibold text-slate-900">{{ categoryList.length }}</div>
                                <div class="mt-1 text-xs text-slate-400">分类总数</div>
                            </div>
                            <div class="rounded-2xl bg-white px-5 py-4 text-center shadow-sm">
                                <div class="text-2xl font-semibold text-slate-900">{{ total }}</div>
                                <div class="mt-1 text-xs text-slate-400">当前分类文章</div>
                            </div>
                            <div class="rounded-2xl bg-white px-5 py-4 text-center shadow-sm sm:col-span-1 col-span-2">
                                <div class="text-2xl font-semibold text-slate-900">{{ currentPage }}</div>
                                <div class="mt-1 text-xs text-slate-400">当前页码</div>
                            </div>
                        </div>
                    </div>

                    <div class="mt-6">
                        <div class="mb-3 text-[15px] font-medium text-slate-700">全部分类</div>
                        <div v-if="categoryList.length > 0" class="flex flex-wrap gap-3">
                            <button
                                v-for="item in categoryList"
                                :key="item.id"
                                type="button"
                                class="rounded-2xl px-4 py-2 text-sm font-medium transition"
                                :class="String(activeCategoryId) === String(item.id)
                                    ? 'bg-blue-600 text-white shadow-sm'
                                    : 'bg-slate-100 text-slate-600 hover:bg-slate-200'"
                                @click="selectCategory(item)"
                            >
                                {{ item.name }}
                            </button>
                        </div>
                        <div v-else class="rounded-2xl border border-dashed border-slate-200 bg-slate-50 px-6 py-10 text-center text-slate-400">
                            暂无分类数据
                        </div>
                    </div>

                    <div v-loading="articleLoading" class="mt-8">
                        <div class="mb-4 flex items-center justify-between gap-3">
                            <div>
                                <h2 class="text-[22px] font-semibold text-slate-900">
                                    {{ activeCategory ? `${activeCategory.name} · 文章列表` : '文章列表' }}
                                </h2>
                                <p class="mt-1 text-sm text-slate-400">
                                    共 {{ total }} 篇文章
                                </p>
                            </div>
                        </div>

                        <div
                            v-if="!articleLoading && categoryList.length > 0 && articleList.length === 0"
                            class="rounded-2xl border border-dashed border-slate-200 bg-slate-50 px-8 py-20 text-center text-slate-400"
                        >
                            当前分类下还没有文章
                        </div>

                        <div v-else class="grid gap-4 md:grid-cols-2">
                            <ArticleListCard
                                v-for="article in articleList"
                                :key="article.id"
                                :article="article"
                            />
                        </div>

                        <div class="mt-6 flex justify-center" v-if="total > pageSize">
                            <el-pagination
                                v-model:current-page="currentPage"
                                v-model:page-size="pageSize"
                                :background="true"
                                layout="prev, pager, next"
                                :total="total"
                                @current-change="loadCategoryArticlePage"
                            />
                        </div>
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
                            @select="selectCategory"
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
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Header from '@/components/frontend/Header.vue'
import Footer from '@/components/frontend/Footer.vue'
import ArticleListCard from '@/components/frontend/ArticleListCard.vue'
import BloggerInfoCard from '@/components/frontend/BloggerInfoCard.vue'
import CategoryTagCard from '@/components/frontend/CategoryTagCard.vue'
import { getCategoryArticlePageList } from '@/api/frontend/category'
import { showMessage } from '@/composables/util'
import { useBlogStore } from '@/stores/blog'

const route = useRoute()
const router = useRouter()
const blogStore = useBlogStore()

const articleLoading = ref(false)
const articleList = ref([])
const currentPage = ref(1)
const pageSize = ref(6)
const total = ref(0)
const activeCategoryId = ref(null)

const categoryList = computed(() => blogStore.categoryList)
const tagList = computed(() => blogStore.tagList)

const activeCategory = computed(() => {
    return categoryList.value.find(item => String(item.id) === String(activeCategoryId.value)) || null
})

const visitTotal = computed(() => Number(blogStore.blogSettings?.pvTotalCount || 0))

const loadCategoryArticlePage = async () => {
    if (!activeCategoryId.value) {
        articleList.value = []
        total.value = 0
        return
    }

    articleLoading.value = true
    try {
        const res = await getCategoryArticlePageList({
            categoryId: Number(activeCategoryId.value),
            pageNum: currentPage.value,
            pageSize: pageSize.value,
        })

        if (res.code === 0 && res.data) {
            articleList.value = res.data.records || []
            currentPage.value = res.data.pageNumber ?? res.data.pageNum ?? currentPage.value
            total.value = res.data.totalRow ?? res.data.total ?? 0
            return
        }

        articleList.value = []
        total.value = 0
        showMessage(res.message || '获取分类文章列表失败', 'error')
    } catch (error) {
        articleList.value = []
        total.value = 0
        showMessage('获取分类文章列表失败', 'error')
    } finally {
        articleLoading.value = false
    }
}

const syncCategoryFromRoute = async () => {
    if (categoryList.value.length === 0) {
        activeCategoryId.value = null
        articleList.value = []
        total.value = 0
        return
    }

    const routeCategoryId = route.query.id ? Number(route.query.id) : null
    const matchedCategory = categoryList.value.find(item => Number(item.id) === routeCategoryId) || categoryList.value[0]
    const nextCategoryId = matchedCategory?.id ?? null

    if (!nextCategoryId) {
        activeCategoryId.value = null
        articleList.value = []
        total.value = 0
        return
    }

    if (String(route.query.id || '') !== String(nextCategoryId)) {
        activeCategoryId.value = nextCategoryId
        currentPage.value = 1
        await router.replace({
            path: '/category',
            query: { id: String(nextCategoryId) },
        })
        return
    }

    const shouldResetPage = String(activeCategoryId.value || '') !== String(nextCategoryId)
    activeCategoryId.value = nextCategoryId

    if (shouldResetPage) {
        currentPage.value = 1
    }

    await loadCategoryArticlePage()
}

const selectCategory = async (item) => {
    if (!item?.id) {
        return
    }

    if (String(activeCategoryId.value) === String(item.id)) {
        return
    }

    await router.push({
        path: '/category',
        query: { id: String(item.id) },
    })
}

const showPending = (item) => {
    showMessage(`${item.name}页面开发中`, 'info')
}

watch(() => route.query.id, () => {
    syncCategoryFromRoute()
})

onMounted(async () => {
    await Promise.allSettled([
        blogStore.fetchBlogSettings(),
        blogStore.fetchTaxonomy(),
    ])

    await syncCategoryFromRoute()
})
</script>
