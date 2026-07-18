import type { BannerType } from './enums';

export interface BannerCreateRequest {
  title: string;
  image_url: string;
  banner_type: BannerType;
  target_url?: string | null;
  display_order?: number;
  is_active?: boolean;
  start_at?: string | null;
  end_at?: string | null;
}

export type BannerUpdateRequest = Partial<BannerCreateRequest>;

export interface BannerResponse {
  id: string;
  title: string;
  image_url: string;
  banner_type: BannerType;
  target_url: string | null;
  display_order: number;
  is_active: boolean;
  start_at: string | null;
  end_at: string | null;
  created_at: string;
  updated_at: string;
}
