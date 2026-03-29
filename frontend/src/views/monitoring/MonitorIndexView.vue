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
  NRadio,
  NSelect,
  NSpace,
  useMessage,
} from 'naive-ui'
import type { DataTableColumns, FormInst, FormRules, SelectOption } from 'naive-ui'
import { h, onMounted, reactive, ref, watch } from 'vue'

import { listDepartments } from '@/services/department'
import {
  createMonitorIndex,
  createMonitorValueType,
  deleteMonitorIndex,
  deleteMonitorValueType,
  listMonitorIndexes,
  listMonitorValueTypes,
  updateMonitorIndex,
  updateMonitorValueType,
} from '@/services/monitorIndex'
import type { MonitorIndex, MonitorValueType } from '@/types/monitorIndex'

const message = useMessage()

const TYPE_OPTIONS = [
  { label: '环境', value: 1 },
  { label: '作用', value: 2 },
  { label: '结构响应', value: 3 },
  { label: '结构变化', value: 4 },
] as const

const typeMap: Record<number, string> = {
  1: '环境',
  2: '作用',
  3: '结构响应',
  4: '结构变化',
}

// ===================== 左侧：监测项 =====================
const leftLoading = ref(false)
const leftData = ref<MonitorIndex[]>([])
const leftTotal = ref(0)
const selectedIndexId = ref<number | null>(null)

const searchName = ref('')
const searchDepartmentId = ref<number | null>(null)
const searchType = ref<number | null>(null)
const leftPagination = reactive({ page: 1, size: 10 })

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

async function fetchLeftData() {
  leftLoading.value = true
  try {
    const res = await listMonitorIndexes({
      name: searchName.value || undefined,
      departmentId: searchDepartmentId.value ?? undefined,
      type: searchType.value ?? undefined,
      page: leftPagination.page,
      size: leftPagination.size,
    })
    leftData.value = res.data.content
    leftTotal.value = res.data.totalElements

    // 默认选中第一条
    if (leftData.value.length > 0) {
      const firstId = leftData.value[0].id!
      if (selectedIndexId.value !== firstId) {
        selectedIndexId.value = firstId
      }
    } else {
      selectedIndexId.value = null
    }
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '加载监测项失败')
  } finally {
    leftLoading.value = false
  }
}

function handleLeftSearch() {
  leftPagination.page = 1
  fetchLeftData()
}

function handleLeftPageChange(page: number) {
  leftPagination.page = page
  fetchLeftData()
}

function handleLeftPageSizeChange(size: number) {
  leftPagination.size = size
  leftPagination.page = 1
  fetchLeftData()
}

function onSelectIndex(id: number) {
  selectedIndexId.value = id
}

// ===================== 监测项弹窗 =====================
const showIndexModal = ref(false)
const isIndexEdit = ref(false)
const indexFormRef = ref<FormInst | null>(null)
const indexFormModel = reactive<MonitorIndex>({
  name: '',
  type: 1,
  departmentId: 0,
  code: '',
})
let editingIndexId: number | null = null

const indexRules: FormRules = {
  name: [{ required: true, message: '请输入监测项名称', trigger: 'blur' }],
  type: [{ required: true, type: 'number', message: '请选择监测项类型', trigger: 'change' }],
  departmentId: [
    {
      validator: () => {
        if (!indexFormModel.departmentId) {
          return new Error('请选择所属机构')
        }
        return true
      },
      trigger: ['blur', 'change'],
    },
  ],
}

function openCreateIndex() {
  isIndexEdit.value = false
  editingIndexId = null
  Object.assign(indexFormModel, {
    name: '',
    type: 1,
    departmentId: departmentOptions.value[0]?.value != null ? Number(departmentOptions.value[0].value) : 0,
    code: '',
  })
  showIndexModal.value = true
}

function openEditIndex(row: MonitorIndex) {
  isIndexEdit.value = true
  editingIndexId = row.id!
  Object.assign(indexFormModel, {
    name: row.name,
    type: row.type,
    departmentId: row.departmentId,
    code: row.code ?? '',
  })
  showIndexModal.value = true
}

