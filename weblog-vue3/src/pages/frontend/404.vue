<template>
    <div class="min-h-screen bg-[#f3f5f7]">
        <Header />

        <main class="mx-auto flex max-w-[1280px] items-center justify-center px-4 py-16">
            <section class="w-full max-w-3xl rounded-[32px] border border-slate-200 bg-white px-8 py-16 text-center shadow-sm">
                <div class="text-6xl font-bold text-[#2563eb]">404</div>
                <h1 class="mt-6 text-3xl font-semibold text-slate-900">页面走丢了</h1>
                <p class="mt-4 text-base leading-8 text-slate-500">
                    你访问的页面不存在，可能已经被移动、删除，或者地址输入有误。
                </p>

                <div class="mt-8 flex items-center justify-center gap-4">
                    <el-button type="primary" round @click="router.push('/')">返回首页</el-button>
                    <el-button round @click="router.back()">返回上一页</el-button>
                </div>
            </section>
        </main>

        <Footer />
    </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import Header from '@/components/frontend/Header.vue'
import Footer from '@/components/frontend/Footer.vue'

const router = useRouter()
</script>
