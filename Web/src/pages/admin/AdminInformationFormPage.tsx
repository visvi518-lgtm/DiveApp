import { useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { informationService } from '../../services/informationService';
import type { InformationArticleResponse } from '../../models/informationModels';
import { Button } from '../../components/Button';
import { SubPageHeader } from '../../components/SubPageHeader';
import { LoadingView } from '../../components/StateViews';

export function AdminInformationFormPage() {
  const { id } = useParams<{ id: string }>();
  const location = useLocation();
  const navigate = useNavigate();
  const isEditMode = Boolean(id);

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [thumbnailImageUrl, setThumbnailImageUrl] = useState('');
  const [isPublished, setIsPublished] = useState(false);
  const [isLoading, setIsLoading] = useState(isEditMode);
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  function applyArticle(article: InformationArticleResponse) {
    setTitle(article.title);
    setContent(article.content);
    setThumbnailImageUrl(article.thumbnail_image_url ?? '');
    setIsPublished(article.is_published);
  }

  useEffect(() => {
    if (!id) return;
    const passedArticle = location.state as InformationArticleResponse | undefined;
    if (passedArticle?.id === id) {
      applyArticle(passedArticle);
      setIsLoading(false);
      return;
    }
    // Fallback for a direct URL visit/refresh — there is no admin single-article
    // GET endpoint, so find it in the full admin list instead.
    informationService
      .adminList()
      .then((articles) => {
        const found = articles.find((article) => article.id === id);
        if (found) applyArticle(found);
        else setErrorMessage('정보글을 찾을 수 없습니다.');
      })
      .catch((error: unknown) => setErrorMessage(error instanceof Error ? error.message : '불러오기에 실패했습니다.'))
      .finally(() => setIsLoading(false));
  }, [id]);

  const isValid = title.trim().length > 0 && content.trim().length > 0;

  async function handleSave() {
    if (!isValid) {
      setErrorMessage('제목과 내용을 입력해주세요.');
      return;
    }
    setErrorMessage(null);
    setIsSaving(true);
    const body = {
      title,
      content,
      thumbnail_image_url: thumbnailImageUrl.trim() || null,
      is_published: isPublished,
    };
    try {
      if (id) await informationService.adminUpdate(id, body);
      else await informationService.adminCreate(body);
      navigate('/admin/information');
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : '저장에 실패했습니다.');
    } finally {
      setIsSaving(false);
    }
  }

  if (isLoading) return <LoadingView />;

  return (
    <div>
      <SubPageHeader
        title={isEditMode ? '정보글 수정' : '정보글 작성'}
        onBack={() => navigate('/admin/information')}
        backLabel="취소"
      />

      <label className="form-field">
        제목
        <input value={title} onChange={(event) => setTitle(event.target.value)} />
      </label>
      <label className="form-field">
        내용
        <textarea value={content} onChange={(event) => setContent(event.target.value)} rows={10} />
      </label>
      <label className="form-field">
        썸네일 이미지 URL
        <input value={thumbnailImageUrl} onChange={(event) => setThumbnailImageUrl(event.target.value)} />
      </label>
      <label className="form-field" style={{ flexDirection: 'row', alignItems: 'center', gap: 'var(--space-sm)' }}>
        <input type="checkbox" checked={isPublished} onChange={(event) => setIsPublished(event.target.checked)} />
        발행하기 (체크 해제 시 비공개 상태로 저장)
      </label>

      {errorMessage && <p className="form-error">{errorMessage}</p>}

      <Button onClick={handleSave} disabled={!isValid || isSaving} style={{ marginTop: 'var(--space-lg)' }}>
        저장
      </Button>
    </div>
  );
}
