export interface Department {
  id?: number
  name: string
  contact?: string
  phone?: string
  address?: string
  description?: string
  createdAt?: string
  updatedAt?: string
}

export interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}
