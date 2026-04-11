import axios from "@/axios";

// 获取分类分页数据
export function getCategoryPageList(data) {
    return axios.post("/category/pageList", data)
}

// 获取所有分类
export function addCategory(data) {
    return axios.post("/category/add", data)
}

// 删除分类
export function deleteCategory(id) {
    return axios.post("/category/delete", {id})
}