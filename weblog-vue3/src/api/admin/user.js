import axios from "@/axios";

// 登录接口
export function login(userAccount, userPassword) {
    return axios.post("/user/login", {userAccount, userPassword})
}

// 获取登录用户信息
export function getUserInfo() {
    return axios.get("/user/get/login")
}

// 修改用户密码
export function updateAdminPassword(data) {
    return axios.post("/admin/password/update", data)
}