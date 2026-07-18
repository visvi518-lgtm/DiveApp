export interface SocialLoginRequest {
  token: string;
}

export interface EmailRegisterRequest {
  email: string;
  password: string;
}

export interface EmailLoginRequest {
  email: string;
  password: string;
}

export interface TokenResponse {
  access_token: string;
  refresh_token: string;
  token_type: string;
  is_new_user: boolean;
}

export interface RefreshTokenRequest {
  refresh_token: string;
}

export interface AccessTokenResponse {
  access_token: string;
  token_type: string;
}
