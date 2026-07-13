import axios from '@/axios'

// 获取前台归档分页数据
export function getArchiveArticlePageList(data) {
    return axios.post('/archive/list', data)
}
