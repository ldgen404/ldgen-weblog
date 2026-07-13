import router from '@/router/index'
import { getToken } from '@/composables/cookie'
import { useUserStore } from '@/stores/user'
import { showMessage } from '@/composables/util'
import { showPageLoading, hidePageLoading } from '@/composables/util'


// 全局路由前置守卫
router.beforeEach(async (to, _from, next) => {
    console.log('==> 全局路由前置守卫')

    // 展示页面加载 Loading
    showPageLoading()
    
    let token = getToken()
    const userStore = useUserStore()
    const isAdminRoute = to.path.startsWith('/admin')

    if (!token && isAdminRoute) { 
        // 若用户想访问后台（以 /admin 为前缀的路由）
        // 未登录，则强制跳转登录页
        showMessage('请先登录', 'warning')
        return next({
            path: '/login',
            query: {
                redirect: to.fullPath
            }
        })
    }

    if (token && isAdminRoute && !userStore.userInfo?.id) {
        const userInfo = await userStore.setUserInfo()
        if (!userInfo?.id) {
            userStore.logout()
            showMessage('登录状态已失效，请重新登录', 'warning')
            return next({
                path: '/login',
                query: {
                    redirect: to.fullPath
                }
            })
        }
    }

    if (token && to.path == '/login') {
        // 若用户已经登录，且重复访问登录页
        showMessage('请勿重复登录', 'warning')
        return next({
            path: (to.query && to.query.redirect) ? to.query.redirect : '/admin/index'
        })
    }

    next()
})

// 全局路由后置守卫
router.afterEach((to) => {
    // 动态设置页面 Titile
    let title = (to.meta.title ? to.meta.title : '') + ' - Weblog'
    document.title = title

    // 隐藏页面加载 Loading
    hidePageLoading()
})
