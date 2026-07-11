<template>
    <div>
        <!-- 表头分页查询条件， shadow="never" 指定 card 卡片组件没有阴影 -->
        <el-card shadow="never" class="mb-5">
            <!-- flex 布局，内容垂直居中 -->
            <div class="flex items-center">
                <el-text>标签名称</el-text>
                <div class="ml-3 w-52 mr-5">
                    <el-input
                        v-model="searchTagName"
                        placeholder="请输入（模糊查询）"
                        clearable
                        @input="triggerSearch"
                        @clear="reset"
                    />
                </div>

                <el-text>创建日期</el-text>
                <div class="ml-3 w-30 mr-5">
                    <!-- 日期选择组件（区间选择） -->
                    <el-date-picker v-model="pickDate" type="daterange" range-separator="至" start-placeholder="开始时间"
                        end-placeholder="结束时间" size="default" :shortcuts="shortcuts" @change="datepickerChange" />
                </div>

                <el-button type="primary" class="ml-3" :icon="Search" :loading="tableLoading" @click="handleSearch">查询</el-button>
                <el-button class="ml-3" :icon="RefreshRight" :disabled="tableLoading" @click="reset">重置</el-button>
            </div>
        </el-card>

        <el-card shadow="never">
            <!-- 新增按钮 -->
            <div class="mb-5">
                <el-button type="primary" @click="addCategoryBtnClick">
                    <el-icon class="mr-1">
                        <Plus />
                    </el-icon>
                    新增</el-button>
            </div>

            <!-- 分页列表 -->
            <el-table :data="tableData" border stripe style="width: 100%" v-loading="tableLoading">
                <el-table-column prop="name" label="标签名称" width="180">
                    <template #default="scope">
                        <el-tag class="ml-2" type="success">{{ scope.row.tagName }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="180" />
                <el-table-column label="操作">
                    <template #default="scope">
                        <el-button type="danger" size="small" @click="deleteTagSubmit($event, scope.row)">
                            <el-icon class="mr-1">
                                <Delete />
                            </el-icon>
                            删除
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>

            <!-- 分页 -->
            <div class="mt-10 flex justify-center">
                <el-pagination v-model:current-page="current" v-model:page-size="size" :page-sizes="[10, 20, 50]"
                    :small="false" :background="true" layout="total, sizes, prev, pager, next, jumper" :total="total"
                    @size-change="handleSizeChange" @current-change="getTableData" />
            </div>

        </el-card>

        <!-- 添加标签 -->
        <FormDialog ref="formDialogRef" title="添加文章标签" destroyOnClose @submit="onSubmit">
            <el-form ref="formRef" :model="form">
                <el-form-item prop="name">
                    <el-tag v-for="tag in dynamicTags" :key="tag" class="mx-1" closable :disable-transitions="false"
                        @close="handleClose(tag)">
                        {{ tag }}
                    </el-tag>
                    <span class="w-20">
                        <el-input v-if="inputVisible" ref="InputRef" v-model="inputValue" class="ml-1 w-20" size="small"
                        @keyup.enter="handleInputConfirm" @blur="handleInputConfirm" />
                    <el-button v-else class="button-new-tag ml-1" size="small" @click="showInput">
                        + 新增标签
                    </el-button>
                    </span>
                </el-form-item>
            </el-form>
        </FormDialog>

    </div>
</template>

<script setup>
import { Search, RefreshRight } from '@element-plus/icons-vue'
import { ref, reactive, nextTick } from 'vue'
import { getTagPageList, addTag, deleteTag } from '@/api/admin/tag'
import moment from 'moment'
import { showMessage, showModel } from '@/composables/util'
import FormDialog from '@/components/FormDialog.vue'

// 分页查询的标签名称
const searchTagName = ref('')
// 日期
const pickDate = ref('')

// 查询条件：开始结束时间
const startDate = ref('')
const endDate = ref('')

// 监听日期组件改变事件，并将开始结束时间设置到变量中
const datepickerChange = (e) => {
    if (!e) {
        startDate.value = ''
        endDate.value = ''
        triggerSearch()
        return
    }
    startDate.value = moment(e[0]).format('YYYY-MM-DD')
    endDate.value = moment(e[1]).format('YYYY-MM-DD')

    triggerSearch()
}

const shortcuts = [
    {
        text: '最近一周',
        value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
            return [start, end]
        },
    },
    {
        text: '最近一个月',
        value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
            return [start, end]
        },
    },
    {
        text: '最近三个月',
        value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
            return [start, end]
        },
    },
]

