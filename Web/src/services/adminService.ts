import { apiClient } from '../core/network/apiClient';
import type { AdminDashboardStats, AdminUserDetailResponse, AdminUserListItem } from '../models/adminModels';

export const adminService = {
  async dashboard(): Promise<AdminDashboardStats> {
    const { data } = await apiClient.get<AdminDashboardStats>('/api/v1/admin/dashboard');
    return data;
  },
  async listUsers(query?: string): Promise<AdminUserListItem[]> {
    const { data } = await apiClient.get<AdminUserListItem[]>('/api/v1/admin/users', {
      params: query ? { q: query } : undefined,
    });
    return data;
  },
  async getUser(id: string): Promise<AdminUserDetailResponse> {
    const { data } = await apiClient.get<AdminUserDetailResponse>(`/api/v1/admin/users/${id}`);
    return data;
  },
  async suspendUser(id: string): Promise<AdminUserDetailResponse> {
    const { data } = await apiClient.patch<AdminUserDetailResponse>(`/api/v1/admin/users/${id}/suspend`);
    return data;
  },
  async unsuspendUser(id: string): Promise<AdminUserDetailResponse> {
    const { data } = await apiClient.patch<AdminUserDetailResponse>(`/api/v1/admin/users/${id}/unsuspend`);
    return data;
  },
};
