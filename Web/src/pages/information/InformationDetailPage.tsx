import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { informationService } from '../../services/informationService';
import type { InformationArticleResponse } from '../../models/informationModels';
import { ErrorState, LoadingView } from '../../components/StateViews';
import { RemoteImage } from '../../components/RemoteImage';
import { SubPageHeader } from '../../components/SubPageHeader';

type UiState =
  | { status: 'loading' }
  | { status: 'success'; article: InformationArticleResponse }
  | { status: 'error'; message: string };

export function InformationDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [state, setState] = useState<UiState>({ status: 'loading' });

  const load = useCallback(() => {
    if (!id) return;
    setState({ status: 'loading' });
    informationService
      .get(id)
      .then((article) => setState({ status: 'success', article }))
      .catch((error: unknown) =>
        setState({ status: 'error', message: error instanceof Error ? error.message : '게시글을 불러오지 못했습니다.' }),
      );
  }, [id]);

  useEffect(() => load(), [load]);

  return (
    <div>
      <SubPageHeader title="정보 게시글" onBack={() => navigate('/information')} backLabel="목록" />
      {state.status === 'loading' && <LoadingView />}
      {state.status === 'error' && <ErrorState message={state.message} onRetry={load} />}
      {state.status === 'success' && (
        <>
          <RemoteImage src={state.article.thumbnail_image_url} className="thumb-16-9" />
          <h2 style={{ marginTop: 'var(--space-lg)' }}>{state.article.title}</h2>
          <p style={{ marginTop: 'var(--space-lg)', whiteSpace: 'pre-wrap' }}>{state.article.content}</p>
        </>
      )}
    </div>
  );
}
