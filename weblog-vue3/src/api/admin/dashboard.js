import axios from '@/axios'

export function getDashboardStatistics() {
    return axios.post('/admin/dashboard/statistics')
}

export function getDashboardPublishHotspot() {
    return axios.post('/admin/dashboard/publish-hotspot')
}

export function getDashboardPvWeekly() {
    return axios.post('/admin/dashboard/pv-weekly')
}
