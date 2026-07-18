import type { AccountStatus, AuthProvider, UserRole } from './enums';

export interface ProfileSetupRequest {
  nickname: string;
  profile_image_url?: string | null;
  phone_number?: string | null;
}

export interface UserProfileResponse {
  nickname: string;
  profile_image_url: string | null;
  phone_number: string | null;
  bio: string | null;
}

export interface CurrentUser {
  id: string;
  email: string;
  provider: AuthProvider;
  role: UserRole;
  account_status: AccountStatus;
  profile: UserProfileResponse | null;
}
