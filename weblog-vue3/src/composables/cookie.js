import { useCookies } from '@vueuse/integrations/useCookies'

const cookie = useCookies()

// ============================== Token 令牌 ==============================

// 存储在 Cookie 中的 Token 的 key
const TOKEN_KEY = 'satoken'
const LEGACY_TOKEN_KEY = 'Authorization'

// 获取 Token 值
export function getToken() {
    return cookie.get(TOKEN_KEY) || cookie.get(LEGACY_TOKEN_KEY)
}

// 设置 Token 到 Cookie 中
export function setToken(token) {
    cookie.remove(LEGACY_TOKEN_KEY)
    return cookie.set(TOKEN_KEY, token)
}

// 删除 Token
export function removeToken() {
    cookie.remove(LEGACY_TOKEN_KEY)
    return cookie.remove(TOKEN_KEY)
}

// ============================== 标签页 ==============================

// 存储在 Cookie 中的标签页数据的 key
const TAB_LIST_KEY = 'tabList'

// 获取 TabList
export function getTabList() {
    return cookie.get(TAB_LIST_KEY)
}

// 存储 TabList 到 Cookie 中
export function setTabList(tabList) {
    return cookie.set(TAB_LIST_KEY, tabList)
}
