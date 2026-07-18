import type { AccountStatus, AuthProvider } from '../../models/enums';

export function accountStatusLabel(status: AccountStatus): string {
  switch (status) {
    case 'ACTIVE':
      return '활성';
    case 'DORMANT':
      return '휴면';
    case 'SUSPENDED':
      return '정지됨';
    case 'DELETED':
      return '삭제됨';
  }
}

export function authProviderLabel(provider: AuthProvider): string {
  switch (provider) {
    case 'NAVER':
      return '네이버';
    case 'GOOGLE':
      return '구글';
    case 'EMAIL':
      return '이메일';
  }
}
