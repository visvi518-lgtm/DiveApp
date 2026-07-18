import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { certificateService } from '../../services/certificateService';
import type { CertificationOrganization } from '../../models/enums';
import { Button } from '../../components/Button';
import { SubPageHeader } from '../../components/SubPageHeader';
import { LoadingView } from '../../components/StateViews';

const ORGANIZATIONS: CertificationOrganization[] = ['AIDA', 'PADI', 'SSI', 'RAID', 'SDI', 'NAUI', 'CMAS'];

export function CertificateFormPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isEditMode = Boolean(id);

  const [organization, setOrganization] = useState<CertificationOrganization>('PADI');
  const [certificationLevel, setCertificationLevel] = useState('');
  const [certificationNumber, setCertificationNumber] = useState('');
  const [issueDate, setIssueDate] = useState('');
  const [expirationDate, setExpirationDate] = useState('');
  const [instructor, setInstructor] = useState('');
  const [diveCenter, setDiveCenter] = useState('');
  const [memo, setMemo] = useState('');

  const [isLoading, setIsLoading] = useState(isEditMode);
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    certificateService
      .get(id)
      .then((certificate) => {
        setOrganization(certificate.organization);
        setCertificationLevel(certificate.certification_level);
        setCertificationNumber(certificate.certification_number ?? '');
        setIssueDate(certificate.issue_date ?? '');
        setExpirationDate(certificate.expiration_date ?? '');
        setInstructor(certificate.instructor ?? '');
        setDiveCenter(certificate.dive_center ?? '');
        setMemo(certificate.memo ?? '');
      })
      .catch((error: unknown) => setErrorMessage(error instanceof Error ? error.message : '불러오기에 실패했습니다.'))
      .finally(() => setIsLoading(false));
  }, [id]);

  const isValid = certificationLevel.trim().length > 0;

  async function handleSave() {
    if (!isValid) {
      setErrorMessage('등급을 입력해주세요.');
      return;
    }
    setErrorMessage(null);
    setIsSaving(true);
    try {
      const body = {
        organization,
        certification_level: certificationLevel,
        certification_number: certificationNumber || null,
        issue_date: issueDate || null,
        expiration_date: expirationDate || null,
        instructor: instructor || null,
        dive_center: diveCenter || null,
        memo: memo || null,
      };
      if (id) {
        await certificateService.update(id, body);
      } else {
        await certificateService.create(body);
      }
      navigate('/mypage/certificates');
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : '저장에 실패했습니다.');
    } finally {
      setIsSaving(false);
    }
  }

  if (isLoading) return <LoadingView />;

  return (
    <div>
      <SubPageHeader title={isEditMode ? '자격증 수정' : '자격증 추가'} onBack={() => navigate(-1)} backLabel="취소" />

      <label className="form-field">
        발급 기관
        <select value={organization} onChange={(event) => setOrganization(event.target.value as CertificationOrganization)}>
          {ORGANIZATIONS.map((org) => (
            <option key={org} value={org}>
              {org}
            </option>
          ))}
        </select>
      </label>

      <label className="form-field">
        등급
        <input value={certificationLevel} onChange={(event) => setCertificationLevel(event.target.value)} placeholder="예: Open Water Diver" />
      </label>

      <label className="form-field">
        자격증 번호 (선택)
        <input value={certificationNumber} onChange={(event) => setCertificationNumber(event.target.value)} />
      </label>

      <div className="form-row">
        <label className="form-field">
          발급일 (선택)
          <input type="date" value={issueDate} onChange={(event) => setIssueDate(event.target.value)} />
        </label>
        <label className="form-field">
          만료일 (선택)
          <input type="date" value={expirationDate} onChange={(event) => setExpirationDate(event.target.value)} />
        </label>
      </div>

      <label className="form-field">
        강사 (선택)
        <input value={instructor} onChange={(event) => setInstructor(event.target.value)} />
      </label>

      <label className="form-field">
        다이브 센터 (선택)
        <input value={diveCenter} onChange={(event) => setDiveCenter(event.target.value)} />
      </label>

      <label className="form-field">
        메모 (선택)
        <textarea value={memo} onChange={(event) => setMemo(event.target.value)} rows={3} />
      </label>

      {errorMessage && <p className="form-error">{errorMessage}</p>}

      <Button onClick={handleSave} disabled={!isValid || isSaving} style={{ marginTop: 'var(--space-lg)' }}>
        저장
      </Button>
    </div>
  );
}
