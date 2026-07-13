<template>
    <section class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
        <div v-if="title" class="mb-4 flex items-center gap-2 text-[18px] font-semibold text-slate-900">
            <slot name="icon" />
            <span>{{ title }}</span>
        </div>
        <slot />
    </section>
</template>

<script setup>
defineProps({
    title: {
        type: String,
        default: '',
    },
})
</script>
