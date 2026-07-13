import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserInfo } from '@/api/admin/user'
import { removeToken } from '@/composables/cookie'

export const useUserStore = defineStore('user', () => {
  // 用户信息
  const userInfo = ref({})

  // 直接设置用户信息
  function setUserInfoData(data = {}) {
    userInfo.value = data || {}
  }

  function clearUserInfo() {
    userInfo.value = {}
  }

  // 设置用户信息
  async function setUserInfo() {
    try {
      const res = await getUserInfo()
      if (res.code === 0 || res.success === true) {
        userInfo.value = res.data || {}
        return userInfo.value
      }
    } catch (error) {
      // 交给调用方决定是否跳转登录
    }

    clearUserInfo()
    return null
  }

  // 退出登录
  function logout() {
    // 删除 cookie 中的 token 令牌
    removeToken()
    // 删除登录用户信息
    clearUserInfo()
  }

  return { userInfo, setUserInfo, setUserInfoData, clearUserInfo, logout }
}, 
{
  // 开启持久化
  persist: true,
}
)
