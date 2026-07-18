import { apiClient } from '../core/network/apiClient';
import type {
  DiveLogCreateRequest,
  DiveLogListItem,
  DiveLogResponse,
  DiveLogStatisticsResponse,
  DiveLogUpdateRequest,
} from '../models/diveLogModels';

export const diveLogService = {
  async create(body: DiveLogCreateRequest): Promise<DiveLogResponse> {
    const { data } = await apiClient.post<DiveLogResponse>('/api/v1/dive-logs', body);
    return data;
  },
  async list(): Promise<DiveLogListItem[]> {
    const { data } = await apiClient.get<DiveLogListItem[]>('/api/v1/dive-logs');
    return data;
  },
  async statistics(): Promise<DiveLogStatisticsResponse> {
    const { data } = await apiClient.get<DiveLogStatisticsResponse>('/api/v1/dive-logs/statistics');
    return data;
  },
  async get(id: string): Promise<DiveLogResponse> {
    const { data } = await apiClient.get<DiveLogResponse>(`/api/v1/dive-logs/${id}`);
    return data;
  },
  async update(id: string, body: DiveLogUpdateRequest): Promise<DiveLogResponse> {
    const { data } = await apiClient.patch<DiveLogResponse>(`/api/v1/dive-logs/${id}`, body);
    return data;
  },
  async remove(id: string): Promise<void> {
    await apiClient.delete(`/api/v1/dive-logs/${id}`);
  },
};