const submittingIndex = ref(false)

async function handleSubmitIndex() {
  try {
    await indexFormRef.value?.validate()
  } catch {
    return
  }
  submittingIndex.value = true
  try {
    const payload: MonitorIndex = {
      name: indexFormModel.name.trim(),
      type: indexFormModel.type,
      departmentId: indexFormModel.departmentId,
      code: indexFormModel.code?.trim() || undefined,
    }
    if (isIndexEdit.value && editingIndexId !== null) {
      await updateMonitorIndex(editingIndexId, payload)
      message.success('修改成功')
    } else {
      await createMonitorIndex(payload)
      message.success('新增成功')
    }
    showIndexModal.value = false
    fetchLeftData()
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '操作失败')
  } finally {
    submittingIndex.value = false
  }
}

async function handleDeleteIndex(id: number) {
  try {
    await deleteMonitorIndex(id)
    message.success('删除成功')
    // 如果删除的是当前选中的，右侧会自动清空
    fetchLeftData()
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '删除失败')
  }
}

// 左侧表格列
const leftColumns: DataTableColumns<MonitorIndex> = [
  {
    title: '',
    key: 'select',
    width: 50,
    fixed: 'left',
    render(row) {
      return h(NRadio, {
        checked: selectedIndexId.value === row.id,
        onUpdateChecked: () => onSelectIndex(row.id!),
      })
    },
  },
  { title: '监测项名称', key: 'name', minWidth: 140, ellipsis: { tooltip: true } },
  { title: '编码', key: 'code', width: 100, render: (row) => row.code || '—' },
  { title: '类型', key: 'type', width: 100, render: (row) => typeMap[row.type] ?? row.type },
  {
    title: '所属机构',
    key: 'departmentId',
    minWidth: 140,
    ellipsis: { tooltip: true },
    render: (row) => departmentNameMap.value[row.departmentId] ?? row.departmentId,
  },
  {
    title: '操作',
    key: 'actions',
    width: 140,
    fixed: 'right',
    render(row) {
      return h(NSpace, { size: 4 }, () => [
        h(
          NButton,
          { text: true, type: 'primary', size: 'small', onClick: () => openEditIndex(row) },
          {
            default: () => '编辑',
            icon: () => h(NIcon, { component: CreateOutline, size: 16 }),
          },
        ),
        h(
          NPopconfirm,
          { onPositiveClick: () => handleDeleteIndex(row.id!) },
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
            default: () => '确认删除该监测项？其下所有监测内容也将被删除。',
          },
        ),
      ])
    },
  },
]

// ===================== 右侧：监测内容 =====================
const rightLoading = ref(false)
const rightData = ref<MonitorValueType[]>([])
const rightTotal = ref(0)
const rightPagination = reactive({ page: 1, size: 10 })

async function fetchRightData() {
  if (!selectedIndexId.value) {
    rightData.value = []
    rightTotal.value = 0
    return
  }
  rightLoading.value = true
  try {
    const res = await listMonitorValueTypes({
      monitorIndexId: selectedIndexId.value,
      page: rightPagination.page,
      size: rightPagination.size,
    })
    rightData.value = res.data.content
    rightTotal.value = res.data.totalElements
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '加载监测内容失败')
  } finally {
    rightLoading.value = false
  }
}

function handleRightPageChange(page: number) {
  rightPagination.page = page
  fetchRightData()
}

function handleRightPageSizeChange(size: number) {
  rightPagination.size = size
  rightPagination.page = 1
  fetchRightData()
}

// 监听选中的监测项变化，刷新右侧
watch(selectedIndexId, () => {
  rightPagination.page = 1
  fetchRightData()
})

// ===================== 监测内容弹窗 =====================
const showValueTypeModal = ref(false)
const isValueTypeEdit = ref(false)
const valueTypeFormRef = ref<FormInst | null>(null)
const valueTypeFormModel = reactive<MonitorValueType>({
  monitorIndexId: 0,
  name: '',
  unit: '',
})
let editingValueTypeId: number | null = null

