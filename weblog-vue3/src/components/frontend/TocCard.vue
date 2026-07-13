<template>
    <SidebarCard title="目录">
        <div v-if="items.length === 0" class="text-sm text-slate-400">暂无目录</div>

        <div v-else class="space-y-1">
            <button
                v-for="item in items"
                :key="item.id"
                type="button"
                class="block w-full rounded-xl px-3 py-2 text-left text-sm transition"
                :class="[
                    item.level === 3 ? 'pl-7' : '',
                    activeId === item.id
                        ? 'bg-[#eef6ff] font-medium text-[#2563eb]'
                        : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900',
                ]"
                @click="emit('select', item)"
            >
                <span class="line-clamp-2">{{ item.text }}</span>
            </button>
        </div>
    </SidebarCard>
</template>

<script setup>
import SidebarCard from '@/components/frontend/SidebarCard.vue'

defineProps({
    items: {
        type: Array,
        default: () => [],
    },
    activeId: {
        type: String,
        default: '',
    },
})

const emit = defineEmits(['select'])
</script>
