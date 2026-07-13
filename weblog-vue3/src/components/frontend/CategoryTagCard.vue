<template>
    <SidebarCard :title="title">
        <template #icon>
            <el-icon
                v-if="type === 'category'"
                class="text-[22px] text-[#5b7df6]"
            >
                <FolderOpened />
            </el-icon>
            <el-icon
                v-else
                class="text-[22px] text-[#a78bfa]"
            >
                <PriceTag />
            </el-icon>
        </template>

        <div v-if="type === 'category'" class="space-y-2">
            <button
                v-for="item in items"
                :key="item.id"
                type="button"
                class="flex w-full items-center gap-3 rounded-xl px-2 py-2.5 text-left transition"
                :class="categoryClass"
                @click="emit('select', item)"
            >
                <el-icon class="shrink-0 text-[22px] text-slate-500"><FolderOpened /></el-icon>
                <span class="truncate text-[15px] text-slate-700">
                    <template v-if="prefix">{{ prefix }} </template>{{ item.name }}
                </span>
            </button>
        </div>

        <div v-else class="flex flex-wrap gap-3">
            <button
                v-for="item in items"
                :key="item.id"
                type="button"
                class="rounded-xl px-5 py-2 text-[15px] font-medium transition"
                :class="getTagClass(item)"
                @click="emit('select', item)"
            >
                <template v-if="prefix">{{ prefix }} </template>{{ item.name }}
            </button>
        </div>

        <div v-if="items.length === 0" class="text-sm text-slate-400">{{ emptyText }}</div>
    </SidebarCard>
</template>

<script setup>
import { FolderOpened, PriceTag } from '@element-plus/icons-vue'
import { computed } from 'vue'
import SidebarCard from '@/components/frontend/SidebarCard.vue'

const props = defineProps({
    title: {
        type: String,
        default: '',
    },
    items: {
        type: Array,
        default: () => [],
    },
    type: {
        type: String,
        default: 'category',
    },
    prefix: {
        type: String,
        default: '',
    },
    emptyText: {
        type: String,
        default: '暂无数据',
    },
    activeId: {
        type: [String, Number, null],
        default: null,
    },
})

const emit = defineEmits(['select'])

const categoryClass = computed(() => {
    return 'hover:bg-slate-50'
})

const getTagClass = (item) => {
    if (String(props.activeId) === String(item?.id)) {
        return 'bg-[#b9eee0] text-[#1f5f55] shadow-sm'
    }

    return 'bg-[#ddf7ef] text-[#285f55] hover:bg-[#c8f1e5] hover:text-[#1f4d45]'
}
</script>
