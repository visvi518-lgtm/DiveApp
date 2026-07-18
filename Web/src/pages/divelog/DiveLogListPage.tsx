import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { diveLogService } from '../../services/diveLogService';
import type { DiveLogListItem, DiveLogStatisticsResponse } from '../../models/diveLogModels';
import { EmptyState, ErrorState, LoadingView } from '../../components/StateViews';
import '../../components/ListCard.css';

type UiState =
  | { status: 'loading' }
  | { status: 'success'; logs: DiveLogListItem[]; stats: DiveLogStatisticsResponse }
  | { status: 'error'; message: string };

export function DiveLogListPage() {
  const [state, setState] = useState<UiState>({ status: 'loading' });

  const load = useCallback(() => {
    setState({ status: 'loading' });
    Promise.all([diveLogService.list(), diveLogService.statistics()])
      .then(([logs, stats]) => setState({ status: 'success', logs, stats }))
      .catch((error: unknown) =>
        setState({ status: 'error', message: error instanceof Error ? error.message : '다이브 로그를 불러오지 못했습니다.' }),
      );
  }, []);

  useEffect(() => load(), [load]);

  return (
    <div>
      <div className="list-card__row" style={{ marginBottom: 'var(--space-lg)' }}>
        <h2>다이브 로그</h2>
        <Link to="/dive-logs/new" className="btn btn-primary" style={{ width: 'auto' }}>
          + 작성
        </Link>
      </div>

      {state.status === 'loading' && <LoadingView />}
      {state.status === 'error' && <ErrorState message={state.message} onRetry={load} />}
      {state.status === 'success' && (
        <>
          <div className="list-page__stats">
            <strong>
              총 {state.stats.total_dive_count}회 · 최대 수심 {state.stats.max_depth_overall}m
            </strong>
            <div style={{ color: 'var(--color-text-secondary)', marginTop: 'var(--space-xs)' }}>
              프리다이빙 {state.stats.freediving_count}회 · 스쿠버 {state.stats.scuba_count}회
            </div>
          </div>

          {state.logs.length === 0 ? (
            <EmptyState title="등록된 다이브 로그가 없습니다" message="오른쪽 위 + 버튼으로 첫 로그를 남겨보세요." />
          ) : (
            state.logs.map((log) => (
              <Link to={`/dive-logs/${log.id}`} key={log.id} className="list-card">
                <div className="list-card__title">{log.dive_type === 'FREEDIVING' ? '프리다이빙' : '스쿠버다이빙'}</div>
                <div className="list-card__subtitle">{log.location_name}</div>
                <div className="list-card__subtitle">
                  {log.dive_date} · 최대 {log.max_depth}m
                </div>
              </Link>
            ))
          )}
        </>
      )}
    </div>
  );
}
