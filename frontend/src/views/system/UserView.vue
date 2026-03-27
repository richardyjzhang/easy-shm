<script setup lang="ts">
import {
  AddOutline,
  CreateOutline,
  KeyOutline,
  SearchOutline,
  TrashOutline,
} from '@vicons/ionicons5'
import {
  NButton,
  NDataTable,
  NForm,
  NFormItem,
  NIcon,
  NInput,
  NModal,
  NPagination,
  NPopconfirm,
  NSelect,
  NSpace,
  NTag,
  useMessage,
} from 'naive-ui'
import type { DataTableColumns, FormInst, FormRules, SelectOption } from 'naive-ui'
import { h, onMounted, reactive, ref } from 'vue'

import { listDepartments } from '@/services/department'
import {
  createUser,
  deleteUser,
  listUsers,
  resetUserPassword,
  updateUser,
} from '@/services/user'
import type { User } from '@/types/user'
import { hashPassword } from '@/utils/crypto'

const message = useMessage()

const loading = ref(false)
const data = ref<User[]>([])
const total = ref(0)
const searchLoginName = ref('')
const pagination = reactive({ page: 1, size: 10 })

const departmentOptions = ref<SelectOption[]>([])
const departmentNameMap = ref<Record<number, string>>({})

const roleOptions: SelectOption[] = [
  { label: '系统管理员', value: 1 },
  { label: '机构管理员', value: 2 },
  { label: '机构用户', value: 3 },
]

function roleLabel(role: number) {
  const m: Record<number, string> = { 1: '系统管理员', 2: '机构管理员', 3: '机构用户' }
  return m[role] ?? String(role)
}

function roleTagType(role: number): 'default' | 'info' | 'success' | 'warning' | 'error' {
  if (role === 1) return 'error'
  if (role === 2) return 'warning'
  return 'info'
}

async function loadDepartments() {
  try {
    const res = await listDepartments({ page: 1, size: 500 })
    const list = res.data.content
    const map: Record<number, string> = {}
    departmentOptions.value = list.map((d) => {
      const id = d.id!
      map[id] = d.name
      return { label: d.name, value: id }
    })
    departmentNameMap.value = map
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '加载机构列表失败')
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await listUsers({
      loginName: searchLoginName.value || undefined,
      page: pagination.page,
      size: pagination.size,
    })
    data.value = res.data.content
    total.value = res.data.totalElements
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '加载失败')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handlePageChange(page: number) {
  pagination.page = page
  fetchData()
}

function handlePageSizeChange(size: number) {
  pagination.size = size
  pagination.page = 1
  fetchData()
}

const showModal = ref(false)
/** create / edit 用户；reset 仅重置密码（同一弹窗内仅两行密码） */
const modalMode = ref<'create' | 'edit' | 'reset'>('create')
const formRef = ref<FormInst | null>(null)
const formModel = reactive<
  User & {
    password?: string
    passwordConfirm?: string
  }
>({
  loginName: '',
  password: '',
  passwordConfirm: '',
  departmentId: 0,
  role: 3,
  phone: '',
  address: '',
  remark: '',
})
let editingId: number | null = null

const rules: FormRules = {
  loginName: [
    {
      validator: () => {
        if (modalMode.value === 'reset') return true
        if (!String(formModel.loginName ?? '').trim()) {
          return new Error('请输入登录名')
        }
        return true
      },
      trigger: 'blur',
    },
  ],
  password: [
    {
      validator: () => {
        if (modalMode.value === 'edit') return true
        if (!String(formModel.password ?? '').trim()) {
          return new Error(modalMode.value === 'reset' ? '请输入新密码' : '请输入密码')
        }
        return true
      },
      trigger: 'blur',
    },
  ],
  passwordConfirm: [
    {
      validator: () => {
        if (modalMode.value === 'edit') return true
        const p = String(formModel.password ?? '').trim()
        const c = String(formModel.passwordConfirm ?? '').trim()
        if (!c) {
          return new Error('请再次输入密码')
        }
        if (p !== c) {
          return new Error('两次输入的密码不一致')
        }
        return true
      },
      trigger: 'blur',
    },
  ],
  departmentId: [
    {
      validator: () => {
        if (modalMode.value === 'reset') return true
        if (!formModel.departmentId) {
          return new Error('请选择所属机构')
        }
        return true
      },
      trigger: ['blur', 'change'],
    },
  ],
  role: [
    {
      validator: () => {
        if (modalMode.value === 'reset') return true
        if (formModel.role == null) {
          return new Error('请选择角色')
        }
        return true
      },
      trigger: 'change',
    },
  ],
}

