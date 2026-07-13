import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getBlogSettingsInfo } from '@/api/frontend/blog'
import { getCategoryList, getTagList } from '@/api/frontend/taxonomy'

const defaultSettings = {
  webName: 'Weblog',
  author: '博主',
  logo: '',
  userAvatar: '',
  userProfile: '',
  githubHomepage: '',
  giteeHomepage: '',
  zhihuHomepage: '',
  csdnHomepage: '',
  pvTotalCount: 0,
}

export const useBlogStore = defineStore('blog', () => {
  const blogSettings = ref({ ...defaultSettings })
  const categoryList = ref([])
  const tagList = ref([])
  const settingsLoaded = ref(false)
  const taxonomyLoaded = ref(false)
  const settingsLoadedAt = ref(0)

  async function fetchBlogSettings(force = false) {
    const isFresh = Date.now() - settingsLoadedAt.value < 30000

    if (settingsLoaded.value && !force && isFresh) {
      return blogSettings.value
    }

    const res = await getBlogSettingsInfo()
    if (res.code === 0 && res.data) {
      blogSettings.value = {
        ...defaultSettings,
        ...res.data,
      }
      settingsLoaded.value = true
      settingsLoadedAt.value = Date.now()
    }
    return blogSettings.value
  }

  async function fetchTaxonomy(force = false) {
    if (taxonomyLoaded.value && !force) {
      return {
        categoryList: categoryList.value,
        tagList: tagList.value,
      }
    }

    const [categoryRes, tagRes] = await Promise.all([
      getCategoryList(),
      getTagList(),
    ])

    if (categoryRes.code === 0) {
      categoryList.value = categoryRes.data || []
    }

    if (tagRes.code === 0) {
      tagList.value = tagRes.data || []
    }

    taxonomyLoaded.value = true
    return {
      categoryList: categoryList.value,
      tagList: tagList.value,
    }
  }

  return {
    blogSettings,
    categoryList,
    tagList,
    fetchBlogSettings,
    fetchTaxonomy,
  }
})
