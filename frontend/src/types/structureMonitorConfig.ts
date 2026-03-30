import type { ApiResponse, PageResult } from './department'
import type { MonitorIndex, MonitorValueType } from './monitorIndex'

export interface StructureMonitorConfig {
  id?: number
  structureId: number
  valueTypeId: number
  createdAt?: string
}

export interface MonitorIndexWithValueTypes extends MonitorIndex {
  valueTypes: MonitorValueType[]
}

export interface MonitorValueTypeWithIndex extends MonitorValueType {
  monitorIndexName?: string
  monitorIndexType?: number
  monitorIndexCode?: string
  monitorIndexDepartmentId?: number
}

export type { ApiResponse, PageResult }
