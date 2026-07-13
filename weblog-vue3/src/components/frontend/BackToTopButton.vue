<template>
    <transition name="fade-slide">
        <button
            v-if="visible"
            type="button"
            class="fixed bottom-8 right-8 z-50 flex h-12 w-12 items-center justify-center rounded-full bg-[#2563eb] text-white shadow-lg transition hover:bg-[#1d4ed8]"
            @click="scrollToTop"
        >
            <el-icon :size="20"><ArrowUp /></el-icon>
        </button>
    </transition>
</template>

<script setup>
import { ArrowUp } from '@element-plus/icons-vue'
import { onBeforeUnmount, onMounted, ref } from 'vue'

const visible = ref(false)

const handleScroll = () => {
    visible.value = window.scrollY > 360
}

const scrollToTop = () => {
    window.scrollTo({
        top: 0,
        behavior: 'smooth',
    })
}

onMounted(() => {
    handleScroll()
    window.addEventListener('scroll', handleScroll, { passive: true })
})

onBeforeUnmount(() => {
    window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.fade-slide-enter-active,
.fade-slide-leave-active {
    transition: all 0.2s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
    opacity: 0;
    transform: translateY(10px);
}
</style>
