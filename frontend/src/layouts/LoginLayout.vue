<script setup lang="ts">
import { PulseOutline } from '@vicons/ionicons5'
import { NIcon } from 'naive-ui'
</script>

<template>
  <div
    class="flex min-h-full flex-col items-center justify-center bg-linear-to-b from-slate-100 via-slate-50 to-white p-6"
  >
    <div class="mb-10 text-center">
      <div
        class="mx-auto mb-5 flex h-16 w-16 items-center justify-center rounded-lg bg-linear-to-br from-primary to-primary-hover text-white shadow-lg shadow-primary/30"
      >
        <n-icon :component="PulseOutline" :size="36" />
      </div>
      <h1 class="text-2xl font-semibold tracking-tight text-slate-800">结构健康监测系统</h1>
      <p class="mt-2 text-sm text-slate-500">请登录后继续</p>
    </div>
    <div
      class="w-full max-w-md rounded-lg border border-slate-200/80 bg-white/90 p-8 shadow-lg shadow-slate-200/50 backdrop-blur-sm"
    >
      <router-view />
    </div>
  </div>
</template>
