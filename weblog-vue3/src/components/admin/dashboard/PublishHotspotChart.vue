<template>
    <div ref="chartRef" class="chart-canvas"></div>
</template>

<script setup>
import * as echarts from 'echarts'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = defineProps({
    dates: {
        type: Array,
        default: () => [],
    },
    values: {
        type: Array,
        default: () => [],
    },
})

const chartRef = ref(null)
let chartInstance = null

const xAxisData = computed(() => Array.isArray(props.dates) ? props.dates : [])
const seriesData = computed(() => Array.isArray(props.values) ? props.values : [])

const initChart = () => {
    if (!chartRef.value) {
        return
    }

    if (!chartInstance) {
        chartInstance = echarts.init(chartRef.value)
    }
}

const renderChart = () => {
    initChart()
    if (!chartInstance) {
        return
    }

    chartInstance.setOption({
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'shadow',
            },
            backgroundColor: 'rgba(17, 24, 39, 0.88)',
            borderWidth: 0,
            textStyle: {
                color: '#fff',
            },
            formatter(params) {
                const item = params?.[0]
                if (!item) {
                    return ''
                }
                return `${item.axisValue}<br/>发布文章：${item.value} 篇`
            },
        },
        grid: {
            top: 24,
            right: 18,
            bottom: 20,
            left: 18,
            containLabel: true,
        },
        xAxis: {
            type: 'category',
            data: xAxisData.value,
            axisTick: {
                show: false,
            },
            axisLine: {
                lineStyle: {
                    color: '#e5e7eb',
                },
            },
            axisLabel: {
                color: '#6b7280',
            },
        },
        yAxis: {
            type: 'value',
            minInterval: 1,
            splitLine: {
                lineStyle: {
                    color: '#f1f5f9',
                },
            },
            axisLabel: {
                color: '#6b7280',
            },
        },
        series: [
            {
                type: 'bar',
                data: seriesData.value,
                barWidth: 22,
                itemStyle: {
                    borderRadius: [10, 10, 0, 0],
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: '#60a5fa' },
                        { offset: 1, color: '#2563eb' },
                    ]),
                },
            },
        ],
    }, true)
}

const handleResize = () => {
    chartInstance?.resize()
}

watch([xAxisData, seriesData], () => {
    nextTick(() => {
        renderChart()
    })
}, { deep: true })

onMounted(() => {
    nextTick(() => {
        renderChart()
    })
    window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
    window.removeEventListener('resize', handleResize)
    chartInstance?.dispose()
    chartInstance = null
})
</script>

<style scoped>
.chart-canvas {
    width: 100%;
    height: 320px;
}
</style>
