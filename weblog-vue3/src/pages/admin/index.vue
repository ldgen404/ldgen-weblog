<template>
    <div class="dashboard-page" v-loading="loading">
        <section class="dashboard-hero">
            <div>
                <p class="dashboard-eyebrow">Admin Dashboard</p>
                <h1 class="dashboard-title">博客后台数据概览</h1>
                <p class="dashboard-subtitle">
                    这里集中展示文章、分类、标签和站点访问趋势，方便快速感知博客运营状态。
                </p>
            </div>

            <el-button type="primary" round @click="loadDashboardData">
                刷新数据
            </el-button>
        </section>

        <section class="dashboard-card-grid">
            <article
                v-for="item in statCards"
                :key="item.key"
                class="dashboard-card"
            >
                <div class="dashboard-card-top">
                    <div class="dashboard-card-icon" :class="`theme-${item.theme}`">
                        <el-icon :size="24">
                            <component :is="item.icon" />
                        </el-icon>
                    </div>
                    <span class="dashboard-card-tag">{{ item.tag }}</span>
                </div>

                <div class="dashboard-card-content">
                    <div class="dashboard-card-label">{{ item.label }}</div>
                    <div class="dashboard-card-value">
                        <CountTo :value="item.value" />
                    </div>
                    <div class="dashboard-card-desc">{{ item.desc }}</div>
                </div>
            </article>
        </section>

        <section class="dashboard-chart-grid">
            <el-card shadow="never" class="dashboard-panel">
                <template #header>
                    <div class="panel-header">
                        <div>
                            <div class="panel-title">近半年文章发布热点</div>
                            <div class="panel-subtitle">按月份统计文章发布数量变化</div>
                        </div>
                    </div>
                </template>

                <PublishHotspotChart
                    :dates="publishHotspot.dateList"
                    :values="publishHotspot.articlePublishCountList"
                />
            </el-card>

            <el-card shadow="never" class="dashboard-panel">
                <template #header>
                    <div class="panel-header">
                        <div>
                            <div class="panel-title">最近一周 PV 访问趋势</div>
                            <div class="panel-subtitle">统计近 7 天页面访问量变化情况</div>
                        </div>
                    </div>
                </template>

                <PvWeeklyLineChart
                    :dates="pvWeekly.dateList"
                    :values="pvWeekly.pvCountList"
                />
            </el-card>
        </section>
    </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { CollectionTag, DataAnalysis, Document, FolderOpened } from '@element-plus/icons-vue'
import CountTo from '@/components/admin/dashboard/CountTo.vue'
import PublishHotspotChart from '@/components/admin/dashboard/PublishHotspotChart.vue'
import PvWeeklyLineChart from '@/components/admin/dashboard/PvWeeklyLineChart.vue'
import { getDashboardPublishHotspot, getDashboardPvWeekly, getDashboardStatistics } from '@/api/admin/dashboard'
import { showMessage } from '@/composables/util'

const loading = ref(false)

const statistics = reactive({
    articleTotalCount: 0,
    categoryTotalCount: 0,
    tagTotalCount: 0,
    pvTotalCount: 0,
})

const publishHotspot = reactive({
    dateList: [],
    articlePublishCountList: [],
})

const pvWeekly = reactive({
    dateList: [],
    pvCountList: [],
})

const statCards = computed(() => ([
    {
        key: 'article',
        label: '文章总数',
        tag: 'Articles',
        value: statistics.articleTotalCount,
        desc: '当前系统中累计发布的文章数量',
        icon: Document,
        theme: 'blue',
    },
    {
        key: 'category',
        label: '分类总数',
        tag: 'Categories',
        value: statistics.categoryTotalCount,
        desc: '用于文章归档与内容组织的分类数量',
        icon: FolderOpened,
        theme: 'green',
    },
    {
        key: 'tag',
        label: '标签总数',
        tag: 'Tags',
        value: statistics.tagTotalCount,
        desc: '文章内容关联使用的标签数量',
        icon: CollectionTag,
        theme: 'purple',
    },
    {
        key: 'pv',
        label: '总浏览量',
        tag: 'Views',
        value: statistics.pvTotalCount,
        desc: '站点累计产生的页面访问总次数',
        icon: DataAnalysis,
        theme: 'orange',
    },
]))

