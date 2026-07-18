import type { DiveType } from './enums';

export interface DiveLocationInput {
  name: string;
  address?: string | null;
  latitude: number;
  longitude: number;
  naver_place_id?: string | null;
  country?: string | null;
  city?: string | null;
}

export interface DiveLocationResponse {
  id: string;
  name: string;
  address: string | null;
  latitude: number;
  longitude: number;
  country: string | null;
  city: string | null;
}

export interface FreedivingDetailInput {
  max_depth: number;
  dive_time_seconds: number;
}

export interface ScubaDetailInput {
  max_depth: number;
  dive_time_seconds: number;
  tank_pressure_start: number;
  tank_pressure_end: number;
}

export interface DivePhotoResponse {
  id: string;
  image_url: string;
  display_order: number;
}

export interface DiveLogCreateRequest {
  dive_type: DiveType;
  dive_date: string;
  location: DiveLocationInput;
  memo?: string | null;
  freediving?: FreedivingDetailInput | null;
  scuba?: ScubaDetailInput | null;
}

export interface DiveLogUpdateRequest {
  memo?: string | null;
  freediving?: FreedivingDetailInput | null;
  scuba?: ScubaDetailInput | null;
}

export interface DiveLogResponse {
  id: string;
  dive_type: DiveType;
  dive_date: string;
  location: DiveLocationResponse;
  memo: string | null;
  freediving: FreedivingDetailInput | null;
  scuba: ScubaDetailInput | null;
  photos: DivePhotoResponse[];
  created_at: string;
}

export interface DiveLogListItem {
  id: string;
  dive_type: DiveType;
  dive_date: string;
  location_name: string;
  cover_image_url: string | null;
  max_depth: number;
}

export interface DiveLogStatisticsResponse {
  total_dive_count: number;
  max_depth_overall: number;
  total_dive_time_seconds: number;
  freediving_count: number;
  scuba_count: number;
}
