import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { adminService } from '../../services/adminService';
import type { AdminUserDetailResponse } from '../../models/adminModels';
import { accountStatusLabel, authProviderLabel } from './adminFormat';
import { Button } from '../../components/Button';
import { SubPageHeader } from '../../components/SubPageHeader';
import { ErrorState, LoadingView } from '../../components/StateViews';

type UiState =
  | { status: 'loading' }
  | { status: 'success'; user: AdminUserDetailResponse }
  | { status: 'error'; message: string };

export function AdminUserDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [state, setState] = useState<UiState>({ status: 'loading' });
  const [isUpdatingStatus, setIsUpdatingStatus] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);

  const load = useCallback(() => {
    if (!id) return;
    setState({ status: 'loading' });
    adminService
      .getUser(id)
      .then((user) => setState({ status: 'success', user }))
      .catch((error: unknown) =>
        setState({ status: 'error', message: error instanceof Error ? error.message : '회원 정보를 불러오지 못했습니다.' }),
      );
  }, [id]);

  useEffect(() => load(), [load]);

  async function handleToggleSuspend(currentlySuspended: boolean) {
    if (!id) return;
    setActionError(null);
    setIsUpdatingStatus(true);
    try {
      const user = currentlySuspended ? await adminService.unsuspendUser(id) : await adminService.suspendUser(id);
      setState({ status: 'success', user });
    } catch (error) {
      setActionError(error instanceof Error ? error.message : '처리에 실패했습니다.');
    } finally {
      setIsUpdatingStatus(false);
    }
  }

  return (
    <div>
      <SubPageHeader title="회원 상세" onBack={() => navigate('/admin/users')} backLabel="회원 목록" />

      {state.status === 'loading' && <LoadingView />}
      {state.status === 'error' && <ErrorState message={state.message} onRetry={load} />}
      {state.status === 'success' && (
        <>
          <h2>{state.user.nickname ?? '닉네임 없음'}</h2>
          <div className="list-page__stats" style={{ marginTop: 'var(--space-md)' }}>
            <div className="list-card__row">
              <span style={{ color: 'var(--color-text-secondary)' }}>이메일</span>
              <span>{state.user.email}</span>
            </div>
            <div className="list-card__row" style={{ marginTop: 'var(--space-sm)' }}>
              <span style={{ color: 'var(--color-text-secondary)' }}>가입 경로</span>
              <span>{authProviderLabel(state.user.provider)}</span>
            </div>
            <div className="list-card__row" style={{ marginTop: 'var(--space-sm)' }}>
              <span style={{ color: 'var(--color-text-secondary)' }}>상태</span>
              <span>{accountStatusLabel(state.user.account_status)}</span>
            </div>
            <div className="list-card__row" style={{ marginTop: 'var(--space-sm)' }}>
              <span style={{ color: 'var(--color-text-secondary)' }}>가입일</span>
              <span>{new Date(state.user.created_at).toLocaleString('ko-KR')}</span>
            </div>
            <div className="list-card__row" style={{ marginTop: 'var(--space-sm)' }}>
              <span style={{ color: 'var(--color-text-secondary)' }}>마지막 로그인</span>
              <span>
                {state.user.last_login_at ? new Date(state.user.last_login_at).toLocaleString('ko-KR') : '없음'}
              </span>
            </div>
          </div>

          {actionError && <p className="form-error">{actionError}</p>}

          {(state.user.account_status === 'ACTIVE' || state.user.account_status === 'SUSPENDED') && (
            <Button
              variant={state.user.account_status === 'SUSPENDED' ? 'secondary' : 'destructive'}
              disabled={isUpdatingStatus}
              onClick={() => handleToggleSuspend(state.user.account_status === 'SUSPENDED')}
              style={{ marginTop: 'var(--space-lg)' }}
            >
              {state.user.account_status === 'SUSPENDED' ? '정지 해제' : '계정 정지'}
            </Button>
          )}
        </>
      )}
    </div>
  );
}
