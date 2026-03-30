import type { ApiResponse, MonitorIndex, MonitorValueType, PageResult } from '@/types/monitorIndex'
import request from '@/utils/request'

// MonitorIndex APIs
export function listMonitorIndexes(params: {
  name?: string
  departmentId?: number
  type?: number
  page?: number
  size?: number
}) {
  return request
    .get<unknown, ApiResponse<MonitorIndex[]>>('/monitor-indexes', {
      params: {
        name: params.name,
        departmentId: params.departmentId,
        type: params.type,
      },
    })
    .then((res) => {
      const page = params.page ?? 1
      const size = params.size ?? 10
      const start = (page - 1) * size
      const end = start + size
      const full = res.data
      return {
        ...res,
        data: {
          content: full.slice(start, end),
          totalElements: full.length,
          totalPages: Math.ceil(full.length / size),
          number: page - 1,
          size,
        },
      } as ApiResponse<PageResult<MonitorIndex>>
    })
}

export function getMonitorIndex(id: number) {
  return request.get<unknown, ApiResponse<MonitorIndex>>(`/monitor-indexes/${id}`)
}

export function createMonitorIndex(data: MonitorIndex) {
  return request.post<unknown, ApiResponse<MonitorIndex>>('/monitor-indexes', data)
}

export function updateMonitorIndex(id: number, data: MonitorIndex) {
  return request.put<unknown, ApiResponse<MonitorIndex>>(`/monitor-indexes/${id}`, data)
}

export function deleteMonitorIndex(id: number) {
  return request.delete<unknown, ApiResponse<void>>(`/monitor-indexes/${id}`)
}

// MonitorValueType APIs
export function listMonitorValueTypes(params: {
  monitorIndexId: number
  page?: number
  size?: number
}) {
  return request
    .get<unknown, ApiResponse<MonitorValueType[]>>('/monitor-value-types', {
      params: { monitorIndexId: params.monitorIndexId },
    })
    .then((res) => {
      const page = params.page ?? 1
      const size = params.size ?? 10
      const start = (page - 1) * size
      const end = start + size
      const full = res.data
      return {
        ...res,
        data: {
          content: full.slice(start, end),
          totalElements: full.length,
          totalPages: Math.ceil(full.length / size),
          number: page - 1,
          size,
        },
      } as ApiResponse<PageResult<MonitorValueType>>
    })
}

export function getMonitorValueType(id: number) {
  return request.get<unknown, ApiResponse<MonitorValueType>>(`/monitor-value-types/${id}`)
}

export function createMonitorValueType(data: MonitorValueType) {
  return request.post<unknown, ApiResponse<MonitorValueType>>('/monitor-value-types', data)
}

export function updateMonitorValueType(id: number, data: MonitorValueType) {
  return request.put<unknown, ApiResponse<MonitorValueType>>(`/monitor-value-types/${id}`, data)
}

export function deleteMonitorValueType(id: number) {
  return request.delete<unknown, ApiResponse<void>>(`/monitor-value-types/${id}`)
}