function openCreate() {
  modalMode.value = 'create'
  editingId = null
  Object.assign(formModel, {
    loginName: '',
    password: '',
    passwordConfirm: '',
    departmentId: departmentOptions.value[0]?.value != null ? Number(departmentOptions.value[0].value) : 0,
    role: 3,
    phone: '',
    address: '',
    remark: '',
  })
  showModal.value = true
}

function openEdit(row: User) {
  modalMode.value = 'edit'
  editingId = row.id!
  Object.assign(formModel, {
    loginName: row.loginName,
    password: '',
    passwordConfirm: '',
    departmentId: row.departmentId,
    role: row.role,
    phone: row.phone ?? '',
    address: row.address ?? '',
    remark: row.remark ?? '',
  })
  showModal.value = true
}

const submitting = ref(false)

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  submitting.value = true
  try {
    if (modalMode.value === 'reset') {
      if (!resetTarget.value) return
      const hashed = await hashPassword(resetTarget.value.loginName, formModel.password!.trim())
      await resetUserPassword(resetTarget.value.id, hashed)
      message.success('密码已重置')
    } else if (modalMode.value === 'edit' && editingId !== null) {
      await updateUser(editingId, {
        loginName: formModel.loginName,
        departmentId: formModel.departmentId,
        role: formModel.role,
        phone: formModel.phone || undefined,
        address: formModel.address || undefined,
        remark: formModel.remark || undefined,
      })
      message.success('修改成功')
    } else {
      const hashed = await hashPassword(formModel.loginName.trim(), formModel.password!.trim())
      await createUser({
        loginName: formModel.loginName.trim(),
        departmentId: formModel.departmentId,
        role: formModel.role,
        phone: formModel.phone || undefined,
        address: formModel.address || undefined,
        remark: formModel.remark || undefined,
        password: hashed,
      })
      message.success('新增成功')
    }
    showModal.value = false
    fetchData()
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '操作失败')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id: number) {
  try {
    await deleteUser(id)
    message.success('删除成功')
    fetchData()
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '删除失败')
  }
}

const resetTarget = ref<{ id: number; loginName: string } | null>(null)

function openReset(row: User) {
  modalMode.value = 'reset'
  resetTarget.value = { id: row.id!, loginName: row.loginName }
  formModel.password = ''
  formModel.passwordConfirm = ''
  showModal.value = true
}

const columns: DataTableColumns<User> = [
  { title: '登录名', key: 'loginName', minWidth: 120 },
  {
    title: '所属机构',
    key: 'departmentId',
    minWidth: 140,
    render: (row) => departmentNameMap.value[row.departmentId] ?? row.departmentId,
  },
  {
    title: '角色',
    key: 'role',
    width: 120,
    render: (row) =>
      h(NTag, { type: roleTagType(row.role), size: 'small' }, { default: () => roleLabel(row.role) }),
  },
  { title: '联系电话', key: 'phone', width: 130 },
  { title: '地址', key: 'address', minWidth: 140 },
  { title: '备注', key: 'remark', minWidth: 120, ellipsis: { tooltip: true } },
  {
    title: '创建时间',
    key: 'createdAt',
    width: 180,
    render: (row) => row.createdAt?.replace('T', ' ') ?? '',
  },
  {
    title: '操作',
    key: 'actions',
    width: 248,
    render(row) {
      return h(NSpace, { size: 4 }, () => [
        h(
          NButton,
          { text: true, type: 'primary', size: 'small', onClick: () => openEdit(row) },
          {
            default: () => '编辑',
            icon: () => h(NIcon, { component: CreateOutline, size: 16 }),
          },
        ),
        h(
          NButton,
          { text: true, type: 'primary', size: 'small', onClick: () => openReset(row) },
          {
            default: () => '重置密码',
            icon: () => h(NIcon, { component: KeyOutline, size: 16 }),
          },
        ),
        h(
          NPopconfirm,
          { onPositiveClick: () => handleDelete(row.id!) },
          {
            trigger: () =>
              h(
                NButton,
                { text: true, type: 'error', size: 'small' },
                {
                  default: () => '删除',
                  icon: () => h(NIcon, { component: TrashOutline, size: 16 }),
                },
              ),
            default: () => '确认删除该用户？',
          },
        ),
      ])
    },
  },
]

