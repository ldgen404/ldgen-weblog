<template>
    <SidebarCard title="目录">
        <div v-if="items.length === 0" class="text-sm text-slate-400">暂无目录</div>

        <div v-else class="toc-scroll">
            <button
                v-for="item in items"
                :key="item.id"
                :ref="(el) => setTocItemRef(item.id, el)"
                type="button"
                class="toc-item"
                :class="[
                    item.level === 3 ? 'toc-item--child' : 'toc-item--parent',
                    activeId === item.id ? 'is-active' : '',
                ]"
                @click="emit('select', item)"
            >
                <span class="toc-item__text">{{ item.text }}</span>
            </button>
        </div>
    </SidebarCard>
</template>

<script setup>
import { nextTick, ref, watch } from 'vue'
import SidebarCard from '@/components/frontend/SidebarCard.vue'

const props = defineProps({
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

const tocItemRefs = ref(new Map())

const setTocItemRef = (id, el) => {
    if (!id) {
        return
    }

    if (el) {
        tocItemRefs.value.set(id, el)
        return
    }

    tocItemRefs.value.delete(id)
}

const scrollActiveItemIntoView = async () => {
    if (!props.activeId) {
        return
    }

    await nextTick()
    const activeElement = tocItemRefs.value.get(props.activeId)
    activeElement?.scrollIntoView({
        block: 'nearest',
        inline: 'nearest',
    })
}

watch(
    () => [props.activeId, props.items.length],
    () => {
        scrollActiveItemIntoView()
    },
    { flush: 'post' }
)
</script>

<style scoped>
.toc-scroll {
    max-height: min(60vh, calc(100vh - 188px));
    overflow-y: auto;
    padding-right: 4px;
    scrollbar-gutter: stable;
}

.toc-scroll::-webkit-scrollbar {
    width: 6px;
}

.toc-scroll::-webkit-scrollbar-thumb {
    background: rgba(148, 163, 184, 0.45);
    border-radius: 999px;
}

.toc-scroll::-webkit-scrollbar-track {
    background: transparent;
}

.toc-item {
    background: transparent;
    border: 0;
    border-radius: 10px;
    color: #475569;
    cursor: pointer;
    display: block;
    padding: 7px 10px;
    position: relative;
    text-align: left;
    transition: background-color 0.2s ease, color 0.2s ease;
    width: 100%;
}

.toc-item:hover {
    color: #2563eb;
}

.toc-item--parent {
    font-size: 13px;
    font-weight: 600;
    line-height: 1.55;
}

.toc-item--child {
    font-size: 12px;
    line-height: 1.6;
    padding-left: 22px;
}

.toc-item__text {
    display: -webkit-box;
    line-clamp: 2;
    overflow: hidden;
    text-overflow: ellipsis;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
}

.toc-item.is-active {
    background: #f8fbff;
    color: #2563eb;
}

.toc-item.is-active::before {
    background: #2563eb;
    border-radius: 999px;
    content: '';
    left: 0;
    position: absolute;
    top: 8px;
    bottom: 8px;
    width: 3px;
}
</style>
