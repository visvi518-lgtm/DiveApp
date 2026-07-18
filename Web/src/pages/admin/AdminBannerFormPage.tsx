import { useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import { bannerService } from '../../services/bannerService';
import type { BannerResponse } from '../../models/bannerModels';
import type { BannerType } from '../../models/enums';
import { Button } from '../../components/Button';
import { SubPageHeader } from '../../components/SubPageHeader';
import { LoadingView } from '../../components/StateViews';

const BANNER_TYPE_OPTIONS: { value: BannerType; label: string }[] = [
  { value: 'NOTICE', label: '공지' },
  { value: 'EVENT', label: '이벤트' },
  { value: 'PROMOTION', label: '프로모션' },
  { value: 'INFORMATION', label: '정보' },
];

/** datetime-local inputs need "YYYY-MM-DDTHH:mm" with no timezone/seconds. */
function toDatetimeLocal(iso: string | null): string {
  return iso ? iso.slice(0, 16) : '';
}

export function AdminBannerFormPage() {
  const { id } = useParams<{ id: string }>();
  const location = useLocation();
  const navigate = useNavigate();
  const isEditMode = Boolean(id);

  const [title, setTitle] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [bannerType, setBannerType] = useState<BannerType>('NOTICE');
  const [targetUrl, setTargetUrl] = useState('');
  const [displayOrder, setDisplayOrder] = useState(1);
  const [isActive, setIsActive] = useState(true);
  const [startAt, setStartAt] = useState('');
  const [endAt, setEndAt] = useState('');
  const [isLoading, setIsLoading] = useState(isEditMode);
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  function applyBanner(banner: BannerResponse) {
    setTitle(banner.title);
    setImageUrl(banner.image_url);
    setBannerType(banner.banner_type);
    setTargetUrl(banner.target_url ?? '');
    setDisplayOrder(banner.display_order);
    setIsActive(banner.is_active);
    setStartAt(toDatetimeLocal(banner.start_at));
    setEndAt(toDatetimeLocal(banner.end_at));
  }

  useEffect(() => {
    if (!id) return;
    const passedBanner = location.state as BannerResponse | undefined;
    if (passedBanner?.id === id) {
      applyBanner(passedBanner);
      setIsLoading(false);
      return;
    }
    // Fallback for a direct URL visit/refresh — there is no admin single-banner
    // GET endpoint, so find it in the full admin list instead.
    bannerService
      .adminList()
      .then((banners) => {
        const found = banners.find((banner) => banner.id === id);
        if (found) applyBanner(found);
        else setErrorMessage('배너를 찾을 수 없습니다.');
      })
      .catch((error: unknown) => setErrorMessage(error instanceof Error ? error.message : '불러오기에 실패했습니다.'))
      .finally(() => setIsLoading(false));
  }, [id]);

  const isValid = title.trim().length > 0 && imageUrl.trim().length > 0;

  async function handleSave() {
    if (!isValid) {
      setErrorMessage('제목과 이미지 URL을 입력해주세요.');
      return;
    }
    if (startAt && endAt && startAt >= endAt) {
      setErrorMessage('시작 일시는 종료 일시보다 이전이어야 합니다.');
      return;
    }
    setErrorMessage(null);
    setIsSaving(true);
    const body = {
      title,
      image_url: imageUrl,
      banner_type: bannerType,
      target_url: targetUrl.trim() || null,
      display_order: displayOrder,
      is_active: isActive,
      start_at: startAt || null,
      end_at: endAt || null,
    };
    try {
      if (id) await bannerService.adminUpdate(id, body);
      else await bannerService.adminCreate(body);
      navigate('/admin/banners');
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
        title={isEditMode ? '배너 수정' : '배너 추가'}
        onBack={() => navigate('/admin/banners')}
        backLabel="취소"
      />

      <label className="form-field">
        제목
        <input value={title} onChange={(event) => setTitle(event.target.value)} />
      </label>
      <label className="form-field">
        이미지 URL
        <input value={imageUrl} onChange={(event) => setImageUrl(event.target.value)} />
      </label>
      <label className="form-field">
        유형
        <select value={bannerType} onChange={(event) => setBannerType(event.target.value as BannerType)}>
          {BANNER_TYPE_OPTIONS.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </label>
      <label className="form-field">
        연결 URL (선택)
        <input value={targetUrl} onChange={(event) => setTargetUrl(event.target.value)} />
      </label>

      <div className="form-row">
        <label className="form-field">
          노출 순서
          <input
            type="number"
            min={1}
            value={displayOrder}
            onChange={(event) => setDisplayOrder(Number(event.target.value))}
          />
        </label>
        <label className="form-field" style={{ flexDirection: 'row', alignItems: 'center', gap: 'var(--space-sm)' }}>
          <input type="checkbox" checked={isActive} onChange={(event) => setIsActive(event.target.checked)} />
          노출 활성화
        </label>
      </div>

      <div className="form-row">
        <label className="form-field">
          시작 일시 (선택)
          <input type="datetime-local" value={startAt} onChange={(event) => setStartAt(event.target.value)} />
        </label>
        <label className="form-field">
          종료 일시 (선택)
          <input type="datetime-local" value={endAt} onChange={(event) => setEndAt(event.target.value)} />
        </label>
      </div>

      {errorMessage && <p className="form-error">{errorMessage}</p>}

      <Button onClick={handleSave} disabled={!isValid || isSaving} style={{ marginTop: 'var(--space-lg)' }}>
        저장
      </Button>
    </div>
  );
}