onMounted(async () => {
  await loadDepartments()
  fetchData()
})
</script>

<template>
  <div class="flex flex-col gap-3">
    <div class="flex items-center justify-between">
      <n-space>
        <n-input
          v-model:value="searchLoginName"
          placeholder="搜索登录名"
          clearable
          style="width: 240px"
          @keydown.enter="handleSearch"
        >
          <template #prefix>
            <n-icon :component="SearchOutline" />
          </template>
        </n-input>
        <n-button type="primary" @click="handleSearch">查询</n-button>
      </n-space>
      <n-button type="primary" @click="openCreate">
        <template #icon>
          <n-icon :component="AddOutline" />
        </template>
        新增
      </n-button>
    </div>

    <n-data-table
      :columns="columns"
      :data="data"
      :loading="loading"
      :bordered="true"
      :single-line="false"
    />

    <div class="flex justify-end">
      <n-pagination
        v-model:page="pagination.page"
        v-model:page-size="pagination.size"
        :item-count="total"
        show-size-picker
        :page-sizes="[10, 20, 30, 50]"
        @update:page="handlePageChange"
        @update:page-size="handlePageSizeChange"
      />
    </div>

    <n-modal
      v-model:show="showModal"
      preset="card"
      :title="
        modalMode === 'reset' ? '重置密码' : modalMode === 'edit' ? '编辑用户' : '新增用户'
      "
      :style="{ width: modalMode === 'reset' ? '420px' : '520px' }"
      :mask-closable="false"
    >
      <n-form ref="formRef" :model="formModel" :rules="rules" label-placement="left" label-width="88">
        <template v-if="modalMode === 'reset'">
          <n-form-item label="新密码" path="password">
            <n-input
              v-model:value="formModel.password"
              type="password"
              show-password-on="click"
              placeholder="请输入新密码"
            />
          </n-form-item>
          <n-form-item label="确认密码" path="passwordConfirm">
            <n-input
              v-model:value="formModel.passwordConfirm"
              type="password"
              show-password-on="click"
              placeholder="请再次输入新密码"
            />
          </n-form-item>
        </template>
        <template v-else>
          <n-form-item label="登录名" path="loginName">
            <n-input v-model:value="formModel.loginName" placeholder="请输入登录名" />
          </n-form-item>
          <template v-if="modalMode === 'create'">
            <n-form-item label="密码" path="password">
              <n-input
                v-model:value="formModel.password"
                type="password"
                show-password-on="click"
                placeholder="请输入密码"
              />
            </n-form-item>
            <n-form-item label="确认密码" path="passwordConfirm">
              <n-input
                v-model:value="formModel.passwordConfirm"
                type="password"
                show-password-on="click"
                placeholder="请再次输入密码"
              />
            </n-form-item>
          </template>
          <n-form-item label="所属机构" path="departmentId">
            <n-select
              v-model:value="formModel.departmentId"
              :options="departmentOptions"
              placeholder="请选择机构"
              filterable
            />
          </n-form-item>
          <n-form-item label="角色" path="role">
            <n-select v-model:value="formModel.role" :options="roleOptions" placeholder="请选择角色" />
          </n-form-item>
          <n-form-item label="联系电话" path="phone">
            <n-input v-model:value="formModel.phone" placeholder="选填" />
          </n-form-item>
          <n-form-item label="地址" path="address">
            <n-input v-model:value="formModel.address" placeholder="选填" />
          </n-form-item>
          <n-form-item label="备注" path="remark">
            <n-input v-model:value="formModel.remark" type="textarea" placeholder="选填" :rows="3" />
          </n-form-item>
        </template>
      </n-form>
      <template #action>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>
