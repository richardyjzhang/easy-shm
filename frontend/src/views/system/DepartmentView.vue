<script setup lang="ts">
import { AddOutline, CreateOutline, SearchOutline, TrashOutline } from '@vicons/ionicons5'
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
  NSpace,
  useMessage,
} from 'naive-ui'
import type { DataTableColumns, FormInst, FormRules } from 'naive-ui'
import { h, onMounted, reactive, ref } from 'vue'

import {
  createDepartment,
  deleteDepartment,
  listDepartments,
  updateDepartment,
} from '@/services/department'
import type { Department } from '@/types/department'

const message = useMessage()

const loading = ref(false)
const data = ref<Department[]>([])
const total = ref(0)
const searchName = ref('')
const pagination = reactive({ page: 1, size: 10 })

async function fetchData() {
  loading.value = true
  try {
    const res = await listDepartments({
      name: searchName.value || undefined,
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

// ---- 新增 / 编辑弹窗 ----
const showModal = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)
const formModel = reactive<Department>({
  name: '',
  contact: '',
  phone: '',
  address: '',
  description: '',
})
let editingId: number | null = null

const rules: FormRules = {
  name: [{ required: true, message: '请输入机构名称', trigger: 'blur' }],
}

function openCreate() {
  isEdit.value = false
  editingId = null
  Object.assign(formModel, { name: '', contact: '', phone: '', address: '', description: '' })
  showModal.value = true
}

function openEdit(row: Department) {
  isEdit.value = true
  editingId = row.id!
  Object.assign(formModel, {
    name: row.name,
    contact: row.contact ?? '',
    phone: row.phone ?? '',
    address: row.address ?? '',
    description: row.description ?? '',
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
    if (isEdit.value && editingId !== null) {
      await updateDepartment(editingId, { ...formModel })
      message.success('修改成功')
    } else {
      await createDepartment({ ...formModel })
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
    await deleteDepartment(id)
    message.success('删除成功')
    fetchData()
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '删除失败')
  }
}

const columns: DataTableColumns<Department> = [
  { title: '机构名称', key: 'name', minWidth: 140 },
  { title: '联系人', key: 'contact', width: 120 },
  { title: '联系电话', key: 'phone', width: 140 },
  { title: '地址', key: 'address', minWidth: 180 },
  { title: '备注', key: 'description', minWidth: 160, ellipsis: { tooltip: true } },
  {
    title: '创建时间',
    key: 'createdAt',
    width: 180,
    render: (row) => row.createdAt?.replace('T', ' ') ?? '',
  },
  {
    title: '操作',
    key: 'actions',
    width: 140,
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
            default: () => '确认删除该机构？',
          },
        ),
      ])
    },
  },
]

onMounted(fetchData)
</script>

<template>
  <div class="flex flex-col gap-3">
    <!-- 搜索栏 -->
    <div class="flex items-center justify-between">
      <n-space>
        <n-input
          v-model:value="searchName"
          placeholder="搜索机构名称"
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

    <!-- 表格 -->
    <n-data-table
      :columns="columns"
      :data="data"
      :loading="loading"
      :bordered="true"
      :single-line="false"
    />

    <!-- 分页 -->
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

    <!-- 新增/编辑弹窗 -->
    <n-modal
      v-model:show="showModal"
      preset="card"
      :title="isEdit ? '编辑机构' : '新增机构'"
      style="width: 520px"
      :mask-closable="false"
    >
      <n-form ref="formRef" :model="formModel" :rules="rules" label-placement="left" label-width="80">
        <n-form-item label="机构名称" path="name">
          <n-input v-model:value="formModel.name" placeholder="请输入机构名称" />
        </n-form-item>
        <n-form-item label="联系人" path="contact">
          <n-input v-model:value="formModel.contact" placeholder="请输入联系人" />
        </n-form-item>
        <n-form-item label="联系电话" path="phone">
          <n-input v-model:value="formModel.phone" placeholder="请输入联系电话" />
        </n-form-item>
        <n-form-item label="地址" path="address">
          <n-input v-model:value="formModel.address" placeholder="请输入地址" />
        </n-form-item>
        <n-form-item label="备注" path="description">
          <n-input
            v-model:value="formModel.description"
            type="textarea"
            placeholder="请输入备注"
            :rows="3"
          />
        </n-form-item>
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
