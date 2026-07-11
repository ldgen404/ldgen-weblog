<template>
    <div>
        <!-- 卡片组件， shadow="never" 指定 card 卡片组件没有阴影 -->
        <el-card shadow="never">
            <el-form ref="formRef" :model="form" label-width="160px" :rules="rules">
                <el-form-item label="博客名称" prop="webName">
                    <el-input v-model="form.webName" clearable />
                </el-form-item>
                <el-form-item label="作者名" prop="author">
                    <el-input v-model="form.author" clearable />
                </el-form-item>

                <el-form-item label="博客 LOGO" prop="logo">
                    <el-upload class="avatar-uploader" action="#" :show-file-list="false" :http-request="uploadLogo" :before-upload="beforeAvatarUpload">
                        <img v-if="form.logo" :src="form.logo" class="avatar" />
                        <el-icon v-else class="avatar-uploader-icon">
                            <Plus />
                        </el-icon>
                    </el-upload>
                </el-form-item>
                <el-form-item label="作者头像" prop="userAvatar">
                    <el-upload class="avatar-uploader" action="#" :show-file-list="false" :http-request="uploadUserAvatar" :before-upload="beforeAvatarUpload">
                        <img v-if="form.userAvatar" :src="form.userAvatar" class="avatar" />
                        <el-icon v-else class="avatar-uploader-icon">
                            <Plus />
                        </el-icon>
                    </el-upload>
                </el-form-item>

                <el-form-item label="介绍语" prop="userProfile">
                    <el-input v-model="form.userProfile" type="textarea" />
                </el-form-item>
                <el-form-item label="开启 GihHub 访问">
                    <el-switch v-model="isGithubChecked" inline-prompt :active-icon="Check" :inactive-icon="Close"
                        @change="githubSwitchChange" />
                </el-form-item>
                <el-form-item label="GitHub 主页访问地址" v-if="isGithubChecked">
                    <el-input v-model="form.githubHomepage" clearable placeholder="请输入 GitHub 主页访问的 URL" />
                </el-form-item>

                <!-- 开启 Gitee 访问 -->
                <el-form-item label="开启 Gitee 访问">
                    <el-switch v-model="isGiteeChecked" inline-prompt :active-icon="Check" :inactive-icon="Close"
                        @change="giteeSwitchChange" />
                </el-form-item>
                <el-form-item label="Gitee 主页访问地址" v-if="isGiteeChecked">
                    <el-input v-model="form.giteeHomepage" clearable placeholder="请输入 Gitee 主页访问的 URL" />
                </el-form-item>

                <!-- 开启知乎访问 -->
                <el-form-item label="开启知乎访问">
                    <el-switch v-model="isZhihuChecked" inline-prompt :active-icon="Check" :inactive-icon="Close"
                        @change="zhihuSwitchChange" />
                </el-form-item>
                <el-form-item label="知乎主页访问地址" v-if="isZhihuChecked">
                    <el-input v-model="form.zhihuHomepage" clearable placeholder="请输入知乎主页访问的 URL" />
                </el-form-item>

                <!-- 开启 CSDN 访问 -->
                <el-form-item label="开启 CSDN 访问">
                    <el-switch v-model="isCSDNChecked" inline-prompt :active-icon="Check" :inactive-icon="Close"
                        @change="csdnSwitchChange" />
                </el-form-item>
                <el-form-item label="CSDN 主页访问地址" v-if="isCSDNChecked">
                    <el-input v-model="form.csdnHomepage" clearable placeholder="请输入 CSDN 主页访问的 URL" />
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" :loading="saving" :disabled="saving" @click="onSubmit($event)">保存</el-button>
                </el-form-item>
            </el-form>
            
        </el-card>
          

    </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { showMessage } from '@/composables/util'
import { Check, Close } from '@element-plus/icons-vue'
import { getBlogSettingsDetail, uploadFile, updateBlogSettings } from '@/api/admin/blogsettings'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
// 表单对象
const form = reactive({
    webName: '',
    author: '',
    logo: '',
    userAvatar: '',
    userProfile: '',
    githubHomepage: '',
    giteeHomepage: '',
    zhihuHomepage: '',
    csdnHomepage: '',
})

const formRef = ref(null)
const saving = ref(false)

// 规则校验
const rules = {
    webName: [{ required: true, message: '请输入博客名称', trigger: 'blur' }],
    author: [{ required: true, message: '请输入作者名', trigger: 'blur' }],
    logo: [{ required: true, message: '请上传博客 LOGO', trigger: 'blur' }],
    userAvatar: [{ required: true, message: '请上传作者头像', trigger: 'blur' }],
    userProfile: [{ required: true, message: '请输入介绍语', trigger: 'blur' }],
}

