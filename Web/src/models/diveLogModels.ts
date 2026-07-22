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
  // Exact coordinate selected for this individual log — kept separate from
  // `location`'s coordinate since the reusable DiveLocation row can be shared
  // (and later re-pointed) across dive logs with a similar name/city/country.
  latitude?: number | null;
  longitude?: number | null;
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
  latitude: number | null;
  longitude: number | null;
  memo: string | null;
  freediving: FreedivingDetailInput | null;
  scuba: ScubaDetailInput | null;
  photos: DivePhotoResponse[];
  created_at: string;
}

/** A point chosen in the map picker (Docs/13), before it's attached to a
 * create/update request. UI-only — not sent to the API as-is. */
export interface PickedDiveLocation {
  siteName: string;
  latitude: number;
  longitude: number;
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
