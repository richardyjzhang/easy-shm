<script setup lang="ts">
import { AddOutline, CreateOutline, LocationOutline, SearchOutline, TrashOutline } from '@vicons/ionicons5'
import {
  NButton,
  NDataTable,
  NForm,
  NFormItem,
  NIcon,
  NInput,
  NInputNumber,
  NModal,
  NPagination,
  NPopconfirm,
  NSelect,
  NSpace,
  useMessage,
} from 'naive-ui'
import type { DataTableColumns, FormInst, FormRules, SelectOption } from 'naive-ui'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { h, nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'

import { listDepartments } from '@/services/department'
import {
  createStructure,
  deleteStructure,
  listStructures,
  updateStructure,
} from '@/services/structure'
import type { Structure } from '@/types/structure'

const STRUCTURE_TYPES = ['民建', '工建', '桥梁', '隧道', '边坡', '其他'] as const
const typeOptions: SelectOption[] = STRUCTURE_TYPES.map((t) => ({ label: t, value: t }))

const message = useMessage()

const loading = ref(false)
const data = ref<Structure[]>([])
const total = ref(0)
const searchName = ref('')
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
    const res = await listStructures({
      name: searchName.value || undefined,
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
const formModel = reactive<Structure>({
  name: '',
  type: '民建',
  departmentId: 0,
  longitude: null,
  latitude: null,
  remark: '',
})
let editingId: number | null = null

const rules: FormRules = {
  name: [{ required: true, message: '请输入结构名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择结构类型', trigger: 'change' }],
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
    name: '',
    type: '民建',
    departmentId: departmentOptions.value[0]?.value != null ? Number(departmentOptions.value[0].value) : 0,
    longitude: null,
    latitude: null,
    remark: '',
  })
  showModal.value = true
}

function openEdit(row: Structure) {
  isEdit.value = true
  editingId = row.id!
  Object.assign(formModel, {
    name: row.name,
    type: row.type,
    departmentId: row.departmentId,
    longitude: row.longitude ?? null,
    latitude: row.latitude ?? null,
    remark: row.remark ?? '',
  })
  showModal.value = true
}

const submitting = ref(false)

function coordsConsistent(): boolean {
  const lng = formModel.longitude
  const lat = formModel.latitude
  const hasLng = lng != null && !Number.isNaN(lng)
  const hasLat = lat != null && !Number.isNaN(lat)
  if (hasLng !== hasLat) {
    message.error('经度与纬度需同时填写或同时留空')
    return false
  }
  return true
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  if (!coordsConsistent()) return
  submitting.value = true
  try {
    const payload: Structure = {
      name: formModel.name.trim(),
      type: formModel.type,
      departmentId: formModel.departmentId,
      longitude: formModel.longitude,
      latitude: formModel.latitude,
      remark: formModel.remark || undefined,
    }
    if (isEdit.value && editingId !== null) {
      await updateStructure(editingId, payload)
      message.success('修改成功')
    } else {
      await createStructure(payload)
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
    await deleteStructure(id)
    message.success('删除成功')
    fetchData()
  } catch (e: unknown) {
    message.error(e instanceof Error ? e.message : '删除失败')
  }
}

function formatCoord(v: number | null | undefined) {
  if (v == null) return '—'
  return String(v)
}

const columns: DataTableColumns<Structure> = [
  { title: '结构名称', key: 'name', minWidth: 140 },
  { title: '类型', key: 'type', width: 100 },
  {
    title: '所属机构',
    key: 'departmentId',
    minWidth: 140,
    render: (row) => departmentNameMap.value[row.departmentId] ?? row.departmentId,
  },
  {
    title: '经度',
    key: 'longitude',
    width: 120,
    render: (row) => formatCoord(row.longitude),
  },
  {
    title: '纬度',
    key: 'latitude',
    width: 120,
    render: (row) => formatCoord(row.latitude),
  },
  { title: '备注', key: 'remark', minWidth: 140, ellipsis: { tooltip: true } },
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
            default: () => '确认删除该结构？',
          },
        ),
      ])
    },
  },
]

// ---- 地图选点 ----
const showMapModal = ref(false)
const mapContainerRef = ref<HTMLElement | null>(null)
let map: L.Map | null = null
let mapMarker: L.Marker | null = null

const tempLat = ref<number | null>(null)
const tempLng = ref<number | null>(null)

/** 与后端/表单一致，经纬度保留 7 位小数 */
function roundTo7(n: number): number {
  return Math.round(n * 1e7) / 1e7
}

function pickIcon() {
  return L.divIcon({
    className: 'structure-map-marker',
    html: '<div style="width:14px;height:14px;background:#18a058;border-radius:50%;border:2px solid #fff;box-shadow:0 1px 4px rgba(0,0,0,.35)"></div>',
    iconSize: [18, 18],
    iconAnchor: [9, 9],
  })
}

function setMapMarker(lat: number, lng: number) {
  if (!map) return
  if (mapMarker) {
    map.removeLayer(mapMarker)
    mapMarker = null
  }
  mapMarker = L.marker([lat, lng], { icon: pickIcon(), draggable: true }).addTo(map)
  mapMarker.on('dragend', () => {
    const p = mapMarker!.getLatLng()
    tempLat.value = roundTo7(p.lat)
    tempLng.value = roundTo7(p.lng)
  })
}

