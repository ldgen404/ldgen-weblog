<template>
    <div class="min-h-screen bg-[#f3f5f7]">
        <Header />

        <main class="mx-auto max-w-[1280px] px-3 py-4 lg:px-4 lg:py-5">
            <div class="grid gap-4 lg:grid-cols-[minmax(0,1fr)_252px] xl:grid-cols-[minmax(0,1fr)_300px]">
                <section>
                    <div
                        v-if="activeTag"
                        class="mb-4 flex items-center justify-between rounded-2xl border border-[#d6f2ea] bg-white px-4 py-3"
                    >
                        <div class="text-sm text-slate-500">
                            当前按标签筛选:
                            <span class="ml-1 rounded-full bg-[#ddf7ef] px-3 py-1 font-medium text-[#285f55]">
                                #{{ activeTag.name }}
                            </span>
                        </div>
                        <button
                            type="button"
                            class="text-sm font-medium text-slate-500 transition hover:text-slate-700"
                            @click="clearActiveTag"
                        >
                            清除筛选
                        </button>
                    </div>

                    <div v-loading="articleLoading" class="grid gap-4 md:grid-cols-2">
                        <ArticleListCard
                            v-for="(article, index) in filteredArticleList"
                            :key="article.id"
                            :article="article"
                            :show-top="index < 2"
                        />

                        <div
                            v-if="!articleLoading && filteredArticleList.length === 0"
                            class="col-span-full rounded-2xl border border-dashed border-slate-200 bg-white px-8 py-20 text-center text-slate-400"
                        >
                            <template v-if="keyword">没有找到和“{{ keyword }}”相关的文章</template>
                            <template v-else-if="activeTag">没有找到和“#{{ activeTag.name }}”相关的文章</template>
                            <template v-else>暂时还没有文章内容</template>
                        </div>
                    </div>

                    <div class="mt-6 flex justify-center" v-if="!keyword && !activeTag && total > pageSize">
                        <el-pagination
                            v-model:current-page="currentPage"
                            v-model:page-size="pageSize"
                            :background="true"
                            layout="prev, pager, next"
                            :total="total"
                            @current-change="loadArticlePage"
                        />
                    </div>
                </section>

                <aside class="space-y-4">
                    <div class="lg:sticky lg:top-[92px] space-y-4">
                        <BloggerInfoCard
                            :article-total="total"
                            :category-total="categoryList.length"
                            :tag-total="tagList.length"
                            :visit-total="visitTotal"
                        />

                       
                        <CategoryTagCard
                            title="分类"
                            :items="categoryCards"
                            empty-text="暂无分类"
                            @select="goCategoryPage"
                        />

                        <CategoryTagCard
                            title="标签"
                            :items="tagCards"
                            type="tag"
                            prefix="#"
                            empty-text="暂无标签"
                            :active-id="activeTagId"
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
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Header from '@/components/frontend/Header.vue'
import Footer from '@/components/frontend/Footer.vue'
import SidebarCard from '@/components/frontend/SidebarCard.vue'
import ArticleListCard from '@/components/frontend/ArticleListCard.vue'
import BloggerInfoCard from '@/components/frontend/BloggerInfoCard.vue'
import CategoryTagCard from '@/components/frontend/CategoryTagCard.vue'
import { getArticlePageList } from '@/api/frontend/article'
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
const activeTagId = ref(null)

const categoryList = computed(() => blogStore.categoryList)
const tagList = computed(() => blogStore.tagList)
const keyword = computed(() => (route.query.keyword || '').trim())
const activeTag = computed(() => {
    return tagCards.value.find(item => String(item.id) === String(activeTagId.value)) || null
})

const filteredArticleList = computed(() => {
    return articleList.value.filter((article) => {
        const searchText = [
            article.title,
            article.summary,
            article.category?.name,
            ...(article.tags || []).map(tag => tag.name),
        ].filter(Boolean).join(' ').toLowerCase()

        const matchesKeyword = !keyword.value || searchText.includes(keyword.value.toLowerCase())
        const matchesTag = !activeTagId.value || (article.tags || []).some(tag => String(tag.id) === String(activeTagId.value))

        return matchesKeyword && matchesTag
    })
})

const visitTotal = computed(() => Number(blogStore.blogSettings?.pvTotalCount || 0))

const categoryCards = computed(() => {
    const countMap = new Map()

    articleList.value.forEach((article) => {
        const category = article.category
        if (!category?.id) {
            return
        }
        countMap.set(category.id, (countMap.get(category.id) || 0) + 1)
    })

    return categoryList.value.map(item => ({
        ...item,
        count: countMap.get(item.id) || 0,
    }))
})

const tagCards = computed(() => {
    const countMap = new Map()

    articleList.value.forEach((article) => {
        ;(article.tags || []).forEach((tag) => {
            if (!tag?.id) {
                return
            }
            countMap.set(tag.id, (countMap.get(tag.id) || 0) + 1)
        })
    })

    return tagList.value.map(item => ({
        ...item,
        count: countMap.get(item.id) || 0,
    }))
})

const loadArticlePage = async () => {
    articleLoading.value = true
    try {
        const res = await getArticlePageList({
            pageNum: currentPage.value,
            pageSize: pageSize.value,
        })

        if (res.code === 0 && res.data) {
            articleList.value = res.data.records || []
            currentPage.value = res.data.pageNumber ?? res.data.pageNum ?? 1
            total.value = res.data.totalRow ?? 0
        }
    } finally {
        articleLoading.value = false
    }
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

const selectTag = (item) => {
    if (!item?.id) {
        return
    }

    if (String(activeTagId.value) === String(item.id)) {
        activeTagId.value = null
        return
    }

    activeTagId.value = item.id
}

const clearActiveTag = () => {
    activeTagId.value = null
}

const showChatroom = () => {
    showMessage('公共聊天室正在开发中', 'info')
}

onMounted(async () => {
    await Promise.allSettled([
        blogStore.fetchBlogSettings(),
        blogStore.fetchTaxonomy(),
        loadArticlePage(),
    ])
})
</script>