const valueTypeRules: FormRules = {
  name: [{ required: true, message: '请输入监测内容名称', trigger: 'blur' }],
}

function openCreateValueType() {
  if (!selectedIndexId.value) {
    message.warning('请先选择左侧的一个监测项')
    return
  }
  isValueTypeEdit.value = false
  editingValueTypeId = null
  Object.assign(valueTypeFormModel, {
    monitorIndexId: selectedIndexId.value,
    name: '',
    unit: '',
  })
  showValueTypeModal.value = true
}

function openEditValueType(row: MonitorValueType) {
  isValueTypeEdit.value = true
  editingValueTypeId = row.id!
  Object.assign(valueTypeFormModel, {
    monitorIndexId: row.monitorIndexId,
    name: row.name,
    unit: row.unit ?? '',
  })
  showValueTypeModal.value = true
}

const submittingValueType = ref(false)

async function handleSubmitValueType() {
  try {
    await valueTypeFormRef.value?.validate()
  } catch {
    return
  }
  submittingValueType.value = true
  try {
    const payload: MonitorValueType = {
      monitorIndexId: valueTypeFormModel.monitorIndexId,
      name: valueTypeFormModel.name.trim(),
      unit: valueTypeFormModel.unit?.trim() || undefined,
    }
    if (isValueTypeEdit.value && editingValueTypeId !== null) {
      await updateMonitorValueType(editingValueTypeId, payload)
      message.success('修改成功')
    } else {
      await createMonitorValueType(payload)
      message.success('新增成功')
    }
    showValueTypeModal.value = false
    fetchRightData()
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '操作失败')
  } finally {
    submittingValueType.value = false
  }
}

async function handleDeleteValueType(id: number) {
  try {
    await deleteMonitorValueType(id)
    message.success('删除成功')
    fetchRightData()
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '删除失败')
  }
}

// 右侧表格列
const rightColumns: DataTableColumns<MonitorValueType> = [
  { title: '监测内容名称', key: 'name', minWidth: 160, ellipsis: { tooltip: true } },
  { title: '单位', key: 'unit', width: 100, render: (row) => row.unit || '—' },
  {
    title: '操作',
    key: 'actions',
    width: 140,
    render(row) {
      return h(NSpace, { size: 4 }, () => [
        h(
          NButton,
          { text: true, type: 'primary', size: 'small', onClick: () => openEditValueType(row) },
          {
            default: () => '编辑',
            icon: () => h(NIcon, { component: CreateOutline, size: 16 }),
          },
        ),
        h(
          NPopconfirm,
          { onPositiveClick: () => handleDeleteValueType(row.id!) },
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
            default: () => '确认删除该监测内容？',
          },
        ),
      ])
    },
  },
]

// ===================== 初始化 =====================
onMounted(async () => {
  await loadDepartments()
  fetchLeftData()
})
</script>