// 表格加载 Loading
const tableLoading = ref(false)
// 表格数据
const tableData = ref([])
// 当前页码，给了一个默认值 1
const current = ref(1)
// 总数据量，给了个默认值 0
const total = ref(0)
// 每页显示的数据量，给了个默认值 10
const size = ref(10)
let searchTimer = null


// 获取分页数据
function getTableData() {
    // 显示表格 loading
    tableLoading.value = true
    // 调用后台分页接口，并传入所需参数

    getTagPageList({ 
        pageNum: current.value, 
        pageSize: size.value, 
        startDate: startDate.value, 
        endDate: endDate.value, 
        tagName: searchTagName.value 
    })
        .then((res) => {
            if (res.code == 0) {
                let data = res.data
                tableData.value = data.records || []
                current.value = data.pageNumber ?? data.pageNum ?? 1
                size.value = data.pageSize ?? size.value
                total.value = data.totalRow ?? 0
            }
        })
        .finally(() => tableLoading.value = false) // 隐藏表格 loading
}
getTableData()

const handleSearch = () => {
    current.value = 1
    getTableData()
}

const triggerSearch = () => {
    current.value = 1
    clearTimeout(searchTimer)
    searchTimer = setTimeout(() => {
        getTableData()
    }, 300)
}

// 每页展示数量变更事件
const handleSizeChange = (chooseSize) => {
    size.value = chooseSize
    current.value = 1
    getTableData()
}

// 重置查询条件
const reset = () => {
    searchTagName.value = ''
    pickDate.value = ''
    startDate.value = ''
    endDate.value = ''
    current.value = 1
    clearTimeout(searchTimer)
    getTableData()
}

// 对话框是否显示
const formDialogRef = ref(null)

// 新增分类按钮点击事件
const addCategoryBtnClick = () => {
    formDialogRef.value.open()
}


// 表单引用
const formRef = ref(null)

// 添加文章标签表单对象
const form = reactive({
    tagName: []
})

const onSubmit = () => {
    // 先验证表单
    if (dynamicTags.value.length == 0) {
        showMessage('请至少添加一个标签', 'error')
        return
    }

    // 显示提交按钮 loading
    formDialogRef.value.showBtnLoading()
    
    // 设置表单数据，后端接收的是 tagName 数组
    form.tagName = dynamicTags.value
    
    addTag(form).then((res) => {
        if (res.code == 0) {
            showMessage('添加成功')
            // 将表单中标签数组置空
            form.tagName = []
            dynamicTags.value = []
            // 隐藏对话框
            formDialogRef.value.close()
            // 重新请求分页接口，渲染数据
            getTableData()
        } else {
            // 获取服务端返回的错误消息
            let message = res.message
            // 提示错误消息
            showMessage(message, 'error')
        }
    }).finally(() => formDialogRef.value.closeBtnLoading()) // 隐藏提交按钮 loading
}

// 删除标签
const deleteTagSubmit = (e, row) => {
    // 使按钮失焦
    e.currentTarget.blur()
    console.log(row)
    showModel('是否确定要删除该标签？').then(() => {
        deleteTag(row.id).then((res) => {
            if (res.code == 0) {
                showMessage('删除成功')
                // 重新请求分页接口，渲染数据
                getTableData()
            } else {
                // 获取服务端返回的错误消息
                let message = res.message
                // 提示错误消息
                showMessage(message, 'error')
            }
        })
    }).catch(() => {
        console.log('取消了')
    })
}

// 标签输入框值
const inputValue = ref('')
// 已输入的标签数组
const dynamicTags = ref([])
// 标签输入框是否显示
const inputVisible = ref(false)
// 标签输入框的引用
const InputRef = ref('')

const handleClose = (tag) => {
  dynamicTags.value.splice(dynamicTags.value.indexOf(tag), 1)
}

const showInput = () => {
  inputVisible.value = true
  nextTick(() => {
    InputRef.value.input.focus()
  })
}

const handleInputConfirm = () => {
  if (inputValue.value) {
    dynamicTags.value.push(inputValue.value)
  }
  inputVisible.value = false
  inputValue.value = ''
}

</script>
