import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { adminService } from '../../services/adminService';
import type { AdminDashboardStats } from '../../models/adminModels';
import { ErrorState, LoadingView } from '../../components/StateViews';
import './AdminDashboardPage.css';

type UiState =
  | { status: 'loading' }
  | { status: 'success'; stats: AdminDashboardStats }
  | { status: 'error'; message: string };

const STAT_LABELS: { key: keyof AdminDashboardStats; label: string }[] = [
  { key: 'total_user_count', label: '총 회원' },
  { key: 'new_user_count_today', label: '오늘 신규 가입' },
  { key: 'active_user_count', label: '활성 회원' },
  { key: 'post_count', label: '게시글' },
  { key: 'comment_count', label: '댓글' },
  { key: 'dive_log_count', label: '다이브 로그' },
];

export function AdminDashboardPage() {
  const [state, setState] = useState<UiState>({ status: 'loading' });

  const load = useCallback(() => {
    setState({ status: 'loading' });
    adminService
      .dashboard()
      .then((stats) => setState({ status: 'success', stats }))
      .catch((error: unknown) =>
        setState({ status: 'error', message: error instanceof Error ? error.message : '통계를 불러오지 못했습니다.' }),
      );
  }, []);

  useEffect(() => load(), [load]);

  return (
    <div>
      <h2>관리자 대시보드</h2>

      {state.status === 'loading' && <LoadingView />}
      {state.status === 'error' && <ErrorState message={state.message} onRetry={load} />}
      {state.status === 'success' && (
        <div className="admin-dashboard__stats">
          {STAT_LABELS.map(({ key, label }) => (
            <div key={key} className="admin-dashboard__stat-card">
              <div className="admin-dashboard__stat-value">{state.stats[key]}</div>
              <div className="admin-dashboard__stat-label">{label}</div>
            </div>
          ))}
        </div>
      )}

      <div className="admin-dashboard__links">
        <Link to="/admin/users" className="list-card">
          <div className="list-card__title">회원 관리</div>
          <div className="list-card__subtitle">회원 검색, 상세 조회, 정지/정지 해제</div>
        </Link>
        <Link to="/admin/information" className="list-card">
          <div className="list-card__title">정보 게시판 관리</div>
          <div className="list-card__subtitle">정보글 작성, 수정, 삭제, 발행 상태 관리</div>
        </Link>
        <Link to="/admin/banners" className="list-card">
          <div className="list-card__title">배너 관리</div>
          <div className="list-card__subtitle">배너 작성, 수정, 삭제, 노출 순서 관리</div>
        </Link>
      </div>
    </div>
  );
}
