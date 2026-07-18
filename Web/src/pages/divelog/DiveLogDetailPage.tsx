import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { diveLogService } from '../../services/diveLogService';
import type { DiveLogResponse } from '../../models/diveLogModels';
import { Button } from '../../components/Button';
import { SubPageHeader } from '../../components/SubPageHeader';
import { ErrorState, LoadingView } from '../../components/StateViews';

type UiState = { status: 'loading' } | { status: 'success'; log: DiveLogResponse } | { status: 'error'; message: string };

export function DiveLogDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [state, setState] = useState<UiState>({ status: 'loading' });

  const load = useCallback(() => {
    if (!id) return;
    setState({ status: 'loading' });
    diveLogService
      .get(id)
      .then((log) => setState({ status: 'success', log }))
      .catch((error: unknown) =>
        setState({ status: 'error', message: error instanceof Error ? error.message : '다이브 로그를 불러오지 못했습니다.' }),
      );
  }, [id]);

  useEffect(() => load(), [load]);

  async function handleDelete() {
    if (!id || !confirm('다이브 로그를 삭제할까요? 삭제한 로그는 복구할 수 없습니다.')) return;
    await diveLogService.remove(id);
    navigate('/dive-logs');
  }

  return (
    <div>
      <SubPageHeader title="다이브 로그 상세" onBack={() => navigate('/dive-logs')} backLabel="목록" />

      {state.status === 'loading' && <LoadingView />}
      {state.status === 'error' && <ErrorState message={state.message} onRetry={load} />}
      {state.status === 'success' && (
        <>
          <h2>{state.log.dive_type === 'FREEDIVING' ? '프리다이빙' : '스쿠버다이빙'}</h2>
          <p style={{ color: 'var(--color-text-secondary)' }}>
            {state.log.dive_date} · {state.log.location.name}
          </p>

          {state.log.freediving && (
            <>
              <DetailRow label="최대 수심" value={`${state.log.freediving.max_depth}m`} />
              <DetailRow label="다이빙 시간" value={`${state.log.freediving.dive_time_seconds}초`} />
            </>
          )}
          {state.log.scuba && (
            <>
              <DetailRow label="최대 수심" value={`${state.log.scuba.max_depth}m`} />
              <DetailRow label="다이빙 시간" value={`${state.log.scuba.dive_time_seconds}초`} />
              <DetailRow label="탱크 압력" value={`${state.log.scuba.tank_pressure_start} → ${state.log.scuba.tank_pressure_end} bar`} />
            </>
          )}
          {state.log.memo && <DetailRow label="메모" value={state.log.memo} />}

          <div style={{ display: 'flex', gap: 'var(--space-md)', marginTop: 'var(--space-xl)' }}>
            <Button variant="secondary" onClick={() => navigate(`/dive-logs/${id}/edit`)}>
              수정
            </Button>
            <Button variant="destructive" onClick={handleDelete}>
              삭제
            </Button>
          </div>
        </>
      )}
    </div>
  );
}

function DetailRow({ label, value }: { label: string; value: string }) {
  return (
    <div style={{ marginTop: 'var(--space-md)' }}>
      <div style={{ fontSize: '0.8125rem', color: 'var(--color-text-secondary)' }}>{label}</div>
      <div>{value}</div>
    </div>
  );
}
