<script setup lang="ts">
import { NButton, NForm, NFormItem, NInput } from 'naive-ui'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const username = ref('')
const password = ref('')

function handleLogin() {
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
