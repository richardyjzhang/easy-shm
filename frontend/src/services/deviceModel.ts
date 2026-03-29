import type { ApiResponse, PageResult, DeviceModel } from '@/types/deviceModel'
import request from '@/utils/request'

export function listDeviceModels(params: {
  manufacturer?: string
  deviceType?: string
  model?: string
  departmentId?: number
  page?: number
  size?: number
}) {
  return request.get<unknown, ApiResponse<PageResult<DeviceModel>>>('/device-models', { params })
}

export function getDeviceModel(id: number) {
  return request.get<unknown, ApiResponse<DeviceModel>>(`/device-models/${id}`)
}

export function createDeviceModel(data: DeviceModel) {
  return request.post<unknown, ApiResponse<DeviceModel>>('/device-models', data)
}

export function updateDeviceModel(id: number, data: DeviceModel) {
  return request.put<unknown, ApiResponse<DeviceModel>>(`/device-models/${id}`, data)
}

export function deleteDeviceModel(id: number) {
  return request.delete<unknown, ApiResponse<void>>(`/device-models/${id}`)
}
