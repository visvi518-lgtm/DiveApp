import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { adminService } from '../../services/adminService';
import type { AdminUserListItem } from '../../models/adminModels';
import { accountStatusLabel, authProviderLabel } from './adminFormat';
import { EmptyState, ErrorState, LoadingView } from '../../components/StateViews';
import { SubPageHeader } from '../../components/SubPageHeader';
import '../../components/ListCard.css';

type UiState =
  | { status: 'loading' }
  | { status: 'success'; users: AdminUserListItem[] }
  | { status: 'error'; message: string };

export function AdminUserListPage() {
  const navigate = useNavigate();
  const [query, setQuery] = useState('');
  const [state, setState] = useState<UiState>({ status: 'loading' });

  const load = useCallback((q?: string) => {
    setState({ status: 'loading' });
    adminService
      .listUsers(q)
      .then((users) => setState({ status: 'success', users }))
      .catch((error: unknown) =>
        setState({ status: 'error', message: error instanceof Error ? error.message : '회원 목록을 불러오지 못했습니다.' }),
      );
  }, []);

  useEffect(() => load(), [load]);

  return (
    <div>
      <SubPageHeader title="회원 관리" onBack={() => navigate('/admin')} backLabel="대시보드" />

      <div style={{ display: 'flex', gap: 'var(--space-sm)', marginBottom: 'var(--space-lg)' }}>
        <input
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          onKeyDown={(event) => event.key === 'Enter' && load(query)}
          placeholder="이메일 또는 닉네임 검색"
          style={{
            flex: 1,
            padding: 'var(--space-md)',
            border: '1px solid var(--color-border)',
            borderRadius: 'var(--radius-md)',
            background: 'var(--color-background)',
            color: 'var(--color-text)',
          }}
        />
        <button className="btn btn-secondary" style={{ width: 'auto' }} onClick={() => load(query)}>
          검색
        </button>
      </div>

      {state.status === 'loading' && <LoadingView />}
      {state.status === 'error' && <ErrorState message={state.message} onRetry={() => load(query)} />}
      {state.status === 'success' && state.users.length === 0 && <EmptyState title="회원이 없습니다" />}
      {state.status === 'success' &&
        state.users.map((user) => (
          <Link to={`/admin/users/${user.id}`} key={user.id} className="list-card">
            <div className="list-card__row">
              <div>
                <div className="list-card__title">{user.nickname ?? '닉네임 없음'}</div>
                <div className="list-card__subtitle">{user.email}</div>
              </div>
              <div style={{ textAlign: 'right', fontSize: '0.8125rem', color: 'var(--color-text-secondary)' }}>
                <div>{authProviderLabel(user.provider)}</div>
                <div>{accountStatusLabel(user.account_status)}</div>
              </div>
            </div>
          </Link>
        ))}
    </div>
  );
}
