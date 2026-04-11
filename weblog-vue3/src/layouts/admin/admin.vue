<template>
    <!-- 外层容器 -->
    <el-container>
    
        <!-- 左边侧边栏 -->
        <el-aside :width='menuStore.menuWidth' class="transition-all duration-300">
            <AdminMenu></AdminMenu>
        </el-aside>
        
        <!-- 右边主内容区域 -->
        <el-container class="h-screen flex flex-col">
            <!-- 顶栏容器 -->
            <el-header class="shrink-0">
                <AdminHeader></AdminHeader>
            </el-header>
            
            <!-- 标签导航栏 - 固定在顶部 -->
            <div class="sticky-taglist">
                <AdminTagList></AdminTagList>
            </div>

            <el-main class="overflow-y-auto">
                <!-- 主内容（根据路由动态展示不同页面） -->
                <router-view v-slot="{ Component }">
                    <Transition name="fade">
                        <!-- max 指定最多缓存 10 个组件 -->
                        <KeepAlive :max="10">
                            <component :is="Component"></component>
                        </KeepAlive>
                    </Transition>
                </router-view>
            </el-main>
            
            <!-- 底栏容器 -->
            <el-footer class="shrink-0">
                <AdminFooter></AdminFooter>
            </el-footer>
        </el-container>
    </el-container>
</template>

<script setup>
// 引入组件
import AdminFooter from './components/AdminFooter.vue';
import AdminHeader from './components/AdminHeader.vue';
import AdminMenu from './components/AdminMenu.vue';
import AdminTagList from './components/AdminTagList.vue';

import { useMenuStore } from '@/stores/menu'

const menuStore = useMenuStore()
</script>

<style scoped>
.el-header {
    padding: 0!important;
}

.sticky-taglist {
    position: sticky;
    top: 0;
    z-index: 10;
}

.el-footer {
    padding: 0!important;
}

/* 内容区域过渡动画：淡入淡出效果 */
/* 刚开始进入时 */
.fade-enter-from {
    /* 透明度 */
    opacity: 0;
}

/* 刚开始结束 */
.fade-enter-to {
    opacity: 1;
}

/* 刚开始离开 */
.fade-leave-from {
  opacity: 1;
}

/* 离开已结束 */
.fade-leave-to {
  opacity: 0;
}

/* 离开进行中 */
.fade-leave-active {
    transition: all 0.3s;
}

/* 进入进行中 */
.fade-enter-active {
    transition: all 0.3s;
    transition-delay: 0.3s;
}
</style>