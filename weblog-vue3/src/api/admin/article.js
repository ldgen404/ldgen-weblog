import axios from "@/axios";

function normalizeResponse(response, defaultMessage = '操作成功') {
    // 兼容 BaseResponse 和 ArticleController 当前直接返回原始数据的两种格式
    if (response && typeof response === 'object' && 'code' in response) {
        return response
    }

    return {
        code: 0,
        data: response,
        message: defaultMessage,
    }
}

// 获取文章列表
export function getArticleList() {
    return axios.get("/article/list").then((res) => normalizeResponse(res, '获取文章列表成功'))
}

// 获取文章详情
export function getArticleDetail(id) {
    return axios.get(`/article/getInfo/${id}`).then((res) => normalizeResponse(res, '获取文章详情成功'))
}

// 发布文章
export function publishArticle(data) {
    return axios.post("/article/publish", data)
}

// 更新文章基础信息
export function updateArticle(data) {
    return axios.put("/article/update", data).then((res) => {
        if (typeof res === 'boolean') {
            return res
                ? { code: 0, data: true, message: '更新成功' }
                : { code: -1, data: false, message: '更新失败' }
        }

        return normalizeResponse(res, '更新成功')
    })
}

// 删除文章
export function deleteArticle(id) {
    return axios.delete(`/article/remove/${id}`).then((res) => {
        if (typeof res === 'boolean') {
            return res
                ? { code: 0, data: true, message: '删除成功' }
                : { code: -1, data: false, message: '删除失败' }
        }

        return normalizeResponse(res, '删除成功')
    })
}
