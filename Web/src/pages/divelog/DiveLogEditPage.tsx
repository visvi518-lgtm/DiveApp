import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { diveLogService } from '../../services/diveLogService';
import type { DiveType } from '../../models/enums';
import { Button } from '../../components/Button';
import { SubPageHeader } from '../../components/SubPageHeader';
import { LoadingView } from '../../components/StateViews';

/** Mirrors the backend's update capability: only memo and type-specific
 * numbers can change — dive_type/date/location are fixed after creation. */
export function DiveLogEditPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const [diveType, setDiveType] = useState<DiveType>('FREEDIVING');
  const [memo, setMemo] = useState('');
  const [maxDepth, setMaxDepth] = useState('');
  const [diveTimeSeconds, setDiveTimeSeconds] = useState('');
  const [tankPressureStart, setTankPressureStart] = useState('');
  const [tankPressureEnd, setTankPressureEnd] = useState('');

  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    diveLogService
      .get(id)
      .then((log) => {
        setDiveType(log.dive_type);
        setMemo(log.memo ?? '');
        if (log.freediving) {
          setMaxDepth(String(log.freediving.max_depth));
          setDiveTimeSeconds(String(log.freediving.dive_time_seconds));
        }
        if (log.scuba) {
          setMaxDepth(String(log.scuba.max_depth));
          setDiveTimeSeconds(String(log.scuba.dive_time_seconds));
          setTankPressureStart(String(log.scuba.tank_pressure_start));
          setTankPressureEnd(String(log.scuba.tank_pressure_end));
        }
      })
      .catch((error: unknown) => setErrorMessage(error instanceof Error ? error.message : '불러오기에 실패했습니다.'))
      .finally(() => setIsLoading(false));
  }, [id]);

  const isValid =
    diveType === 'FREEDIVING'
      ? maxDepth !== '' && diveTimeSeconds !== ''
      : maxDepth !== '' && diveTimeSeconds !== '' && tankPressureStart !== '' && tankPressureEnd !== '';

  async function handleSave() {
    if (!id || !isValid) {
      setErrorMessage('필수 항목을 모두 입력해주세요.');
      return;
    }
    setErrorMessage(null);
    setIsSaving(true);
    try {
      await diveLogService.update(id, {
        memo: memo || null,
        freediving:
          diveType === 'FREEDIVING' ? { max_depth: Number(maxDepth), dive_time_seconds: Number(diveTimeSeconds) } : null,
        scuba:
          diveType === 'SCUBA'
            ? {
                max_depth: Number(maxDepth),
                dive_time_seconds: Number(diveTimeSeconds),
                tank_pressure_start: Number(tankPressureStart),
                tank_pressure_end: Number(tankPressureEnd),
              }
            : null,
      });
      navigate(`/dive-logs/${id}`);
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : '저장에 실패했습니다.');
    } finally {
      setIsSaving(false);
    }
  }

  if (isLoading) return <LoadingView />;

  return (
    <div>
      <SubPageHeader title="다이브 로그 수정" onBack={() => navigate(-1)} backLabel="취소" />

      <label className="form-field">
        최대 수심 (m)
        <input value={maxDepth} onChange={(event) => setMaxDepth(event.target.value)} inputMode="decimal" />
      </label>
      <label className="form-field">
        다이빙 시간 (초)
        <input value={diveTimeSeconds} onChange={(event) => setDiveTimeSeconds(event.target.value)} inputMode="numeric" />
      </label>

      {diveType === 'SCUBA' && (
        <div className="form-row">
          <label className="form-field">
            시작 압력 (bar)
            <input value={tankPressureStart} onChange={(event) => setTankPressureStart(event.target.value)} inputMode="numeric" />
          </label>
          <label className="form-field">
            종료 압력 (bar)
            <input value={tankPressureEnd} onChange={(event) => setTankPressureEnd(event.target.value)} inputMode="numeric" />
          </label>
        </div>
      )}

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
