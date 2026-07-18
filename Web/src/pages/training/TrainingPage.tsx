import { useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { trainingService } from '../../services/trainingService';
import { Button } from '../../components/Button';

type Step = 'setup' | 'running' | 'result';
type Phase = 'rest' | 'hold';

function restSecondsFor(setIndex: number, restTime: number, restInterval: number): number {
  return Math.max(5, restTime + setIndex * restInterval);
}

function holdSecondsFor(setIndex: number, holdTime: number, holdInterval: number): number {
  return Math.max(5, holdTime + setIndex * holdInterval);
}

function formatSeconds(totalSeconds: number): string {
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = totalSeconds % 60;
  return `${minutes}:${String(seconds).padStart(2, '0')}`;
}

/** CO2 Table training: the timer runs entirely on the client
 * (Docs/02_Requirements.md 3.3) — the server only stores the outcome. */
export function TrainingPage() {
  const [step, setStep] = useState<Step>('setup');

  const [totalSets, setTotalSets] = useState(8);
  const [restTime, setRestTime] = useState(120);
  const [holdTime, setHoldTime] = useState(60);
  const [restInterval, setRestInterval] = useState(-15);
  const [holdInterval, setHoldInterval] = useState(10);

  const [currentSetIndex, setCurrentSetIndex] = useState(0);
  const [phase, setPhase] = useState<Phase>('rest');
  const [remainingSeconds, setRemainingSeconds] = useState(0);
  const [isPaused, setIsPaused] = useState(false);
  const [completedSets, setCompletedSets] = useState(0);
  const [isCompleted, setIsCompleted] = useState(false);

  const [isSaving, setIsSaving] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);
  const [saveCompleted, setSaveCompleted] = useState(false);

  const isPausedRef = useRef(isPaused);
  isPausedRef.current = isPaused;

  const isSetupValid = totalSets >= 5 && totalSets <= 20 && restTime > 0 && holdTime > 0;

  useEffect(() => {
    if (step !== 'running') return;

    const intervalId = setInterval(() => {
      if (isPausedRef.current) return;
      setRemainingSeconds((seconds) => {
        if (seconds > 1) return seconds - 1;

        // Current phase finished — advance the state machine.
        if (phase === 'rest') {
          setPhase('hold');
          return holdSecondsFor(currentSetIndex, holdTime, holdInterval);
        }

        const justCompletedSets = currentSetIndex + 1;
        setCompletedSets(justCompletedSets);
        if (justCompletedSets >= totalSets) {
          setIsCompleted(true);
          setStep('result');
          return 0;
        }
        setCurrentSetIndex((index) => index + 1);
        setPhase('rest');
        return restSecondsFor(currentSetIndex + 1, restTime, restInterval);
      });
    }, 1000);

    return () => clearInterval(intervalId);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [step, phase, currentSetIndex]);

  function handleStart() {
    if (!isSetupValid) return;
    setCurrentSetIndex(0);
    setCompletedSets(0);
    setIsPaused(false);
    setPhase('rest');
    setRemainingSeconds(restSecondsFor(0, restTime, restInterval));
    setStep('running');
  }

  function handleStop() {
    setIsCompleted(false);
    setStep('result');
  }

  function handleRestart() {
    setStep('setup');
    setSaveCompleted(false);
    setSaveError(null);
  }

  async function handleSaveResult() {
    setIsSaving(true);
    setSaveError(null);
    try {
      await trainingService.create({
        total_sets: totalSets,
        completed_sets: completedSets,
        is_completed: isCompleted,
        rest_time_seconds: restTime,
        hold_time_seconds: holdTime,
        rest_interval_seconds: restInterval,
        hold_interval_seconds: holdInterval,
      });
      setSaveCompleted(true);
    } catch (error) {
      setSaveError(error instanceof Error ? error.message : '훈련 기록 저장에 실패했습니다.');
    } finally {
      setIsSaving(false);
    }
  }

  if (step === 'running') {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 'var(--space-md)', paddingTop: 'var(--space-xxl)' }}>
        <div>
          SET {currentSetIndex + 1} / {totalSets}
        </div>
        <h1>{phase === 'rest' ? '휴식' : '숨참기'}</h1>
        <div style={{ fontSize: '3rem', fontWeight: 700 }}>{formatSeconds(remainingSeconds)}</div>
        <div style={{ width: '100%', maxWidth: 320 }}>
          <Button variant="secondary" onClick={() => setIsPaused((value) => !value)}>
            {isPaused ? '재개' : '일시정지'}
          </Button>
          <div style={{ height: 'var(--space-md)' }} />
          <Button variant="destructive" onClick={handleStop}>
            종료
          </Button>
        </div>
      </div>
    );
  }

  if (step === 'result') {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 'var(--space-md)', paddingTop: 'var(--space-xxl)' }}>
        <h1>{isCompleted ? '훈련을 완료했습니다!' : '훈련이 중단되었습니다'}</h1>
        <p>
          완료 세트: {completedSets} / {totalSets}
        </p>
        {saveError && <p className="form-error">{saveError}</p>}
        {saveCompleted ? (
          <>
            <p style={{ color: 'var(--color-success)' }}>기록이 저장되었습니다.</p>
            <Button onClick={handleRestart} style={{ maxWidth: 320 }}>
              확인
            </Button>
          </>
        ) : (
          <Button onClick={handleSaveResult} disabled={isSaving} style={{ maxWidth: 320 }}>
            기록 저장
          </Button>
        )}
      </div>
    );
  }

  return (
    <div>
      <div className="list-card__row" style={{ marginBottom: 'var(--space-lg)' }}>
        <h2>CO₂ Table</h2>
        <Link to="/training/history">기록</Link>
      </div>

      <label className="form-field">
        세트 수: {totalSets}
        <input
          type="range"
          min={5}
          max={20}
          value={totalSets}
          onChange={(event) => setTotalSets(Number(event.target.value))}
        />
      </label>

      <label className="form-field">
        휴식 시간 (초)
        <input type="number" value={restTime} onChange={(event) => setRestTime(Number(event.target.value))} />
      </label>
      <label className="form-field">
        숨참기 시간 (초)
        <input type="number" value={holdTime} onChange={(event) => setHoldTime(Number(event.target.value))} />
      </label>

      <div className="form-row">
        <label className="form-field">
          세트당 휴식 변화(초)
          <input type="number" value={restInterval} onChange={(event) => setRestInterval(Number(event.target.value))} />
        </label>
        <label className="form-field">
          세트당 숨참기 변화(초)
          <input type="number" value={holdInterval} onChange={(event) => setHoldInterval(Number(event.target.value))} />
        </label>
      </div>

      {!isSetupValid && <p className="form-error">세트 수는 5~20, 휴식/숨참기 시간은 0보다 커야 합니다.</p>}

      <Button onClick={handleStart} disabled={!isSetupValid} style={{ marginTop: 'var(--space-lg)' }}>
        훈련 시작
      </Button>
    </div>
  );
}
