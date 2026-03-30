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

export type { ApiResponse, PageResult }
