<template>
    <header class="sticky top-0 z-40 border-b border-slate-200 bg-white/95 backdrop-blur">
        <div class="mx-auto flex max-w-screen-xl items-center justify-between gap-4 px-4 py-3 lg:px-6">
            <div class="flex min-w-0 items-center gap-3">
                <button
                    type="button"
                    class="inline-flex h-10 w-10 items-center justify-center rounded-xl border border-slate-200 text-slate-600 transition hover:border-blue-200 hover:bg-blue-50 hover:text-blue-600 md:hidden"
                    @click="toggleMobileMenu"
                >
                    <el-icon :size="18">
                        <component :is="isMobileMenuOpen ? Close : Menu" />
                    </el-icon>
                </button>

                <RouterLink to="/" class="flex min-w-0 items-center gap-3">
                    <div class="flex h-11 w-11 items-center justify-center overflow-hidden rounded-2xl bg-gradient-to-br from-blue-500 to-cyan-400 shadow-sm ring-1 ring-blue-100">
                        <img
                            v-if="blogSettings.logo"
                            :src="blogSettings.logo"
                            :alt="blogSettings.webName"
                            class="h-full w-full object-cover"
                        />
                        <span v-else class="text-lg font-semibold text-white">{{ logoText }}</span>
                    </div>

                    <div class="min-w-0">
                        <div class="truncate text-lg font-semibold leading-6 text-slate-900">
                            {{ blogSettings.webName }}
                        </div>
                        <div class="truncate text-xs text-slate-500">
                            {{ subTitle }}
                        </div>
                    </div>
                </RouterLink>
            </div>

            <nav class="hidden items-center gap-1 md:flex">
                <RouterLink
                    v-for="item in navItems"
                    :key="item.key"
                    :to="item.path"
                    class="rounded-xl px-4 py-2 text-sm font-medium transition"
                    :class="route.path === item.path
                        ? 'bg-blue-50 text-blue-600'
                        : 'text-slate-600 hover:bg-slate-50 hover:text-blue-600'"
                >
                    {{ item.label }}
                </RouterLink>
            </nav>

            <div class="hidden items-center gap-3 md:flex">
                <div class="relative">
                    <el-icon class="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">
                        <Search />
                    </el-icon>
                    <input
                        v-model.trim="keyword"
                        type="text"
                        placeholder="请输入关键词..."
                        class="h-10 w-56 rounded-xl border border-slate-200 bg-slate-50 pl-9 pr-4 text-sm text-slate-700 outline-none transition placeholder:text-slate-400 focus:border-blue-300 focus:bg-white"
                        @keyup.enter="handleSearch"
                    />
                </div>

                <template v-if="isLogin">
                    <el-dropdown @command="handleCommand">
                        <div class="flex cursor-pointer items-center gap-3 rounded-2xl border border-slate-200 bg-slate-50 px-3 py-2">
                            <el-avatar :size="34" :src="userAvatar">
                                {{ userNameText }}
                            </el-avatar>
                            <div class="max-w-[120px]">
                                <div class="truncate text-sm font-medium text-slate-800">{{ displayName }}</div>
                                <div class="truncate text-xs text-slate-400">已登录</div>
                            </div>
                            <el-icon class="text-slate-400"><ArrowDown /></el-icon>
                        </div>
                        <template #dropdown>
                            <el-dropdown-menu>
                                <el-dropdown-item command="admin">进入后台</el-dropdown-item>
                                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
                            </el-dropdown-menu>
                        </template>
                    </el-dropdown>
                </template>

                <template v-else>
                    <button
                        type="button"
                        class="inline-flex h-10 items-center rounded-xl border border-slate-200 px-4 text-sm font-medium text-slate-600 transition hover:border-blue-200 hover:bg-blue-50 hover:text-blue-600"
                        @click="router.push('/login')"
                    >
                        登录
                    </button>
                    <button
                        type="button"
                        class="inline-flex h-10 items-center rounded-xl bg-blue-600 px-4 text-sm font-medium text-white transition hover:bg-blue-700"
                        @click="router.push('/admin/index')"
                    >
                        后台管理
                    </button>
                </template>
            </div>
        </div>

        <transition
            enter-active-class="transition duration-200 ease-out"
            enter-from-class="-translate-y-2 opacity-0"
            enter-to-class="translate-y-0 opacity-100"
            leave-active-class="transition duration-150 ease-in"
            leave-from-class="translate-y-0 opacity-100"
            leave-to-class="-translate-y-2 opacity-0"
        >
            <div v-if="isMobileMenuOpen" class="border-t border-slate-200 bg-white md:hidden">
                <div class="mx-auto max-w-screen-xl px-4 py-4">
                    <div class="relative">
                        <el-icon class="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">
                            <Search />
                        </el-icon>
                        <input
                            v-model.trim="keyword"
                            type="text"
                            placeholder="请输入关键词..."
                            class="h-11 w-full rounded-2xl border border-slate-200 bg-slate-50 pl-9 pr-4 text-sm text-slate-700 outline-none transition placeholder:text-slate-400 focus:border-blue-300 focus:bg-white"
                            @keyup.enter="handleMobileSearch"
                        />
                    </div>

                    <nav class="mt-4 grid gap-2">
                        <RouterLink
                            v-for="item in navItems"
                            :key="`mobile-${item.key}`"
                            :to="item.path"
                            class="flex items-center justify-between rounded-2xl px-4 py-3 text-left text-sm font-medium transition"
                            :class="route.path === item.path
                                ? 'bg-blue-50 text-blue-600'
                                : 'bg-slate-50 text-slate-700 hover:bg-slate-100'"
                            @click="isMobileMenuOpen = false"
                        >
                            <span>{{ item.label }}</span>
                            <el-icon v-if="route.path === item.path" :size="16">
                                <ArrowRight />
                            </el-icon>
                        </RouterLink>
                    </nav>

                    <div class="mt-4 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
                        <div class="flex items-center justify-between gap-3">
                            <div class="min-w-0">
                                <div class="truncate text-sm font-medium text-slate-800">
                                    {{ isLogin ? displayName : blogSettings.author }}
                                </div>
                                <div class="truncate text-xs text-slate-400">
                                    {{ blogSettings.webName }}
                                </div>
                            </div>
                        </div>

                        <div class="mt-4 flex gap-3">
                            <button
                                v-if="!isLogin"
                                type="button"
                                class="inline-flex h-10 flex-1 items-center justify-center rounded-xl border border-slate-200 bg-white text-sm font-medium text-slate-600"
                                @click="goLogin"
                            >
                                登录
                            </button>
                            <button
                                type="button"
                                class="inline-flex h-10 flex-1 items-center justify-center rounded-xl bg-blue-600 text-sm font-medium text-white"
                                @click="goAdmin"
                            >
                                进入后台
                            </button>
                            <button
                                v-if="isLogin"
                                type="button"
                                class="inline-flex h-10 flex-1 items-center justify-center rounded-xl border border-slate-200 bg-white text-sm font-medium text-slate-600"
                                @click="logout"
                            >
                                退出登录
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </transition>
    </header>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowDown, ArrowRight, Close, Menu, Search } from '@element-plus/icons-vue'
