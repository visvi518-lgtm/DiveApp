import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { certificateService } from '../../services/certificateService';
import type { CertificateResponse } from '../../models/certificateModels';
import { Button } from '../../components/Button';
import { SubPageHeader } from '../../components/SubPageHeader';
import { ErrorState, LoadingView } from '../../components/StateViews';

type UiState = { status: 'loading' } | { status: 'success'; certificate: CertificateResponse } | { status: 'error'; message: string };

export function CertificateDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [state, setState] = useState<UiState>({ status: 'loading' });

  const load = useCallback(() => {
    if (!id) return;
    setState({ status: 'loading' });
    certificateService
      .get(id)
      .then((certificate) => setState({ status: 'success', certificate }))
      .catch((error: unknown) =>
        setState({ status: 'error', message: error instanceof Error ? error.message : '불러오기에 실패했습니다.' }),
      );
  }, [id]);

  useEffect(() => load(), [load]);

  async function handleDelete() {
    if (!id || !confirm('자격증을 삭제할까요? 삭제한 자격증은 복구할 수 없습니다.')) return;
    await certificateService.remove(id);
    navigate('/mypage/certificates');
  }

  return (
    <div>
      <SubPageHeader title="자격증 상세" onBack={() => navigate('/mypage/certificates')} backLabel="목록" />

      {state.status === 'loading' && <LoadingView />}
      {state.status === 'error' && <ErrorState message={state.message} onRetry={load} />}
      {state.status === 'success' && (
        <>
          <h3>
            {state.certificate.organization} · {state.certificate.certification_level}
          </h3>
          <DetailRow label="자격증 번호" value={state.certificate.certification_number} />
          <DetailRow label="발급일" value={state.certificate.issue_date} />
          <DetailRow label="만료일" value={state.certificate.expiration_date} />
          <DetailRow label="강사" value={state.certificate.instructor} />
          <DetailRow label="다이브 센터" value={state.certificate.dive_center} />
          <DetailRow label="메모" value={state.certificate.memo} />

          <div style={{ display: 'flex', gap: 'var(--space-md)', marginTop: 'var(--space-xl)' }}>
            <Button variant="secondary" onClick={() => navigate(`/mypage/certificates/${id}/edit`)}>
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

function DetailRow({ label, value }: { label: string; value: string | null | undefined }) {
  if (!value) return null;
  return (
    <div style={{ marginTop: 'var(--space-md)' }}>
      <div style={{ fontSize: '0.8125rem', color: 'var(--color-text-secondary)' }}>{label}</div>
      <div>{value}</div>
    </div>
  );
}
