<template>
    <div class="min-h-screen bg-[#f3f5f7]">
        <Header />

        <main class="mx-auto max-w-[1280px] px-3 py-4 lg:px-4 lg:py-5">
            <div class="grid gap-4 lg:grid-cols-[minmax(0,1fr)_252px] xl:grid-cols-[minmax(0,1fr)_300px]">
                <section class="rounded-[28px] border border-slate-200 bg-white p-5 shadow-sm lg:p-7">
                    <div class="rounded-[24px] border border-slate-200 bg-white p-5">
                        <div class="text-[18px] font-semibold text-slate-900">标签</div>

                        <div v-if="filteredTagList.length > 0" class="mt-4 flex flex-wrap gap-3">
                            <button
                                v-for="item in filteredTagList"
                                :key="item.id"
                                type="button"
                                class="rounded-full px-4 py-2 text-sm font-medium transition"
                                :class="String(activeTagId) === String(item.id)
                                    ? 'bg-[#b9eee0] text-[#1f5f55] shadow-sm'
                                    : 'bg-[#e7f7f1] text-[#5f8e83] hover:bg-[#d6f2ea]'"
                                @click="selectTag(item)"
                            >
                                {{ item.name }}
                            </button>
                        </div>

                        <div v-else class="mt-4 rounded-2xl border border-dashed border-slate-200 bg-slate-50 px-8 py-12 text-center text-slate-400">
                            暂无标签
                        </div>
                    </div>

                    <section class="mt-8">
                        <div class="mb-4 flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                            <div>
                                <h2 class="text-[22px] font-semibold text-slate-900">
                                    {{ activeTag ? `#${activeTag.name} 相关文章` : '标签文章列表' }}
                                </h2>
                                <p class="mt-1 text-sm text-slate-400">
                                    {{ activeTag ? `共 ${total} 篇文章` : '请选择一个标签查看文章' }}
                                </p>
                            </div>

                            <div class="flex gap-3">
                                <el-input
                                    v-model.trim="keyword"
                                    clearable
                                    placeholder="请输入标签关键词"
                                    class="md:w-[260px]"
                                />
                                <el-button v-if="keyword" @click="keyword = ''">清空</el-button>
                            </div>
                        </div>

                        <div
                            v-if="!activeTag"
                            class="rounded-2xl border border-dashed border-slate-200 bg-slate-50 px-8 py-20 text-center text-slate-400"
                        >
                            请选择一个标签查看文章
                        </div>

                        <div
                            v-else-if="!articleLoading && articleList.length === 0"
                            class="rounded-2xl border border-dashed border-slate-200 bg-slate-50 px-8 py-20 text-center text-slate-400"
                        >
                            当前标签下还没有文章
                        </div>

                        <div v-else v-loading="articleLoading" class="grid gap-4 md:grid-cols-2">
                            <ArticleListCard
                                v-for="article in articleList"
                                :key="article.id"
                                :article="article"
                            />
                        </div>

                        <div class="mt-6 flex justify-center" v-if="activeTag && total > pageSize">
                            <el-pagination
                                v-model:current-page="currentPage"
                                v-model:page-size="pageSize"
                                :background="true"
                                layout="prev, pager, next"
                                :total="total"
                                @current-change="loadTagArticlePage"
                            />
                        </div>
                    </section>
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
                            @select="goCategoryPage"
                        />

                        <CategoryTagCard
                            title="标签"
                            :items="filteredTagList"
                            type="tag"
                            prefix="#"
                            empty-text="暂无标签"
                            @select="selectTag"
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
import { getTagArticlePageList } from '@/api/frontend/tag'
import { showMessage } from '@/composables/util'
import { useBlogStore } from '@/stores/blog'

const route = useRoute()
const router = useRouter()
const blogStore = useBlogStore()

const keyword = ref('')
const activeTagId = ref(null)
const articleLoading = ref(false)
const articleList = ref([])
const currentPage = ref(1)
const pageSize = ref(6)
const total = ref(0)

const tagList = computed(() => blogStore.tagList)
const categoryList = computed(() => blogStore.categoryList)

const filteredTagList = computed(() => {
    const searchText = keyword.value.trim().toLowerCase()

    if (!searchText) {
        return tagList.value
    }

    return tagList.value.filter(item => String(item.name || '').toLowerCase().includes(searchText))
})

const activeTag = computed(() => {
    return tagList.value.find(item => String(item.id) === String(activeTagId.value)) || null
})

const visitTotal = computed(() => {
    return 0
})

const loadTagArticlePage = async () => {
    if (!activeTagId.value) {
        articleList.value = []
        total.value = 0
        return
    }

    articleLoading.value = true
    try {
        const res = await getTagArticlePageList({
            tagId: Number(activeTagId.value),
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
        showMessage(res.message || '获取标签文章列表失败', 'error')
    } catch (error) {
        articleList.value = []
        total.value = 0
        showMessage('获取标签文章列表失败', 'error')
    } finally {
        articleLoading.value = false
    }
}

const syncTagFromRoute = async () => {
    if (tagList.value.length === 0) {
        activeTagId.value = null
        articleList.value = []
        total.value = 0
        return
    }

    const routeTagId = route.query.id ? Number(route.query.id) : null
    const matchedTag = tagList.value.find(item => Number(item.id) === routeTagId) || tagList.value[0]
    const nextTagId = matchedTag?.id ?? null

    if (!nextTagId) {
        activeTagId.value = null
        articleList.value = []
        total.value = 0
        return
    }

    if (String(route.query.id || '') !== String(nextTagId)) {
        activeTagId.value = nextTagId
        currentPage.value = 1
        await router.replace({
            path: '/tag',
            query: { id: String(nextTagId) },
        })
        return
    }

    const shouldResetPage = String(activeTagId.value || '') !== String(nextTagId)
    activeTagId.value = nextTagId

    if (shouldResetPage) {
        currentPage.value = 1
    }

    await loadTagArticlePage()
}

const selectTag = async (item) => {
    if (!item?.id) {
        return
    }

    if (String(activeTagId.value) === String(item.id)) {
        return
    }

    await router.push({
        path: '/tag',
        query: { id: String(item.id) },
    })
}

const goCategoryPage = (item) => {
    if (!item?.id) {
        return
    }

    router.push({
        path: '/category',
        query: { id: String(item.id) },
    })
}

watch(() => route.query.id, () => {
    syncTagFromRoute()
})

onMounted(async () => {
    await Promise.allSettled([
        blogStore.fetchBlogSettings(),
        blogStore.fetchTaxonomy(),
    ])

    await syncTagFromRoute()
})
</script>
