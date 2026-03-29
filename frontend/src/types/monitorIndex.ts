import type { ApiResponse, PageResult } from '@/types/department'

export interface MonitorIndex {
  id?: number
  departmentId: number
  type: number        // 1=环境, 2=作用, 3=结构响应, 4=结构变化
  name: string
  code?: string       // 编码
  createdAt?: string
  updatedAt?: string
}

export interface MonitorValueType {
  id?: number
  monitorIndexId: number
  name: string
  unit?: string
  createdAt?: string
  updatedAt?: string
}

export type { ApiResponse, PageResult }
