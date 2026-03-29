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
  NSelect,
  NSpace,
  useMessage,
} from 'naive-ui'
import type { DataTableColumns, FormInst, FormRules, SelectOption } from 'naive-ui'
import { h, onMounted, reactive, ref } from 'vue'

import { listDepartments } from '@/services/department'
import {
  createDeviceModel,
  deleteDeviceModel,
  listDeviceModels,
  updateDeviceModel,
} from '@/services/deviceModel'
import type { DeviceModel } from '@/types/deviceModel'

const message = useMessage()

const loading = ref(false)
const data = ref<DeviceModel[]>([])
const total = ref(0)
const searchManufacturer = ref('')
const searchDeviceType = ref('')
const searchModel = ref('')
const searchDepartmentId = ref<number | null>(null)
const pagination = reactive({ page: 1, size: 10 })

const departmentOptions = ref<SelectOption[]>([])
const departmentNameMap = ref<Record<number, string>>({})

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
    const res = await listDeviceModels({
      manufacturer: searchManufacturer.value || undefined,
      deviceType: searchDeviceType.value || undefined,
      model: searchModel.value || undefined,
      departmentId: searchDepartmentId.value ?? undefined,
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
const formModel = reactive<DeviceModel>({
  departmentId: 0,
  manufacturer: '',
  deviceType: '',
  model: '',
  range: '',
  principleType: '',
  sensitivity: '',
  accuracy: '',
  remark: '',
})
let editingId: number | null = null

const rules: FormRules = {
  manufacturer: [{ required: true, message: '请输入设备厂商', trigger: 'blur' }],
  deviceType: [{ required: true, message: '请输入设备类型', trigger: 'blur' }],
  model: [{ required: true, message: '请输入设备型号', trigger: 'blur' }],
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
}

function openCreate() {
  isEdit.value = false
  editingId = null
  Object.assign(formModel, {
    departmentId: departmentOptions.value[0]?.value != null ? Number(departmentOptions.value[0].value) : 0,
    manufacturer: '',
    deviceType: '',
    model: '',
    range: '',
    principleType: '',
    sensitivity: '',
    accuracy: '',
    remark: '',
  })
  showModal.value = true
}

function openEdit(row: DeviceModel) {
  isEdit.value = true
  editingId = row.id!
  Object.assign(formModel, {
    departmentId: row.departmentId,
    manufacturer: row.manufacturer ?? '',
    deviceType: row.deviceType,
    model: row.model,
    range: row.range ?? '',
    principleType: row.principleType ?? '',
    sensitivity: row.sensitivity ?? '',
    accuracy: row.accuracy ?? '',
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
    const payload: DeviceModel = {
      departmentId: formModel.departmentId,
      manufacturer: formModel.manufacturer?.trim() || undefined,
      deviceType: formModel.deviceType.trim(),
      model: formModel.model.trim(),
      range: formModel.range?.trim() || undefined,
      principleType: formModel.principleType?.trim() || undefined,
      sensitivity: formModel.sensitivity?.trim() || undefined,
      accuracy: formModel.accuracy?.trim() || undefined,
      remark: formModel.remark?.trim() || undefined,
    }
    if (isEdit.value && editingId !== null) {
      await updateDeviceModel(editingId, payload)
      message.success('修改成功')
    } else {
      await createDeviceModel(payload)
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
    await deleteDeviceModel(id)
    message.success('删除成功')
    fetchData()
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '删除失败')
  }
}

function formatValue(v: string | null | undefined) {
  if (v == null || v === '') return '—'
  return v
}

const columns: DataTableColumns<DeviceModel> = [
  {
    title: '设备厂商',
    key: 'manufacturer',
    minWidth: 140,
    render: (row) => formatValue(row.manufacturer),
  },
  {
    title: '设备类型',
    key: 'deviceType',
    minWidth: 120,
  },
  {
    title: '设备型号',
    key: 'model',
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
    title: '量程',
    key: 'range',
    width: 120,
    render: (row) => formatValue(row.range),
  },
  {
    title: '原理类型',
    key: 'principleType',
    width: 120,
    render: (row) => formatValue(row.principleType),
  },
  {
    title: '灵敏度',
    key: 'sensitivity',
    width: 100,
    render: (row) => formatValue(row.sensitivity),
  },
  {
    title: '精度',
    key: 'accuracy',
    width: 100,
    render: (row) => formatValue(row.accuracy),
  },
  {
    title: '备注',
    key: 'remark',
    minWidth: 140,
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
            default: () => '确认删除该设备型号？',
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
          v-model:value="searchManufacturer"
          placeholder="搜索设备厂商"
          clearable
          style="width: 180px"
          @keydown.enter="handleSearch"
        >
          <template #prefix>
            <n-icon :component="SearchOutline" />
          </template>
        </n-input>
        <n-input
          v-model:value="searchDeviceType"
          placeholder="搜索设备类型"
          clearable
          style="width: 180px"
          @keydown.enter="handleSearch"
        >
          <template #prefix>
            <n-icon :component="SearchOutline" />
          </template>
        </n-input>
        <n-input
          v-model:value="searchModel"
          placeholder="搜索设备型号"
          clearable
          style="width: 180px"
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
      :title="isEdit ? '编辑设备型号' : '新增设备型号'"
      style="width: 560px"
      :mask-closable="false"
    >
      <n-form ref="formRef" :model="formModel" :rules="rules" label-placement="left" label-width="88">
        <n-form-item label="设备厂商" path="manufacturer">
          <n-input v-model:value="formModel.manufacturer" placeholder="请输入设备厂商" />
        </n-form-item>
        <n-form-item label="设备类型" path="deviceType">
          <n-input v-model:value="formModel.deviceType" placeholder="请输入设备类型，如：传感器、采集仪" />
        </n-form-item>
        <n-form-item label="设备型号" path="model">
          <n-input v-model:value="formModel.model" placeholder="请输入设备型号" />
        </n-form-item>
        <n-form-item label="所属机构" path="departmentId">
          <n-select
            v-model:value="formModel.departmentId"
            :options="departmentOptions"
            placeholder="请选择机构"
            filterable
          />
        </n-form-item>
        <n-form-item label="量程" path="range">
          <n-input v-model:value="formModel.range" placeholder="选填，如：0-100mm" />
        </n-form-item>
        <n-form-item label="原理类型" path="principleType">
          <n-input v-model:value="formModel.principleType" placeholder="选填，如：振弦式、压阻式" />
        </n-form-item>
        <n-form-item label="灵敏度" path="sensitivity">
          <n-input v-model:value="formModel.sensitivity" placeholder="选填" />
        </n-form-item>
        <n-form-item label="精度" path="accuracy">
          <n-input v-model:value="formModel.accuracy" placeholder="选填" />
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
