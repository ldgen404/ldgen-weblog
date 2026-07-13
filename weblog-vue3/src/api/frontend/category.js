import axios from '@/axios'

// 获取前台分类下的文章分页数据
export function getCategoryArticlePageList(data) {
    return axios.post('/article/category/list', data)
}
