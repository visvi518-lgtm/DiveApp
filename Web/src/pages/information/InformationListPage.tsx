import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { informationService } from '../../services/informationService';
import type { InformationArticleListItem } from '../../models/informationModels';
import { EmptyState, ErrorState, LoadingView } from '../../components/StateViews';
import { RemoteImage } from '../../components/RemoteImage';
import { SubPageHeader } from '../../components/SubPageHeader';
import '../../components/ListCard.css';

type UiState =
  | { status: 'loading' }
  | { status: 'success'; articles: InformationArticleListItem[] }
  | { status: 'error'; message: string };

export function InformationListPage() {
  const navigate = useNavigate();
  const [state, setState] = useState<UiState>({ status: 'loading' });

  const load = useCallback(() => {
    setState({ status: 'loading' });
    informationService
      .list()
      .then((articles) => setState({ status: 'success', articles }))
      .catch((error: unknown) =>
        setState({ status: 'error', message: error instanceof Error ? error.message : '정보를 불러오지 못했습니다.' }),
      );
  }, []);

  useEffect(() => load(), [load]);

  return (
    <div>
      <SubPageHeader title="정보 게시판" onBack={() => navigate('/')} backLabel="홈" />

      {state.status === 'loading' && <LoadingView />}
      {state.status === 'error' && <ErrorState message={state.message} onRetry={load} />}
      {state.status === 'success' && state.articles.length === 0 && <EmptyState title="등록된 정보글이 없습니다" />}
      {state.status === 'success' &&
        state.articles.map((article) => (
          <Link to={`/information/${article.id}`} key={article.id} className="list-card">
            <RemoteImage src={article.thumbnail_image_url} className="thumb-16-9" />
            <div className="list-card__title" style={{ marginTop: 'var(--space-sm)' }}>
              {article.title}
            </div>
          </Link>
        ))}
    </div>
  );
}
