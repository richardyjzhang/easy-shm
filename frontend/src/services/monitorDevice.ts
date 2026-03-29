import type { ApiResponse, PageResult, MonitorDevice } from '@/types/monitorDevice'
import request from '@/utils/request'

export function listMonitorDevices(params: {
  sn?: string
  departmentId?: number
  deviceModelId?: number
  page?: number
  size?: number
}) {
  return request.get<unknown, ApiResponse<PageResult<MonitorDevice>>>('/monitor-devices', { params })
}

export function getMonitorDevice(id: number) {
  return request.get<unknown, ApiResponse<MonitorDevice>>(`/monitor-devices/${id}`)
}

export function createMonitorDevice(data: MonitorDevice) {
  return request.post<unknown, ApiResponse<MonitorDevice>>('/monitor-devices', data)
}

export function updateMonitorDevice(id: number, data: MonitorDevice) {
  return request.put<unknown, ApiResponse<MonitorDevice>>(`/monitor-devices/${id}`, data)
}

export function deleteMonitorDevice(id: number) {
  return request.delete<unknown, ApiResponse<void>>(`/monitor-devices/${id}`)
}
