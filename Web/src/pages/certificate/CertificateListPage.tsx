import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { certificateService } from '../../services/certificateService';
import type { CertificateResponse } from '../../models/certificateModels';
import { EmptyState, ErrorState, LoadingView } from '../../components/StateViews';
import '../../components/ListCard.css';

type UiState =
  | { status: 'loading' }
  | { status: 'success'; certificates: CertificateResponse[] }
  | { status: 'error'; message: string };

export function CertificateListPage() {
  const [state, setState] = useState<UiState>({ status: 'loading' });

  const load = useCallback(() => {
    setState({ status: 'loading' });
    certificateService
      .list()
      .then((certificates) => setState({ status: 'success', certificates }))
      .catch((error: unknown) =>
        setState({ status: 'error', message: error instanceof Error ? error.message : '자격증을 불러오지 못했습니다.' }),
      );
  }, []);

  useEffect(() => load(), [load]);

  return (
    <div>
      <div className="list-card__row" style={{ marginBottom: 'var(--space-lg)' }}>
        <h2>자격증 관리</h2>
        <Link to="/mypage/certificates/new" className="btn btn-primary" style={{ width: 'auto' }}>
          + 추가
        </Link>
      </div>

      {state.status === 'loading' && <LoadingView />}
      {state.status === 'error' && <ErrorState message={state.message} onRetry={load} />}
      {state.status === 'success' && state.certificates.length === 0 && (
        <EmptyState title="등록된 자격증이 없습니다" message="오른쪽 위 + 버튼으로 자격증을 추가해보세요." />
      )}
      {state.status === 'success' &&
        state.certificates.map((certificate) => (
          <Link to={`/mypage/certificates/${certificate.id}`} key={certificate.id} className="list-card">
            <div className="list-card__title">
              {certificate.organization} · {certificate.certification_level}
            </div>
            {certificate.dive_center && <div className="list-card__subtitle">{certificate.dive_center}</div>}
          </Link>
        ))}
    </div>
  );
}
