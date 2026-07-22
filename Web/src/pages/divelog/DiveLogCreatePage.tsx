import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { diveLogService } from '../../services/diveLogService';
import type { DiveType } from '../../models/enums';
import type { PickedDiveLocation } from '../../models/diveLogModels';
import { Button } from '../../components/Button';
import { SubPageHeader } from '../../components/SubPageHeader';

function today(): string {
  return new Date().toISOString().slice(0, 10);
}

export function DiveLogCreatePage() {
  const navigate = useNavigate();
  const routerLocation = useLocation();

  const [diveType, setDiveType] = useState<DiveType>('FREEDIVING');
  const [diveDate, setDiveDate] = useState(today());
  const [locationName, setLocationName] = useState('');
  const [city, setCity] = useState('');
  const [latitude, setLatitude] = useState('');
  const [longitude, setLongitude] = useState('');
  const [memo, setMemo] = useState('');

  useEffect(() => {
    const pickedLocation = (routerLocation.state as { pickedLocation?: PickedDiveLocation } | null)?.pickedLocation;
    if (!pickedLocation) return;
    setLocationName(pickedLocation.siteName);
    setLatitude(String(pickedLocation.latitude));
    setLongitude(String(pickedLocation.longitude));
    navigate('.', { replace: true, state: null });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [routerLocation.state]);

  const [maxDepth, setMaxDepth] = useState('');
  const [diveTimeSeconds, setDiveTimeSeconds] = useState('');
  const [tankPressureStart, setTankPressureStart] = useState('');
  const [tankPressureEnd, setTankPressureEnd] = useState('');

  const [isSaving, setIsSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const hasLocation = locationName.trim().length > 0 && latitude !== '' && longitude !== '';
  const hasDetail =
    diveType === 'FREEDIVING'
      ? maxDepth !== '' && diveTimeSeconds !== ''
      : maxDepth !== '' && diveTimeSeconds !== '' && tankPressureStart !== '' && tankPressureEnd !== '';
  const isValid = hasLocation && hasDetail;

  async function handleSave() {
    if (!isValid) {
      setErrorMessage('필수 항목을 모두 입력해주세요.');
      return;
    }
    setErrorMessage(null);
    setIsSaving(true);
    try {
      const log = await diveLogService.create({
        dive_type: diveType,
        dive_date: diveDate,
        location: {
          name: locationName,
          latitude: Number(latitude),
          longitude: Number(longitude),
          city: city || null,
        },
        // Exact coordinate selected for this individual log — sent alongside
        // the reusable location's coordinate (Docs/13 data contract).
        latitude: Number(latitude),
        longitude: Number(longitude),
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
      navigate(`/dive-logs/${log.id}`);
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : '저장에 실패했습니다.');
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <div>
      <SubPageHeader title="다이브 로그 작성" onBack={() => navigate(-1)} backLabel="취소" />

      <label className="form-field">
        종류
        <select value={diveType} onChange={(event) => setDiveType(event.target.value as DiveType)}>
          <option value="FREEDIVING">프리다이빙</option>
          <option value="SCUBA">스쿠버다이빙</option>
        </select>
      </label>

      <label className="form-field">
        날짜
        <input type="date" value={diveDate} onChange={(event) => setDiveDate(event.target.value)} />
      </label>

      <label className="form-field">
        다이빙 장소
        <input value={locationName} onChange={(event) => setLocationName(event.target.value)} />
      </label>

      <Button
        variant="secondary"
        onClick={() =>
          navigate('/dive-logs/new/location', {
            state: {
              initialSiteName: locationName,
              initialLatitude: latitude !== '' ? Number(latitude) : undefined,
              initialLongitude: longitude !== '' ? Number(longitude) : undefined,
            },
          })
        }
        style={{ width: 'auto', marginBottom: 'var(--space-md)' }}
      >
        지도에서 위치 선택
      </Button>
      {latitude !== '' && longitude !== '' && (
        <p style={{ fontSize: '0.875rem', color: 'var(--color-text-secondary)', marginBottom: 'var(--space-md)' }}>
          선택된 위치: 위도 {latitude}, 경도 {longitude}
        </p>
      )}

      <label className="form-field">
        도시 (선택)
        <input value={city} onChange={(event) => setCity(event.target.value)} />
      </label>

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
