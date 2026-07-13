import axios from "axios";
import router from "@/router";
import { getToken, removeToken } from "@/composables/cookie"
import { useUserStore } from '@/stores/user'
import { showMessage} from '@/composables/util'

// 创建 Axios 实例
const instance = axios.create({
    baseURL: "/api", // 你的 API 基础 URL
    timeout: 7000, // 请求超时时间
})

// 添加请求拦截器
instance.interceptors.request.use(function (config) {
    // 在发送请求之前做些什么
    const token = getToken()
    console.log('统一添加请求头中的 Token:' + token)

    // 当 token 不为空时
    if (token) {
        // Sa-Token 默认从 satoken 请求头中读取 token
        config.headers['satoken'] = token
    }

    return config;
}, function (error) {
    // 对请求错误做些什么
    return Promise.reject(error)
});

// 添加响应拦截器
instance.interceptors.response.use(function (response) {
    // 2xx 范围内的状态码都会触发该函数。
    // 对响应数据做点什么
    const data = response.data
    const code = data?.code
    const currentPath = router.currentRoute?.value?.fullPath || '/admin/index'
    const currentRoutePath = router.currentRoute?.value?.path || ''
    const isAdminPage = currentRoutePath.startsWith('/admin')

    if (code === 40100) {
        removeToken()
        const userStore = useUserStore()
        userStore.clearUserInfo()

        if (isAdminPage && currentRoutePath !== '/login') {
            showMessage('登录状态已失效，请重新登录', 'warning')
            router.push({
                path: '/login',
                query: {
                    redirect: currentPath
                }
            }).catch(() => {})
        }
    }

    return response.data
}, function (error) {
    // 超出 2xx 范围的状态码都会触发该函数。
    // 对响应错误做点什么

    const status = error?.response?.status

    if (status === 401) {
        removeToken()
        const userStore = useUserStore()
        userStore.clearUserInfo()
        showMessage('请先登录', 'warning')

        const currentPath = router.currentRoute?.value?.fullPath || '/admin/index'
        const currentRoutePath = router.currentRoute?.value?.path || ''
        if (currentRoutePath !== '/login') {
            router.push({
                path: '/login',
                query: {
                    redirect: currentPath
                }
            }).catch(() => {})
        }

        return Promise.reject(error)
    }

    const errorMsg = error?.response?.data?.message || error?.message || '请求失败'
    showMessage(errorMsg, 'error')

    return Promise.reject(error)
})

// 暴露出去
export default instance;
