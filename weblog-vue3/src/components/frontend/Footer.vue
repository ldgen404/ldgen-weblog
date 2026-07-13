<template>
    <footer class="border-t border-slate-200 bg-white/90">
        <div class="mx-auto flex max-w-screen-xl flex-col gap-3 px-4 py-8 text-center text-sm text-slate-400 lg:px-6">
            <div>{{ blogSettings.webName || 'Weblog' }}</div>
            <div>
                Copyright © 2023 - {{ currentYear }}
                <span class="mx-1">•</span>
                Powered by {{ blogSettings.author || 'ldgen' }}
            </div>
        </div>
    </footer>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useBlogStore } from '@/stores/blog'

const blogStore = useBlogStore()

const blogSettings = computed(() => blogStore.blogSettings)
const currentYear = new Date().getFullYear()

onMounted(() => {
    blogStore.fetchBlogSettings().catch(() => {})
})
</script>
