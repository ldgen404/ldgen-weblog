import axios from '@/axios'

// 获取前台首页文章分页数据
export function getArticlePageList(data) {
    return axios.post('/article/list', data)
}

// 获取前台文章详情
export function getFrontendArticleDetail(id) {
    return axios.get(`/article/detail/${id}`)
}
