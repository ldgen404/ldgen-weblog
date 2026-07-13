<template>
    <SidebarCard>
        <div class="text-center">
            <el-avatar :size="78" :src="blogSettings.userAvatar">
                {{ authorText }}
            </el-avatar>

            <div class="mt-4 text-[18px] font-semibold text-slate-900">
                {{ blogSettings.author || '博主' }}
            </div>

            <div class="mt-2 text-sm text-slate-500">
                {{ briefProfile }}
            </div>
        </div>

        <div class="mt-7 grid grid-cols-4 gap-2 text-center">
            <div v-for="item in stats" :key="item.label">
                <div class="text-[28px] font-semibold leading-none text-slate-800">{{ item.value }}</div>
                <div class="mt-2 text-xs text-slate-400">{{ item.label }}</div>
            </div>
        </div>

        <div class="mt-6 flex items-center justify-center gap-3">
            <a
                v-for="item in socialLinks"
                :key="item.label"
                :href="item.href"
                target="_blank"
                rel="noreferrer"
                class="flex h-9 w-9 items-center justify-center rounded-full text-xs font-semibold text-white transition hover:-translate-y-0.5"
                :class="item.className"
                :title="item.label"
            >
                {{ item.short }}
            </a>
        </div>
    </SidebarCard>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import SidebarCard from '@/components/frontend/SidebarCard.vue'
import { useBlogStore } from '@/stores/blog'

const blogStore = useBlogStore()

const props = defineProps({
    articleTotal: {
        type: Number,
        default: 0,
    },
    categoryTotal: {
        type: Number,
        default: 0,
    },
    tagTotal: {
        type: Number,
        default: 0,
    },
    visitTotal: {
        type: [Number, String],
        default: 0,
    },
})

const blogSettings = computed(() => blogStore.blogSettings)

const authorText = computed(() => {
    const author = blogSettings.value.author?.trim()
    return author ? author.slice(0, 1) : '博'
})

const briefProfile = computed(() => {
    return blogSettings.value.userProfile || '一个程序员'
})

const stats = computed(() => [
    { label: '文章', value: props.articleTotal },
    { label: '分类', value: props.categoryTotal },
    { label: '标签', value: props.tagTotal },
    { label: '总访问量', value: props.visitTotal },
])

const socialLinks = computed(() => {
    return [
        { label: 'GitHub', short: 'GH', href: blogSettings.value.githubHomepage, className: 'bg-[#3b82f6]' },
        { label: 'Gitee', short: 'G', href: blogSettings.value.giteeHomepage, className: 'bg-[#ef4444]' },
        { label: '知乎', short: '知', href: blogSettings.value.zhihuHomepage, className: 'bg-[#2563eb]' },
        { label: 'CSDN', short: 'C', href: blogSettings.value.csdnHomepage, className: 'bg-[#f97316]' },
    ].filter(item => item.href)
})

onMounted(() => {
    blogStore.fetchBlogSettings().catch(() => {})
})
</script>
