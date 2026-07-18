import type { AccountStatus, AuthProvider } from './enums';

export interface AdminDashboardStats {
  total_user_count: number;
  new_user_count_today: number;
  active_user_count: number;
  post_count: number;
  comment_count: number;
  dive_log_count: number;
}

export interface AdminUserListItem {
  id: string;
  email: string;
  provider: AuthProvider;
  account_status: AccountStatus;
  nickname: string | null;
  created_at: string;
}

export interface AdminUserDetailResponse {
  id: string;
  email: string;
  provider: AuthProvider;
  account_status: AccountStatus;
  nickname: string | null;
  last_login_at: string | null;
  created_at: string;
}
