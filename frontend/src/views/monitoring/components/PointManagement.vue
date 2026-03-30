<script setup lang="ts">
import { AddOutline, CreateOutline, LocationOutline, TrashOutline } from '@vicons/ionicons5'
import {
  NButton,
  NCard,
  NDataTable,
  NEmpty,
  NForm,
  NFormItem,
  NIcon,
  NInput,
  NModal,
  NPopconfirm,
  NSelect,
  NSpace,
  NSpin,
  useMessage,
} from 'naive-ui'
import type { DataTableColumns, SelectOption } from 'naive-ui'
import { computed, h, onMounted, ref, watch } from 'vue'

import { listDepartments } from '@/services/department'
import { listStructures } from '@/services/structure'
import { getStructureMonitorConfigs, listMonitorIndexesWithValueTypes } from '@/services/structureMonitorConfig'
import { listMonitorDevices } from '@/services/monitorDevice'
import {
  listMonitorPoints,
  createMonitorPoint,
  updateMonitorPoint,
  deleteMonitorPoint,
} from '@/services/monitorPoint'
import type { MonitorPoint } from '@/types/monitorPoint'

const message = useMessage()

// ===================== 顶部：结构物选择 =====================
const selectedStructureId = ref<number | null>(null)
const structureOptions = ref<SelectOption[]>([])
const departmentOptions = ref<SelectOption[]>([])
const structureLoading = ref(false)
const searchStructureDeptId = ref<number | null>(null)

async function loadDepartments() {
  try {
    const res = await listDepartments({ page: 1, size: 500 })
    departmentOptions.value = res.data.content.map((d) => ({
      label: d.name,
      value: d.id!,
    }))
    if (departmentOptions.value.length > 0 && searchStructureDeptId.value == null) {
      searchStructureDeptId.value = Number(departmentOptions.value[0].value)
    }
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '加载机构列表失败')
  }
}

async function loadStructures() {
  structureLoading.value = true
  try {
    const params: { name?: string; departmentId?: number; page: number; size: number } = {
      page: 1,
      size: 500,
    }
    if (searchStructureDeptId.value) {
      params.departmentId = searchStructureDeptId.value
    }
    const res = await listStructures(params)
    structureOptions.value = res.data.content.map((s) => ({
      label: s.name,
      value: s.id!,
    }))
    selectedStructureId.value =
      structureOptions.value.length > 0 ? Number(structureOptions.value[0].value) : null
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '加载结构物列表失败')
  } finally {
    structureLoading.value = false
  }
}

// ===================== 中间：数据表 =====================
const points = ref<MonitorPoint[]>([])
const dataLoading = ref(false)

const valueTypeOptions = ref<SelectOption[]>([])
const valueTypeMap = ref<Map<number, string>>(new Map())

const deviceOptions = ref<SelectOption[]>([])
const deviceMap = ref<Map<number, string>>(new Map())

async function loadConfiguredValueTypes() {
  if (!selectedStructureId.value) {
    valueTypeOptions.value = []
    valueTypeMap.value = new Map()
    return
  }
  try {
    const [configRes, indexRes] = await Promise.all([
      getStructureMonitorConfigs(selectedStructureId.value),
      listMonitorIndexesWithValueTypes(),
    ])
    const configuredIds = new Set(configRes.data.map((c) => c.valueTypeId))
    const options: SelectOption[] = []
    const map = new Map<number, string>()
    for (const idx of indexRes.data) {
      for (const vt of idx.valueTypes) {
        if (configuredIds.has(vt.id!)) {
          const label = `${idx.name} / ${vt.name}${vt.unit ? ` (${vt.unit})` : ''}`
          options.push({ label, value: vt.id! })
          map.set(vt.id!, label)
        }
      }
    }
    valueTypeOptions.value = options
    valueTypeMap.value = map
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '加载监测内容失败')
  }
}

async function loadDevices() {
  try {
    const params: { departmentId?: number; page: number; size: number } = {
      page: 1,
      size: 9999,
    }
    if (searchStructureDeptId.value) {
      params.departmentId = searchStructureDeptId.value
    }
    const res = await listMonitorDevices(params)
    deviceOptions.value = res.data.content.map((d) => ({
      label: d.sn,
      value: d.id!,
    }))
    deviceMap.value = new Map(res.data.content.map((d) => [d.id!, d.sn]))
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '加载设备列表失败')
  }
}

async function loadPoints() {
  if (!selectedStructureId.value) {
    points.value = []
    return
  }
  dataLoading.value = true
  try {
    const res = await listMonitorPoints(selectedStructureId.value)
    points.value = res.data
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '加载测点列表失败')
  } finally {
    dataLoading.value = false
  }
}

const tableColumns = computed<DataTableColumns<MonitorPoint>>(() => [
  { title: '测点编号', key: 'code', width: 260, ellipsis: { tooltip: true } },
  {
    title: '监测内容',
    key: 'valueTypeId',
    minWidth: 200,
    ellipsis: { tooltip: true },
    render: (row) => valueTypeMap.value.get(row.valueTypeId) ?? `ID:${row.valueTypeId}`,
  },
  {
    title: '选用设备',
    key: 'deviceId',
    width: 160,
    ellipsis: { tooltip: true },
    render: (row) => deviceMap.value.get(row.deviceId) ?? `ID:${row.deviceId}`,
  },
  { title: '通道编号', key: 'channel', width: 120, ellipsis: { tooltip: true } },
  { title: '位置描述', key: 'location', minWidth: 160, ellipsis: { tooltip: true } },
  {
    title: '操作',
    key: 'actions',
    width: 140,
    fixed: 'right',
    render: (row) =>
      h(NSpace, { size: 4 }, () => [
        h(
          NButton,
          { text: true, type: 'primary', size: 'small', onClick: () => openEditDialog(row) },
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
            default: () => '确定删除该测点？',
          },
        ),
      ]),
  },
])

