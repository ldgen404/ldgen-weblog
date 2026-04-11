<template>
    <div>
        <!-- 表头分页查询条件， shadow="never" 指定 card 卡片组件没有阴影 -->
        <el-card shadow="never" class="mb-5">
            <!-- flex 布局，内容垂直居中 -->
            <div class="flex items-center">
                <el-text>分类名称</el-text>
                <div class="ml-3 w-52 mr-5"><el-input v-model="searchCategoryName" placeholder="请输入（模糊查询）" /></div>

                <el-text>创建日期</el-text>
                <div class="ml-3 w-30 mr-5">
                    <!-- 日期选择组件（区间选择） -->
                    <el-date-picker v-model="pickDate" type="daterange" range-separator="至" start-placeholder="开始时间"
                        end-placeholder="结束时间" size="default" :shortcuts="shortcuts" @change="datepickerChange"/>
                </div>

                <el-button type="primary" class="ml-3" :icon="Search" :loading="tableLoading" @click="getTableData">查询</el-button>
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
                <el-table-column prop="categoryName" label="分类名称" width="180" />
                <el-table-column prop="createTime" label="创建时间" width="180" />
                <el-table-column label="操作" >
                    <template #default="scope">
                    <el-button type="danger" size="small" @click="deleteCategorySubmit($event, scope.row)">
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
                :small="false" :background="true" layout="total, sizes, prev, pager, next, jumper"
                :total="total" @size-change="handleSizeChange" @current-change="getTableData" />
            </div>

        </el-card>

    <!-- 添加分类 -->
    <FormDialog ref="formDialogRef" title="添加文章分类" destroyOnClose @submit="onSubmit">
        <el-form ref="formRef" :rules="rules" :model="form">
            <el-form-item label="分类名称" prop="categoryName" label-width="80px" size="large">
                <el-input v-model="form.categoryName" placeholder="请输入分类名称" maxlength="20" show-word-limit clearable/>
            </el-form-item>
        </el-form>
    </FormDialog>

    </div>
</template>

<script setup>
import { Search, RefreshRight } from '@element-plus/icons-vue'
import { ref, reactive } from 'vue'
import { getCategoryPageList, addCategory, deleteCategory } from '@/api/admin/category'
import moment from 'moment'
import { showMessage, showModel } from '@/composables/util'
import FormDialog from '@/components/FormDialog.vue'

// 分页查询的分类名称
const searchCategoryName = ref('')
// 日期
const pickDate = ref('')

// 查询条件：开始结束时间
const startDate = ref('')
const endDate = ref('')

// 监听日期组件改变事件，并将开始结束时间设置到变量中
const datepickerChange = (e) => {
    if(!e) {
        startDate.value = ''
        endDate.value = ''
        return
    }
    startDate.value = moment(e[0]).format('YYYY-MM-DD')
    endDate.value = moment(e[1]).format('YYYY-MM-DD')

    console.log('开始时间：' + startDate.value + ', 结束时间：' + endDate.value)
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


// 获取分页数据
function getTableData() {
    // 显示表格 loading
    tableLoading.value = true;
    getCategoryPageList({
        current: current.value,
        size: size.value,
        startDate: startDate.value,
        endDate: endDate.value,
        categoryName: searchCategoryName.value
    })
    .then((res) => {
        if (res.code === 0) {
            const pageResult = res.data;
            tableData.value = pageResult.records || [];
            current.value = pageResult.pageNumber;
            size.value = pageResult.pageSize;
            total.value = pageResult.totalRow;
        }
    })
    .catch((err) => {
        console.error('请求失败:', err);
        // 即使请求出错，也要把loading关掉
    })
    .finally(() => {
        // 隐藏表格 loading
        tableLoading.value = false;
    });
}
getTableData()

// 每页展示数量变更事件
const handleSizeChange = (chooseSize) => {
    console.log('选择的页码' + chooseSize)
    size.value = chooseSize
    getTableData()
}

// 重置查询条件
const reset = () => {
    searchCategoryName.value = ''
    pickDate.value = ''
    startDate.value = ''
    endDate.value = ''
}

// 对话框是否显示
const formDialogRef = ref(null)

// 新增分类按钮点击事件
const addCategoryBtnClick = () => {
    formDialogRef.value.open()
}


// 表单引用
const formRef = ref(null)

// 添加文章分类表单对象
const form = reactive({
    categoryName: ''
})

// 规则校验
const rules = {
    categoryName: [
        { required: true, message: '分类名称不能为空', trigger: 'blur' },
        { min: 1, max: 20, message: '分类名称字数要求大于 1 个字符，小于 20 个字符', trigger: 'blur' },
    ]
}

// 提交表单（教程标准写法）
const onSubmit = () => {
    formRef.value.validate((valid) => {
        if (!valid) {
            console.log('表单验证不通过')
            return false
        }

        // 显示提交按钮 loading
        formDialogRef.value.showBtnLoading()

        addCategory(form).then((res) => {
            if (res.code === 0) {
                showMessage('添加成功')
                form.categoryName = ''
                formDialogRef.value.close()
                getTableData()
            } else {
                showMessage(res.message, 'error')
            }
        }).catch((err) => {
            console.error(err)
            showMessage('请求失败', 'error')
        }).finally(() => {
            // 隐藏提交按钮 loading
            formDialogRef.value.closeBtnLoading()
        })
    })
}

// 删除分类
const deleteCategorySubmit = (e, row) => {
    // 使按钮失焦
    e.currentTarget.blur()
    console.log(row)
    showModel('是否确定要删除该分类？').then(() => {
        deleteCategory(row.id).then((res) => {
            if (res.code === 0) {
                showMessage('删除成功')
                getTableData()
            } else {
                showMessage(res.message, 'error')
            }
        })
    }).catch(() => {
        console.log('取消了')
    })
}

</script>