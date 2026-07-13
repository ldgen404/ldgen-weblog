import axios from "@/axios";

// 获取前台博客设置信息
export function getBlogSettingsInfo() {
    return axios.get("/user/blog/settings");
}

export function getBlogSettings() {
    return getBlogSettingsInfo();
}
