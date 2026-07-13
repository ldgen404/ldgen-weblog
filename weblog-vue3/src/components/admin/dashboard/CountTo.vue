<template>
    <span>{{ displayValue }}</span>
</template>

<script setup>
import { computed, onBeforeUnmount, ref, watch } from 'vue'

const props = defineProps({
    value: {
        type: Number,
        default: 0,
    },
    duration: {
        type: Number,
        default: 1200,
    },
})

const currentValue = ref(0)
let animationFrameId = 0

const stopAnimation = () => {
    if (animationFrameId) {
        cancelAnimationFrame(animationFrameId)
        animationFrameId = 0
    }
}

const animateTo = (targetValue) => {
    stopAnimation()

    const startValue = currentValue.value
    const diff = targetValue - startValue
    if (diff === 0) {
        return
    }

    const startTime = performance.now()

    const step = (timestamp) => {
        const progress = Math.min((timestamp - startTime) / props.duration, 1)
        const easedProgress = 1 - Math.pow(1 - progress, 3)
        currentValue.value = Math.round(startValue + diff * easedProgress)

        if (progress < 1) {
            animationFrameId = requestAnimationFrame(step)
        } else {
            currentValue.value = targetValue
            animationFrameId = 0
        }
    }

    animationFrameId = requestAnimationFrame(step)
}

watch(
    () => props.value,
    (value) => {
        animateTo(Number(value || 0))
    },
    { immediate: true }
)

onBeforeUnmount(() => {
    stopAnimation()
})

const displayValue = computed(() => currentValue.value.toLocaleString())
</script>