// ===================== 新增/编辑弹窗 =====================
const dialogVisible = ref(false)
const dialogTitle = ref('新增测点')
const editingId = ref<number | null>(null)
const formSaving = ref(false)

const formModel = ref<{
  valueTypeId: number | null
  code: string
  location: string
  deviceId: number | null
  channel: string
}>({
  valueTypeId: null,
  code: '',
  location: '',
  deviceId: null,
  channel: '',
})

function openCreateDialog() {
  if (!selectedStructureId.value) {
    message.warning('请先选择结构物')
    return
  }
  editingId.value = null
  dialogTitle.value = '新增测点'
  formModel.value = {
    valueTypeId: null,
    code: '',
    location: '',
    deviceId: null,
    channel: '',
  }
  dialogVisible.value = true
}

function openEditDialog(row: MonitorPoint) {
  editingId.value = row.id!
  dialogTitle.value = '编辑测点'
  formModel.value = {
    valueTypeId: row.valueTypeId,
    code: row.code,
    location: row.location ?? '',
    deviceId: row.deviceId,
    channel: row.channel ?? '',
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formModel.value.code.trim()) {
    message.warning('测点编号不能为空')
    return
  }
  if (!formModel.value.valueTypeId) {
    message.warning('请选择监测内容')
    return
  }
  if (!formModel.value.deviceId) {
    message.warning('请选择设备')
    return
  }
  formSaving.value = true
  try {
    const data: MonitorPoint = {
      structureId: selectedStructureId.value!,
      valueTypeId: formModel.value.valueTypeId,
      code: formModel.value.code.trim(),
      location: formModel.value.location.trim() || undefined,
      deviceId: formModel.value.deviceId,
      channel: formModel.value.channel.trim() || undefined,
    }
    if (editingId.value) {
      await updateMonitorPoint(editingId.value, data)
      message.success('更新成功')
    } else {
      await createMonitorPoint(data)
      message.success('创建成功')
    }
    dialogVisible.value = false
    await loadPoints()
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '保存失败')
  } finally {
    formSaving.value = false
  }
}

async function handleDelete(id: number) {
  try {
    await deleteMonitorPoint(id)
    message.success('删除成功')
    await loadPoints()
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '删除失败')
  }
}

// ===================== 初始化 =====================
onMounted(async () => {
  await loadDepartments()
  await loadStructures()
  await loadDevices()
})

watch(selectedStructureId, async () => {
  await loadConfiguredValueTypes()
  await loadPoints()
})

watch(searchStructureDeptId, () => {
  loadDevices()
})
</script>

<template>
  <n-card class="h-full" content-style="height: 100%; display: flex; flex-direction: column; padding: 16px;">
    <div class="flex items-center justify-between gap-3">
      <div class="flex items-center gap-2 text-base font-medium">
        <n-icon :component="LocationOutline" size="18" />
        测点配置
      </div>
      <n-button
        type="primary"
        :disabled="!selectedStructureId"
        @click="openCreateDialog"
      >
        <template #icon>
          <n-icon :component="AddOutline" />
        </template>
        新增测点
      </n-button>
    </div>

    <div class="mt-4 flex items-center gap-3 flex-nowrap overflow-x-auto pb-1">
      <span class="font-medium whitespace-nowrap">结构物</span>
      <n-select
        v-model:value="searchStructureDeptId"
        :options="departmentOptions"
        placeholder="所属机构"
        filterable
        style="width: 180px; flex: 0 0 auto;"
        @update:value="loadStructures"
      />
      <n-select
        v-model:value="selectedStructureId"
        :options="structureOptions"
        placeholder="请选择结构物"
        filterable
        :loading="structureLoading"
        style="width: 180px; flex: 0 0 auto;"
      />
    </div>

    <div class="mt-4 flex-1 min-h-0">
      <n-spin :show="dataLoading">
        <n-empty v-if="points.length === 0 && !dataLoading" size="small" description="暂无测点数据" />
        <n-data-table
          v-else
          :row-key="(row: MonitorPoint) => row.id!"
          :columns="tableColumns"
          :data="points"
          :bordered="true"
          :single-line="false"
          size="small"
          max-height="100%"
          :scroll-x="900"
        />
      </n-spin>
    </div>

    <!-- 新增/编辑弹窗 -->
    <n-modal
      v-model:show="dialogVisible"
      preset="dialog"
      :title="dialogTitle"
      :positive-text="editingId ? '保存' : '创建'"
      negative-text="取消"
      :loading="formSaving"
      style="width: 520px;"
      @positive-click="handleSubmit"
      @negative-click="dialogVisible = false"
    >
      <n-form label-placement="left" label-width="80" class="mt-4">
        <n-form-item label="监测内容" required>
          <n-select
            v-model:value="formModel.valueTypeId"
            :options="valueTypeOptions"
            placeholder="请选择监测内容"
            filterable
          />
        </n-form-item>
        <n-form-item label="测点编号" required>
          <n-input v-model:value="formModel.code" placeholder="请输入测点编号" />
        </n-form-item>
        <n-form-item label="位置描述">
          <n-input v-model:value="formModel.location" placeholder="请输入测点位置描述（选填）" />
        </n-form-item>
        <n-form-item label="选用设备" required>
          <n-select
            v-model:value="formModel.deviceId"
            :options="deviceOptions"
            placeholder="请选择设备"
            filterable
          />
        </n-form-item>
        <n-form-item label="通道编号">
          <n-input v-model:value="formModel.channel" placeholder="请输入通道编号（选填）" />
        </n-form-item>
      </n-form>
    </n-modal>
  </n-card>
</template>
