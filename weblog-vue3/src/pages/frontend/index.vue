<template>
    <div class="min-h-screen bg-[#f3f5f7]">
        <Header />

        <main class="mx-auto max-w-[1280px] px-3 py-4 lg:px-4 lg:py-5">
            <div class="grid gap-4 lg:grid-cols-[minmax(0,1fr)_252px] xl:grid-cols-[minmax(0,1fr)_300px]">
                <section>
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
                            <template v-else>暂时还没有文章内容</template>
                        </div>
                    </div>

                    <div class="mt-6 flex justify-center" v-if="!keyword && total > pageSize">
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

                        <SidebarCard>
                            <button
                                type="button"
                                class="flex w-full items-center justify-between rounded-xl bg-[#f8fbff] px-4 py-4 text-left transition hover:bg-[#eef6ff]"
                                @click="showChatroom"
                            >
                                <div class="flex items-center gap-3">
                                    <div class="flex h-10 w-10 items-center justify-center rounded-full bg-[#dff1ff] text-[#5aa7d8]">
                                        <el-icon :size="18"><ChatDotRound /></el-icon>
                                    </div>
                                    <div>
                                        <div class="text-[15px] font-semibold text-slate-800">公共聊天室</div>
                                        <div class="mt-1 text-sm text-slate-400">一起来聊天吧</div>
                                    </div>
                                </div>
                                <span class="text-slate-300">›</span>
                            </button>
                        </SidebarCard>

                        <CategoryTagCard
                            title="分类"
                            :items="categoryCards"
                            empty-text="暂无分类"
                            @select="showPending"
                        />

                        <CategoryTagCard
                            title="标签"
                            :items="tagCards"
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
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
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
const blogStore = useBlogStore()

const articleLoading = ref(false)
const articleList = ref([])
const currentPage = ref(1)
const pageSize = ref(6)
const total = ref(0)

const categoryList = computed(() => blogStore.categoryList)
const tagList = computed(() => blogStore.tagList)
const keyword = computed(() => (route.query.keyword || '').trim())

const filteredArticleList = computed(() => {
    if (!keyword.value) {
        return articleList.value
    }

    return articleList.value.filter((article) => {
        const searchText = [
            article.title,
            article.summary,
            article.category?.name,
            ...(article.tags || []).map(tag => tag.name),
        ].filter(Boolean).join(' ').toLowerCase()

        return searchText.includes(keyword.value.toLowerCase())
    })
})

const visitTotal = computed(() => {
    return 0
})

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

const showPending = (item) => {
    showMessage(`${item.name}页面开发中`, 'info')
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
