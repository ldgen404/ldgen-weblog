import axios from "@/axios";

// 获取分类分页数据
export function getCategoryPageList(data) {
    return axios.post("/category/pageList", data)
}

// 获取分类下拉列表
export function getCategorySelectList() {
    return axios.post("/category/select/list")
}

// 获取所有分类
export function addCategory(data) {
    return axios.post("/category/add", data)
}

// 删除分类
export function deleteCategory(id) {
    return axios.post("/category/delete", {id})
}
