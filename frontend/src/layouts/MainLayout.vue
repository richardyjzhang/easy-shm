<script setup lang="ts">
import { LogOutOutline, PulseOutline } from '@vicons/ionicons5'
import {
  NButton,
  NCard,
  NIcon,
  NLayout,
  NLayoutContent,
  NLayoutHeader,
  NLayoutSider,
  NMenu,
  NSpace,
} from 'naive-ui'
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { menuOptions } from '@/config/menu'

const route = useRoute()
const router = useRouter()
const collapsed = ref(false)

const activeKey = computed(() => route.path)

/** 与侧栏菜单文案一致（路由 meta.title） */
const pageTitle = computed(() => (route.meta.title as string) ?? '页面')

function handleMenuUpdate(key: string | number) {
  const k = String(key)
  if (k.startsWith('/')) {
    router.push(k)
  }
}

function goLogin() {
  router.push('/login')
}
</script>

<template>
  <!-- 视口高度 + 纵向 flex，保证 header 以下占满剩余高度 -->
  <div class="flex h-dvh min-h-0 flex-col overflow-hidden">
    <n-layout-header
      bordered
      class="flex h-14 shrink-0 items-center justify-between bg-white/90 px-4 backdrop-blur-sm"
      style="height: 56px"
    >
      <div class="flex min-w-0 items-center gap-2.5">
        <span
          class="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-linear-to-br from-primary to-primary-hover text-white shadow-md shadow-primary/25"
        >
          <n-icon :component="PulseOutline" :size="20" />
        </span>
        <span class="truncate text-xl font-semibold tracking-tight text-slate-800"
          >结构健康监测系统</span
        >
      </div>
      <n-space align="center" :size="14">
        <span
          class="max-w-[min(50vw,22rem)] shrink truncate text-sm tracking-tight text-slate-500 md:max-w-none"
          title="Structural Health Monitoring"
        >
          Structural Health Monitoring
        </span>
        <n-button quaternary size="small" @click="goLogin">
          <template #icon>
            <n-icon :component="LogOutOutline" />
          </template>
          退出
        </n-button>
      </n-space>
    </n-layout-header>

    <n-layout
      has-sider
      class="min-h-0 flex-1 overflow-hidden"
      content-style="display: flex; min-height: 0; flex: 1; height: 100%; overflow: hidden;"
    >
      <n-layout-sider
        v-model:collapsed="collapsed"
        bordered
        collapse-mode="width"
        :collapsed-width="64"
        :width="232"
        show-trigger
        content-style="display: flex; flex-direction: column; min-height: 0; height: 100%;"
      >
        <n-menu
          class="min-h-0 flex-1 overflow-y-auto pt-2"
          :value="activeKey"
          :collapsed="collapsed"
          :collapsed-width="64"
          :options="menuOptions"
          :indent="20"
          :dropdown-props="{ trigger: 'click' }"
          default-expand-all
          @update:value="handleMenuUpdate"
        />
      </n-layout-sider>
      <n-layout-content
        content-style="padding: 0; min-height: 0; flex: 1; height: 100%; overflow: hidden; background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);"
      >
        <div class="box-border flex h-full min-h-0 flex-col p-4 pb-6">
          <n-card class="main-content-card min-h-0 flex-1 overflow-hidden" bordered size="small">
            <template #header>
              <span class="text-base font-semibold tracking-tight text-slate-800">{{
                pageTitle
              }}</span>
            </template>
            <router-view />
          </n-card>
        </div>
      </n-layout-content>
    </n-layout>
  </div>
</template>

<style scoped>
/* 卡片铺满主内容区，内容区可滚动 */
.main-content-card {
  display: flex;
  flex-direction: column;
  flex: 1 1 0;
  min-height: 0;
  height: 100%;
}
.main-content-card :deep(.n-card-content) {
  flex: 1 1 0;
  min-height: 0;
  overflow: auto;
}
</style>
