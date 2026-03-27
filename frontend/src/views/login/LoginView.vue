<script setup lang="ts">
import { NButton, NForm, NFormItem, NInput } from 'naive-ui'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

import { hashPassword } from '@/utils/crypto'

const router = useRouter()
const username = ref('')
const password = ref('')

async function handleLogin() {
  // 与后端约定：登录请求体应传 SHA-256(loginName + easy_shm + 明文) 的 hex，此处先预哈希供后续接入登录接口使用
  await hashPassword(username.value.trim(), password.value)
  router.push('/dashboard/workspace')
}
</script>

<template>
  <n-form class="w-full max-w-sm" label-placement="left" label-width="72">
    <n-form-item label="用户名">
      <n-input v-model:value="username" placeholder="请输入用户名" />
    </n-form-item>
    <n-form-item label="密码">
      <n-input
        v-model:value="password"
        type="password"
        show-password-on="click"
        placeholder="请输入密码"
      />
    </n-form-item>
    <n-form-item>
      <n-button type="primary" block @click="handleLogin">登录</n-button>
    </n-form-item>
  </n-form>
</template>