// 是否开启 GitHub
const isGithubChecked = ref(false)
// 是否开启 Gitee
const isGiteeChecked = ref(false)
// 是否开启知乎
const isZhihuChecked = ref(false)
// 是否开启 CSDN
const isCSDNChecked = ref(false)


// 监听 Github Switch 改变事件
const githubSwitchChange = (checked) => {
    if (checked == false) {
        form.githubHomepage = ''
    }
}

// 监听 Gitee Switch 改变事件
const giteeSwitchChange = (checked) => {
    if (checked == false) {
        form.giteeHomepage = ''
    }
}

// 监听知乎 Switch 改变事件
const zhihuSwitchChange = (checked) => {
    if (checked == false) {
        form.zhihuHomepage = ''
    }
}

// 监听 CSDN Switch 改变事件
const csdnSwitchChange = (checked) => {
    if (checked == false) {
        form.csdnHomepage = ''
    }
}



// 初始化博客设置数据，并渲染到页面上
function initBlogSettings() {
    getBlogSettingsDetail().then((res) => {
        if (res.code != 0) {
            showMessage(res.message || '获取博客设置失败', 'error')
            return
        }

        const data = res.data || {}

        form.webName = data.webName || ''
        form.author = data.author || ''
        form.logo = data.logo || ''
        form.userAvatar = data.userAvatar || data.avatar || ''
        form.userProfile = data.userProfile || ''
        form.githubHomepage = data.githubHomepage || ''
        form.giteeHomepage = data.giteeHomepage || ''
        form.zhihuHomepage = data.zhihuHomepage || ''
        form.csdnHomepage = data.csdnHomepage || ''

        isGithubChecked.value = !!form.githubHomepage
        isGiteeChecked.value = !!form.giteeHomepage
        isZhihuChecked.value = !!form.zhihuHomepage
        isCSDNChecked.value = !!form.csdnHomepage

        userStore.setUserInfoData({
            ...userStore.userInfo,
            ...data,
            username: data.userName || data.username || data.userAccount || userStore.userInfo.username || '',
        })
    })
}
// 手动调用一下初始化方法
initBlogSettings()

const getUploadedUrl = (res) => {
    const data = res?.data
    if (!data) return ''
    if (typeof data === 'string') return data
    if (typeof data === 'object') return data.url || data.fileUrl || data.path || data.fullPath || ''
    return ''
}

const onSubmit = (e) => {
    e?.currentTarget?.blur?.()
    formRef.value.validate((valid) => {
        if (!valid) {
            return
        }

        saving.value = true
        updateBlogSettings(form)
            .then((res) => {
                if (res.code == 0) {
                    userStore.setUserInfoData({
                        ...userStore.userInfo,
                        ...form,
                    })
                    showMessage('保存成功')
                } else {
                    showMessage(res.message || '保存失败', 'error')
                }
            })
            .catch(() => {
                showMessage('请求失败', 'error')
            })
            .finally(() => {
                saving.value = false
            })
    })
}

const beforeAvatarUpload = (rawFile) => {
    const isImage = rawFile.type?.startsWith('image/')
    if (!isImage) {
        showMessage('只能上传图片文件', 'error')
        return false
    }
    const isLt2M = rawFile.size / 1024 / 1024 < 2
    if (!isLt2M) {
        showMessage('图片大小不能超过 2MB', 'error')
        return false
    }
    return true
}

const uploadLogo = (options) => {
    const formData = new FormData()
    formData.append('file', options.file)
    uploadFile(formData).then((res) => {
        if (res.code == 0) {
            const url = getUploadedUrl(res)
            if (url) {
                form.logo = url
            }
            options.onSuccess(res)
        } else {
            showMessage(res.message || '上传失败', 'error')
            options.onError(new Error(res.message || '上传失败'))
        }
    }).catch((err) => {
        showMessage('上传失败', 'error')
        options.onError(err)
    })
}

const uploadUserAvatar = (options) => {
    const formData = new FormData()
    formData.append('file', options.file)
    uploadFile(formData).then((res) => {
        if (res.code == 0) {
            const url = getUploadedUrl(res)
            if (url) {
                form.userAvatar = url
                userStore.setUserInfoData({
                    ...userStore.userInfo,
                    userAvatar: url,
                })
            }
            options.onSuccess(res)
        } else {
            showMessage(res.message || '上传失败', 'error')
            options.onError(new Error(res.message || '上传失败'))
        }
    }).catch((err) => {
        showMessage('上传失败', 'error')
        options.onError(err)
    })
}
</script>


<style>
/* 解决 textarea :focus 状态下，边框消失的问题 */
.el-textarea__inner:focus {
    outline: 0 !important;
    box-shadow: 0 0 0 1px var(--el-input-focus-border-color) inset !important;
}

.avatar-uploader .avatar {
    width: 100px;
    height: 100px;
    display: block;
}

.avatar-uploader .el-upload {
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
    border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 100px;
    height: 100px;
    text-align: center;
}
</style>