<template>
  <div class="flex flex-col gap-3 h-full">
    <!-- 搜索栏 -->
    <div class="flex items-center">
      <n-space>
        <n-input
          v-model:value="searchName"
          placeholder="搜索监测项名称"
          clearable
          style="width: 200px"
          @keydown.enter="handleLeftSearch"
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
          v-model:value="searchType"
          :options="TYPE_OPTIONS"
          placeholder="类型"
          clearable
          style="width: 140px"
        />
        <n-button type="primary" @click="handleLeftSearch">查询</n-button>
      </n-space>
    </div>

    <!-- 左右双面板 -->
    <div class="flex-1 flex gap-4 min-h-0">
      <!-- 左侧：监测项 -->
      <div class="w-1/2 flex flex-col gap-3 min-h-0">
        <!-- 卡片标题栏 -->
        <div class="flex items-center justify-between">
          <span class="font-medium text-base">监测项</span>
          <n-button type="primary" @click="openCreateIndex">
            <template #icon>
              <n-icon :component="AddOutline" />
            </template>
            新增
          </n-button>
        </div>
        <n-data-table
          :columns="leftColumns"
          :data="leftData"
          :loading="leftLoading"
          :bordered="true"
          :single-line="false"
        />
        <div class="flex justify-end">
          <n-pagination
            v-model:page="leftPagination.page"
            v-model:page-size="leftPagination.size"
            :item-count="leftTotal"
            show-size-picker
            :page-sizes="[10, 20, 30, 50]"
            @update:page="handleLeftPageChange"
            @update:page-size="handleLeftPageSizeChange"
          />
        </div>
      </div>

      <!-- 右侧：监测内容 -->
      <div class="w-1/2 flex flex-col gap-3 min-h-0">
        <!-- 卡片标题栏 -->
        <div class="flex items-center justify-between">
          <span class="font-medium text-base">监测内容</span>
          <n-button type="primary" @click="openCreateValueType">
            <template #icon>
              <n-icon :component="AddOutline" />
            </template>
            新增
          </n-button>
        </div>
        <n-data-table
          :columns="rightColumns"
          :data="rightData"
          :loading="rightLoading"
          :bordered="true"
          :single-line="false"
        />
        <div class="flex justify-end">
          <n-pagination
            v-model:page="rightPagination.page"
            v-model:page-size="rightPagination.size"
            :item-count="rightTotal"
            show-size-picker
            :page-sizes="[10, 20, 30, 50]"
            @update:page="handleRightPageChange"
            @update:page-size="handleRightPageSizeChange"
          />
        </div>
      </div>
    </div>

    <!-- 监测项弹窗 -->
    <n-modal
      v-model:show="showIndexModal"
      preset="card"
      :title="isIndexEdit ? '编辑监测项' : '新增监测项'"
      style="width: 480px"
      :mask-closable="false"
    >
      <n-form ref="indexFormRef" :model="indexFormModel" :rules="indexRules" label-placement="left" label-width="88">
        <n-form-item label="监测项名称" path="name">
          <n-input v-model:value="indexFormModel.name" placeholder="请输入监测项名称" />
        </n-form-item>
        <n-form-item label="类型" path="type">
          <n-select v-model:value="indexFormModel.type" :options="TYPE_OPTIONS" placeholder="请选择类型" />
        </n-form-item>
        <n-form-item label="所属机构" path="departmentId">
          <n-select
            v-model:value="indexFormModel.departmentId"
            :options="departmentOptions"
            placeholder="请选择机构"
            filterable
          />
        </n-form-item>
        <n-form-item label="编码" path="code">
          <n-input v-model:value="indexFormModel.code" placeholder="可选，用于简称或编号" />
        </n-form-item>
      </n-form>
      <template #action>
        <n-space justify="end">
          <n-button @click="showIndexModal = false">取消</n-button>
          <n-button type="primary" :loading="submittingIndex" @click="handleSubmitIndex">确定</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 监测内容弹窗 -->
    <n-modal
      v-model:show="showValueTypeModal"
      preset="card"
      :title="isValueTypeEdit ? '编辑监测内容' : '新增监测内容'"
      style="width: 440px"
      :mask-closable="false"
    >
      <n-form
        ref="valueTypeFormRef"
        :model="valueTypeFormModel"
        :rules="valueTypeRules"
        label-placement="left"
        label-width="80"
      >
        <n-form-item label="监测内容" path="name">
          <n-input v-model:value="valueTypeFormModel.name" placeholder="请输入监测内容名称" />
        </n-form-item>
        <n-form-item label="单位" path="unit">
          <n-input v-model:value="valueTypeFormModel.unit" placeholder="如：mm、℃、kN，可选" />
        </n-form-item>
      </n-form>
      <template #action>
        <n-space justify="end">
          <n-button @click="showValueTypeModal = false">取消</n-button>
          <n-button type="primary" :loading="submittingValueType" @click="handleSubmitValueType">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<style scoped>
:deep(.n-data-table .n-data-table-td) {
  padding: 8px 12px;
}
</style>