function initOrRefreshMap() {
  const el = mapContainerRef.value
  if (!el) return

  const lat0 = tempLat.value ?? 39.90923
  const lng0 = tempLng.value ?? 116.397428
  const zoom = tempLat.value != null && tempLng.value != null ? 15 : 5

  if (map) {
    map.remove()
    map = null
    mapMarker = null
  }

  map = L.map(el, { zoomControl: true }).setView([lat0, lng0], zoom)
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; OpenStreetMap',
  }).addTo(map)

  if (tempLat.value != null && tempLng.value != null) {
    setMapMarker(tempLat.value, tempLng.value)
  }

  map.on('click', (e: L.LeafletMouseEvent) => {
    const lat = roundTo7(e.latlng.lat)
    const lng = roundTo7(e.latlng.lng)
    tempLat.value = lat
    tempLng.value = lng
    setMapMarker(lat, lng)
  })

  setTimeout(() => map?.invalidateSize(), 200)
}

function destroyMap() {
  if (map) {
    map.remove()
    map = null
    mapMarker = null
  }
}

function openMapPicker() {
  const la = formModel.latitude
  const ln = formModel.longitude
  tempLat.value = la != null ? roundTo7(la) : null
  tempLng.value = ln != null ? roundTo7(ln) : null
  showMapModal.value = true
}

function applyMapCoords() {
  const lat = tempLat.value
  const lng = tempLng.value
  if (lat == null || lng == null) {
    message.warning('请先在地图上点击选点，或拖动标记')
    return
  }
  formModel.latitude = roundTo7(lat)
  formModel.longitude = roundTo7(lng)
  showMapModal.value = false
}

watch(showMapModal, (show) => {
  if (!show) destroyMap()
})

function onMapModalAfterEnter() {
  nextTick(() => initOrRefreshMap())
}

onMounted(async () => {
  await loadDepartments()
  fetchData()
})

onUnmounted(() => {
  destroyMap()
})
</script>

<template>
  <div class="flex flex-col gap-3">
    <div class="flex items-center justify-between">
      <n-space>
        <n-input
          v-model:value="searchName"
          placeholder="搜索结构名称"
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
          style="width: 200px"
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
      :title="isEdit ? '编辑结构' : '新增结构'"
      style="width: 560px"
      :mask-closable="false"
    >
      <n-form ref="formRef" :model="formModel" :rules="rules" label-placement="left" label-width="88">
        <n-form-item label="结构名称" path="name">
          <n-input v-model:value="formModel.name" placeholder="请输入结构名称" />
        </n-form-item>
        <n-form-item label="类型" path="type">
          <n-select v-model:value="formModel.type" :options="typeOptions" placeholder="请选择类型" />
        </n-form-item>
        <n-form-item label="所属机构" path="departmentId">
          <n-select
            v-model:value="formModel.departmentId"
            :options="departmentOptions"
            placeholder="请选择机构"
            filterable
          />
        </n-form-item>
        <n-form-item label="经度" path="longitude">
          <n-input-number
            v-model:value="formModel.longitude"
            :show-button="false"
            placeholder="可手动输入，或与纬度同时留空"
            :precision="7"
            :min="-180"
            :max="180"
            style="width: 100%"
            clearable
          />
        </n-form-item>
        <n-form-item label="纬度" path="latitude">
          <n-input-number
            v-model:value="formModel.latitude"
            :show-button="false"
            placeholder="可手动输入，或与经度同时留空"
            :precision="7"
            :min="-90"
            :max="90"
            style="width: 100%"
            clearable
          />
        </n-form-item>
        <n-form-item label="备注" path="remark">
          <n-input v-model:value="formModel.remark" type="textarea" placeholder="选填" :rows="3" />
        </n-form-item>
        <n-form-item :show-label="false">
          <div class="pl-[88px]">
            <n-button secondary size="small" @click="openMapPicker">
              <template #icon>
                <n-icon :component="LocationOutline" />
              </template>
              地图选点
            </n-button>
          </div>
        </n-form-item>
      </n-form>
      <template #action>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">确定</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 地图选点 -->
    <n-modal
      v-model:show="showMapModal"
      preset="card"
      title="地图选点"
      style="width: min(920px, 96vw)"
      :mask-closable="false"
      @after-enter="onMapModalAfterEnter"
    >
      <p class="text-sm text-gray-500 mb-2">
        点击地图放置标记，可拖动标记微调；选好后点「填入表单」。也可关闭弹窗后在表单中直接输入经纬度。
      </p>
      <div
        ref="mapContainerRef"
        class="w-full rounded border border-gray-200"
        style="height: 420px"
      />
      <template #action>
        <n-space justify="space-between" style="width: 100%">
          <span v-if="tempLat != null && tempLng != null" class="text-sm text-gray-600">
            当前：{{ tempLng.toFixed(7) }}, {{ tempLat.toFixed(7) }}
          </span>
          <span v-else class="text-sm text-gray-400">尚未选点</span>
          <n-space>
            <n-button @click="showMapModal = false">取消</n-button>
            <n-button type="primary" @click="applyMapCoords">填入表单</n-button>
          </n-space>
        </n-space>
      </template>
    </n-modal>
  </div>
</template>

<style scoped>
:deep(.structure-map-marker) {
  background: transparent;
  border: none;
}
</style>
