import { apiClient } from '../core/network/apiClient';
import type { BannerCreateRequest, BannerResponse, BannerUpdateRequest } from '../models/bannerModels';

/** Admin-only for now — there is no public banner display screen yet
 * (see Report/00_todo_backlog.md), only the admin management CRUD covered here. */
export const bannerService = {
  async adminList(): Promise<BannerResponse[]> {
    const { data } = await apiClient.get<BannerResponse[]>('/api/v1/admin/banners');
    return data;
  },
  async adminCreate(body: BannerCreateRequest): Promise<BannerResponse> {
    const { data } = await apiClient.post<BannerResponse>('/api/v1/admin/banners', body);
    return data;
  },
  async adminUpdate(id: string, body: BannerUpdateRequest): Promise<BannerResponse> {
    const { data } = await apiClient.patch<BannerResponse>(`/api/v1/admin/banners/${id}`, body);
    return data;
  },
  async adminDelete(id: string): Promise<void> {
    await apiClient.delete(`/api/v1/admin/banners/${id}`);
  },
};
