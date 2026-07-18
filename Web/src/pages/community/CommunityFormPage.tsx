import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { communityService } from '../../services/communityService';
import { Button } from '../../components/Button';
import { SubPageHeader } from '../../components/SubPageHeader';
import { LoadingView } from '../../components/StateViews';

export function CommunityFormPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isEditMode = Boolean(id);

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [isLoading, setIsLoading] = useState(isEditMode);
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    communityService
      .getPost(id)
      .then((post) => {
        setTitle(post.title);
        setContent(post.content);
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
    try {
      if (id) {
        await communityService.updatePost(id, { title, content });
        navigate(`/community/${id}`);
      } else {
        const post = await communityService.createPost({ title, content });
        navigate(`/community/${post.id}`);
      }
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : '저장에 실패했습니다.');
    } finally {
      setIsSaving(false);
    }
  }

  if (isLoading) return <LoadingView />;

  return (
    <div>
      <SubPageHeader title={isEditMode ? '게시글 수정' : '글쓰기'} onBack={() => navigate(-1)} backLabel="취소" />

      <label className="form-field">
        제목
        <input value={title} onChange={(event) => setTitle(event.target.value)} />
      </label>
      <label className="form-field">
        내용
        <textarea value={content} onChange={(event) => setContent(event.target.value)} rows={10} />
      </label>

      {errorMessage && <p className="form-error">{errorMessage}</p>}

      <Button onClick={handleSave} disabled={!isValid || isSaving} style={{ marginTop: 'var(--space-lg)' }}>
        저장
      </Button>
    </div>
  );
}
