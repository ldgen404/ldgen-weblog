import axios from '@/axios'

// 获取前台标签下的文章分页数据
export function getTagArticlePageList(data) {
    return axios.post('/article/tag/list', data)
}
