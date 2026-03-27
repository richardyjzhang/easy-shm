import type { ApiResponse, Department, PageResult } from '@/types/department'
import request from '@/utils/request'

export function listDepartments(params: { name?: string; page?: number; size?: number }) {
  return request.get<unknown, ApiResponse<PageResult<Department>>>('/departments', { params })
}

export function getDepartment(id: number) {
  return request.get<unknown, ApiResponse<Department>>(`/departments/${id}`)
}

export function createDepartment(data: Department) {
  return request.post<unknown, ApiResponse<Department>>('/departments', data)
}

export function updateDepartment(id: number, data: Department) {
  return request.put<unknown, ApiResponse<Department>>(`/departments/${id}`, data)
}

export function deleteDepartment(id: number) {
  return request.delete<unknown, ApiResponse<void>>(`/departments/${id}`)
}