const loadDashboardData = async () => {
    loading.value = true
    try {
        const [statisticsRes, publishRes, pvRes] = await Promise.all([
            getDashboardStatistics(),
            getDashboardPublishHotspot(),
            getDashboardPvWeekly(),
        ])

        if (statisticsRes.code === 0 && statisticsRes.data) {
            Object.assign(statistics, statisticsRes.data)
        }

        if (publishRes.code === 0 && publishRes.data) {
            publishHotspot.dateList = Array.isArray(publishRes.data.dateList) ? publishRes.data.dateList : []
            publishHotspot.articlePublishCountList = Array.isArray(publishRes.data.articlePublishCountList)
                ? publishRes.data.articlePublishCountList
                : []
        }

        if (pvRes.code === 0 && pvRes.data) {
            pvWeekly.dateList = Array.isArray(pvRes.data.dateList) ? pvRes.data.dateList : []
            pvWeekly.pvCountList = Array.isArray(pvRes.data.pvCountList) ? pvRes.data.pvCountList : []
        }
    } catch (error) {
        showMessage('获取仪表盘数据失败', 'error')
    } finally {
        loading.value = false
    }
}

onMounted(() => {
    loadDashboardData()
})
</script>

<style scoped>
.dashboard-page {
    min-height: 100%;
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.dashboard-hero {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding: 28px 30px;
    border-radius: 24px;
    background: linear-gradient(135deg, #eff6ff 0%, #ffffff 55%, #f0fdf4 100%);
    border: 1px solid #e5e7eb;
}

.dashboard-eyebrow {
    margin: 0 0 8px;
    font-size: 12px;
    font-weight: 700;
    letter-spacing: 0.16em;
    text-transform: uppercase;
    color: #3b82f6;
}

.dashboard-title {
    margin: 0;
    font-size: 30px;
    font-weight: 700;
    line-height: 1.2;
    color: #111827;
}

.dashboard-subtitle {
    margin: 10px 0 0;
    max-width: 680px;
    color: #6b7280;
    font-size: 14px;
    line-height: 1.75;
}

.dashboard-card-grid {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 18px;
}

.dashboard-card {
    padding: 22px;
    border-radius: 22px;
    background: #fff;
    border: 1px solid #edf2f7;
    box-shadow: 0 14px 30px rgba(15, 23, 42, 0.05);
}

.dashboard-card-top {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
}

.dashboard-card-icon {
    width: 52px;
    height: 52px;
    border-radius: 16px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
}

.dashboard-card-tag {
    padding: 5px 12px;
    border-radius: 999px;
    background: #f8fafc;
    color: #64748b;
    font-size: 12px;
    font-weight: 600;
}

.dashboard-card-content {
    margin-top: 22px;
}

.dashboard-card-label {
    font-size: 14px;
    color: #64748b;
}

.dashboard-card-value {
    margin-top: 12px;
    font-size: 34px;
    font-weight: 700;
    color: #0f172a;
    line-height: 1.2;
}

.dashboard-card-desc {
    margin-top: 10px;
    color: #94a3b8;
    font-size: 13px;
    line-height: 1.7;
}

.theme-blue {
    color: #2563eb;
    background: rgba(59, 130, 246, 0.12);
}

.theme-green {
    color: #16a34a;
    background: rgba(34, 197, 94, 0.12);
}

.theme-purple {
    color: #7c3aed;
    background: rgba(168, 85, 247, 0.12);
}

.theme-orange {
    color: #ea580c;
    background: rgba(249, 115, 22, 0.14);
}

.dashboard-chart-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 18px;
}

.dashboard-panel {
    border-radius: 22px;
    border: 1px solid #edf2f7;
    box-shadow: 0 14px 30px rgba(15, 23, 42, 0.05);
}

:deep(.dashboard-panel .el-card__header) {
    padding: 22px 24px 0;
    border-bottom: none;
}

:deep(.dashboard-panel .el-card__body) {
    padding: 12px 18px 18px;
}

.panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.panel-title {
    font-size: 18px;
    font-weight: 700;
    color: #0f172a;
}

.panel-subtitle {
    margin-top: 6px;
    color: #94a3b8;
    font-size: 13px;
}

@media (max-width: 1280px) {
    .dashboard-card-grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
    }

    .dashboard-chart-grid {
        grid-template-columns: 1fr;
    }
}

@media (max-width: 768px) {
    .dashboard-page {
        gap: 16px;
    }

    .dashboard-hero {
        flex-direction: column;
        align-items: flex-start;
        padding: 22px 18px;
    }

    .dashboard-title {
        font-size: 24px;
    }

    .dashboard-card-grid {
        grid-template-columns: 1fr;
    }

    .dashboard-card {
        padding: 18px;
    }
}
</style>
