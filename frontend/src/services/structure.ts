import type { ApiResponse, PageResult, Structure } from '@/types/structure'
import request from '@/utils/request'

export function listStructures(params: {
  name?: string
  departmentId?: number
  page?: number
  size?: number
}) {
  return request.get<unknown, ApiResponse<PageResult<Structure>>>('/structures', { params })
}

export function getStructure(id: number) {
  return request.get<unknown, ApiResponse<Structure>>(`/structures/${id}`)
}

export function createStructure(data: Structure) {
  return request.post<unknown, ApiResponse<Structure>>('/structures', data)
}

export function updateStructure(id: number, data: Structure) {
  return request.put<unknown, ApiResponse<Structure>>(`/structures/${id}`, data)
}

export function deleteStructure(id: number) {
  return request.delete<unknown, ApiResponse<void>>(`/structures/${id}`)
}
