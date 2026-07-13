import axios from '@/axios'

// 获取前台分类列表
export function getCategoryList() {
    return axios.post('/category/list')
}

// 获取前台标签列表
export function getTagList() {
    return axios.post('/tag/list')
}
