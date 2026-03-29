<script setup lang="ts">
import { AddOutline, CreateOutline, SearchOutline, TrashOutline } from '@vicons/ionicons5'
import {
  NButton,
  NDataTable,
  NDatePicker,
  NForm,
  NFormItem,
  NIcon,
  NInput,
  NModal,
  NPagination,
  NPopconfirm,
  NSelect,
  NSpace,
  useMessage,
} from 'naive-ui'
import type { DataTableColumns, FormInst, FormRules, SelectOption } from 'naive-ui'
import { h, onMounted, reactive, ref } from 'vue'

import { listDepartments } from '@/services/department'
import { listDeviceModels } from '@/services/deviceModel'
import {
  createMonitorDevice,
  deleteMonitorDevice,
  listMonitorDevices,
  updateMonitorDevice,
} from '@/services/monitorDevice'
import type { DeviceModel } from '@/types/deviceModel'
import type { MonitorDevice } from '@/types/monitorDevice'

const message = useMessage()

const loading = ref(false)
const data = ref<MonitorDevice[]>([])
const total = ref(0)
const searchSn = ref('')
const searchDepartmentId = ref<number | null>(null)
const searchDeviceModelId = ref<number | null>(null)
const pagination = reactive({ page: 1, size: 10 })

const departmentOptions = ref<SelectOption[]>([])
const departmentNameMap = ref<Record<number, string>>({})
const deviceModelOptions = ref<SelectOption[]>([])
const deviceModelNameMap = ref<Record<number, string>>({})

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

async function loadDeviceModels() {
  try {
    const res = await listDeviceModels({ page: 1, size: 500 })
    const list = res.data.content
    const map: Record<number, string> = {}
    deviceModelOptions.value = list.map((d) => {
      const id = d.id!
      const name = `${d.manufacturer} - ${d.model}`
      map[id] = name
      return { label: name, value: id }
    })
    deviceModelNameMap.value = map
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '加载设备型号列表失败')
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await listMonitorDevices({
      sn: searchSn.value || undefined,
      departmentId: searchDepartmentId.value ?? undefined,
      deviceModelId: searchDeviceModelId.value ?? undefined,
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
const isEdit = ref(false)
const formRef = ref<FormInst | null>(null)
const formModel = reactive<MonitorDevice>({
  departmentId: 0,
  deviceModelId: 0,
  sn: '',
  productionDate: undefined,
  remark: '',
})
let editingId: number | null = null

const rules: FormRules = {
  sn: [{ required: true, message: '请输入序列号', trigger: 'blur' }],
  departmentId: [
    {
      validator: () => {
        if (!formModel.departmentId) {
          return new Error('请选择所属机构')
        }
        return true
      },
      trigger: ['blur', 'change'],
    },
  ],
  deviceModelId: [
    {
      validator: () => {
        if (!formModel.deviceModelId) {
          return new Error('请选择设备型号')
        }
        return true
      },
      trigger: ['blur', 'change'],
    },
  ],
}

function openCreate() {
  isEdit.value = false
  editingId = null
  Object.assign(formModel, {
    departmentId: departmentOptions.value[0]?.value != null ? Number(departmentOptions.value[0].value) : 0,
    deviceModelId: deviceModelOptions.value[0]?.value != null ? Number(deviceModelOptions.value[0].value) : 0,
    sn: '',
    productionDate: undefined,
    remark: '',
  })
  showModal.value = true
}

function openEdit(row: MonitorDevice) {
  isEdit.value = true
  editingId = row.id!
  Object.assign(formModel, {
    departmentId: row.departmentId,
    deviceModelId: row.deviceModelId,
    sn: row.sn,
    productionDate: row.productionDate ? new Date(row.productionDate).getTime() : undefined,
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
    const payload: MonitorDevice = {
      departmentId: formModel.departmentId,
      deviceModelId: formModel.deviceModelId,
      sn: formModel.sn.trim(),
      productionDate: formModel.productionDate ? formatDate(formModel.productionDate) : undefined,
      remark: formModel.remark?.trim() || undefined,
    }
    if (isEdit.value && editingId !== null) {
      await updateMonitorDevice(editingId, payload)
      message.success('修改成功')
    } else {
      await createMonitorDevice(payload)
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
    await deleteMonitorDevice(id)
    message.success('删除成功')
    fetchData()
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '删除失败')
  }
}

function formatDate(timestamp: number | string): string {
  const date = new Date(timestamp)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function formatValue(v: string | null | undefined) {
  if (v == null || v === '') return '—'
  return v
}

const columns: DataTableColumns<MonitorDevice> = [
  {
    title: '序列号',
    key: 'sn',
    minWidth: 140,
    ellipsis: { tooltip: true },
  },
  {
    title: '所属机构',
    key: 'departmentId',
    minWidth: 140,
    render: (row) => departmentNameMap.value[row.departmentId] ?? row.departmentId,
  },
  {
    title: '设备型号',
    key: 'deviceModelId',
    minWidth: 200,
    render: (row) => deviceModelNameMap.value[row.deviceModelId] ?? row.deviceModelId,
  },
  {
    title: '出厂日期',
    key: 'productionDate',
    width: 120,
    render: (row) => formatValue(row.productionDate),
  },
  {
    title: '备注',
    key: 'remark',
    minWidth: 200,
    ellipsis: { tooltip: true },
    render: (row) => formatValue(row.remark),
  },
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
            default: () => '确认删除该监测设备？',
          },
        ),
      ])
    },
  },
]

onMounted(async () => {
  await Promise.all([loadDepartments(), loadDeviceModels()])
  fetchData()
})
</script>

<template>
  <div class="flex flex-col gap-3">
    <div class="flex items-center justify-between">
      <n-space>
        <n-input
          v-model:value="searchSn"
          placeholder="搜索序列号"
          clearable
          style="width: 200px"
          @keydown.enter="handleSearch"
        >
          <template #prefix>
            <n-icon :component="SearchOutline" />
          </template>
        </n-input>
        <n-select
          v-model:value="searchDepartmentId"
          :options="departmentOptions"
          placeholder="所属机构"
          clearable
          filterable
          style="width: 180px"
        />
        <n-select
          v-model:value="searchDeviceModelId"
          :options="deviceModelOptions"
          placeholder="设备型号"
          clearable
          filterable
          style="width: 220px"
        />
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

    <!-- 新增/编辑 -->
    <n-modal
      v-model:show="showModal"
      preset="card"
      :title="isEdit ? '编辑监测设备' : '新增监测设备'"
      style="width: 520px"
      :mask-closable="false"
    >
      <n-form ref="formRef" :model="formModel" :rules="rules" label-placement="left" label-width="88">
        <n-form-item label="序列号" path="sn">
          <n-input v-model:value="formModel.sn" placeholder="请输入序列号" />
        </n-form-item>
        <n-form-item label="所属机构" path="departmentId">
          <n-select
            v-model:value="formModel.departmentId"
            :options="departmentOptions"
            placeholder="请选择机构"
            filterable
          />
        </n-form-item>
        <n-form-item label="设备型号" path="deviceModelId">
          <n-select
            v-model:value="formModel.deviceModelId"
            :options="deviceModelOptions"
            placeholder="请选择设备型号"
            filterable
          />
        </n-form-item>
        <n-form-item label="出厂日期" path="productionDate">
          <n-date-picker
            v-model:value="formModel.productionDate"
            type="date"
            placeholder="选择出厂日期"
            style="width: 100%"
            clearable
          />
        </n-form-item>
        <n-form-item label="备注" path="remark">
          <n-input v-model:value="formModel.remark" type="textarea" placeholder="选填" :rows="3" />
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