import { getToken } from '@/composables/cookie'
import { showMessage } from '@/composables/util'
import { useBlogStore } from '@/stores/blog'
import { useUserStore } from '@/stores/user'
import { logout as logoutApi } from '@/api/admin/user'

const router = useRouter()
const route = useRoute()
const blogStore = useBlogStore()
const userStore = useUserStore()

const isMobileMenuOpen = ref(false)
const keyword = ref(route.query.keyword || '')

const navItems = [
    { key: 'home', label: '首页', path: '/' },
    { key: 'category', label: '分类', path: '/category' },
    { key: 'tag', label: '标签', path: '/tag' },
    { key: 'archive', label: '归档', path: '/archive' },
]

const blogSettings = computed(() => blogStore.blogSettings)
const isLogin = computed(() => !!getToken())
const displayName = computed(() => userStore.userInfo?.userName || userStore.userInfo?.userAccount || blogSettings.value.author || '管理员')
const userAvatar = computed(() => userStore.userInfo?.userAvatar || blogSettings.value.userAvatar || '')
const userNameText = computed(() => displayName.value?.slice(0, 1) || '管')
const logoText = computed(() => blogSettings.value.webName?.trim()?.slice(0, 1) || 'W')
const subTitle = computed(() => blogSettings.value.userProfile || `${blogSettings.value.author || '博主'} 的技术博客`)

const syncKeywordFromRoute = () => {
    keyword.value = route.query.keyword || ''
}

const handleSearch = () => {
    const nextKeyword = keyword.value.trim()
    router.push({
        path: '/',
        query: nextKeyword ? { keyword: nextKeyword } : {},
    })
}

const handleMobileSearch = () => {
    handleSearch()
    isMobileMenuOpen.value = false
}

const goLogin = () => {
    isMobileMenuOpen.value = false
    router.push('/login')
}

const goAdmin = () => {
    isMobileMenuOpen.value = false
    router.push('/admin/index')
}

const logout = async () => {
    try {
        await logoutApi()
    } catch (error) {
        // 即使后端退出异常，也要清掉本地登录态
    }
    userStore.logout()
    isMobileMenuOpen.value = false
    showMessage('已退出登录')
    router.push('/')
}

const handleCommand = (command) => {
    if (command === 'admin') {
        goAdmin()
        return
    }
    logout()
}

const toggleMobileMenu = () => {
    isMobileMenuOpen.value = !isMobileMenuOpen.value
}

watch(() => route.fullPath, () => {
    isMobileMenuOpen.value = false
    syncKeywordFromRoute()
})

onMounted(() => {
    blogStore.fetchBlogSettings().catch(() => {})
    if (isLogin.value && !userStore.userInfo?.id) {
        userStore.setUserInfo()
    }
})
</script>
