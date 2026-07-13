<template>
    <article
        class="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm transition duration-200 hover:-translate-y-1 hover:shadow-lg cursor-pointer"
        @click="goDetail"
    >
        <div class="relative article-cover-wrap">
            <img :src="coverUrl" :alt="article.title || '文章封面'" class="article-cover" />
        </div>

        <div class="p-5">
            <div class="flex flex-wrap gap-2">
                <span
                    v-for="item in displayChips"
                    :key="item"
                    class="rounded-md bg-[#e8f6ee] px-2 py-1 text-xs font-medium text-[#4e9f73]"
                >
                    {{ item }}
                </span>
            </div>

            <h3 class="mt-4 line-clamp-2 text-[21px] font-bold leading-[1.45] text-slate-800">
                {{ article.title || '未命名文章' }}
            </h3>

            <p class="mt-3 line-clamp-2 text-[15px] leading-7 text-slate-500">
                {{ article.summary || fallbackSummary }}
            </p>

            <div class="mt-4 flex items-center gap-4 text-sm text-slate-400">
                <span class="inline-flex items-center gap-1">
                    <el-icon><Calendar /></el-icon>
                    {{ formatDate(article.createTime) }}
                </span>
                <span class="inline-flex items-center gap-1">
                    <el-icon><FolderOpened /></el-icon>
                    {{ article.category?.name || '未分类' }}
                </span>
            </div>
        </div>
    </article>
</template>

<script setup>
import developerImage from '@/assets/developer.png'
import { Calendar, FolderOpened } from '@element-plus/icons-vue'
import { computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const props = defineProps({
    article: {
        type: Object,
        default: () => ({}),
    },
    showTop: {
        type: Boolean,
        default: false,
    },
})

const coverUrl = computed(() => props.article?.cover || developerImage)

const fallbackSummary = computed(() => {
    return `${props.article?.title || '这篇文章'} 目前还没有补充摘要，这里先展示博客首页卡片样式。`
})

const displayChips = computed(() => {
    const tags = (props.article?.tags || []).map(tag => tag.name).filter(Boolean)
    const category = props.article?.category?.name
    const chips = []

    if (category) {
        chips.push(category)
    }

    tags.slice(0, 3).forEach(tag => chips.push(tag))

    if (chips.length === 0) {
        chips.push('专栏', '知识星球')
    }

    return chips.slice(0, 4)
})

const formatDate = (value) => {
    if (!value) {
        return '未知时间'
    }
    const date = new Date(value)
    if (Number.isNaN(date.getTime())) {
        return value
    }
    return `${date.getFullYear()}-${`${date.getMonth() + 1}`.padStart(2, '0')}-${`${date.getDate()}`.padStart(2, '0')}`
}

const goDetail = () => {
    if (!props.article?.id) {
        return
    }

    router.push({
        path: `/article/${props.article.id}`,
    })
}

</script>

<style scoped>
.article-cover-wrap {
    aspect-ratio: 16 / 9;
    background: linear-gradient(135deg, #eff6ff 0%, #f8fafc 100%);
}

.article-cover {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
}

.article-cover--placeholder {
    display: flex;
    align-items: center;
    justify-content: center;
    color: #2563eb;
    font-size: 28px;
    font-weight: 700;
    letter-spacing: 0.08em;
}
</style>
