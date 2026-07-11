import axios from "@/axios";

// 获取博客设置详情
export function getBlogSettingsDetail() {
    return axios.get("/user/get/login")
}

// 上传文件
export function uploadFile(form) {
    return axios.post("/file/upload", form)
}

// 更新博客设置
export function updateBlogSettings(data) {
    return axios.post("/user/update", data)
}