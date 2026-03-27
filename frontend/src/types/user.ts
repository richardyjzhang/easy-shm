import type { ApiResponse, PageResult } from '@/types/department'

export type { ApiResponse, PageResult }

export interface User {
  id?: number
  loginName: string
  departmentId: number
  role: number
  phone?: string
  address?: string
  remark?: string
  createdAt?: string
  updatedAt?: string
}
