import type { ApiResponse, PageResult, User } from '@/types/user'
import request from '@/utils/request'

export function listUsers(params: { loginName?: string; page?: number; size?: number }) {
  return request.get<unknown, ApiResponse<PageResult<User>>>('/users', { params })
}

export function getUser(id: number) {
  return request.get<unknown, ApiResponse<User>>(`/users/${id}`)
}

export function createUser(data: User & { password?: string }) {
  return request.post<unknown, ApiResponse<User>>('/users', data)
}

export function updateUser(id: number, data: User) {
  return request.put<unknown, ApiResponse<User>>(`/users/${id}`, data)
}

export function deleteUser(id: number) {
  return request.delete<unknown, ApiResponse<void>>(`/users/${id}`)
}

export function resetUserPassword(id: number, password: string) {
  return request.put<unknown, ApiResponse<void>>(`/users/${id}/password`, { password })
}
