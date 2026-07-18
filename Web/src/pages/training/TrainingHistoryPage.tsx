import { useCallback, useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { trainingService } from '../../services/trainingService';
import type { TrainingRecordResponse, TrainingStatisticsResponse } from '../../models/trainingModels';
import { EmptyState, ErrorState, LoadingView } from '../../components/StateViews';
import { SubPageHeader } from '../../components/SubPageHeader';
import '../../components/ListCard.css';

type UiState =
  | { status: 'loading' }
  | { status: 'success'; records: TrainingRecordResponse[]; stats: TrainingStatisticsResponse }
  | { status: 'error'; message: string };

export function TrainingHistoryPage() {
  const navigate = useNavigate();
  const [state, setState] = useState<UiState>({ status: 'loading' });

  const load = useCallback(() => {
    setState({ status: 'loading' });
    Promise.all([trainingService.list(), trainingService.statistics()])
      .then(([records, stats]) => setState({ status: 'success', records, stats }))
      .catch((error: unknown) =>
        setState({ status: 'error', message: error instanceof Error ? error.message : '훈련 기록을 불러오지 못했습니다.' }),
      );
  }, []);

  useEffect(() => load(), [load]);

  return (
    <div>
      <SubPageHeader title="훈련 기록" onBack={() => navigate('/training')} backLabel="닫기" />

      {state.status === 'loading' && <LoadingView />}
      {state.status === 'error' && <ErrorState message={state.message} onRetry={load} />}
      {state.status === 'success' && (
        <>
          <div className="list-page__stats">
            <div>총 훈련 횟수: {state.stats.total_training_count}</div>
            <div>완료율: {Math.round(state.stats.completion_rate * 100)}%</div>
            <div>평균 완료 세트: {state.stats.average_completed_sets.toFixed(1)}</div>
          </div>

          {state.records.length === 0 ? (
            <EmptyState title="훈련 기록이 없습니다" />
          ) : (
            state.records.map((record) => (
              <div key={record.id} className="list-card list-card__row">
                <span>{new Date(record.completed_at).toLocaleString('ko-KR')}</span>
                <span>
                  {record.completed_sets}/{record.total_sets} 세트 {record.is_completed ? '· 완료' : '· 중단'}
                </span>
              </div>
            ))
          )}
        </>
      )}
    </div>
  );
}
