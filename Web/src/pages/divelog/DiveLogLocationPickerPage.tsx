import { useCallback, useEffect, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import type { PickedDiveLocation } from '../../models/diveLogModels';
import { GeolocationError, requestCurrentPosition } from '../../core/geolocation/currentPosition';
import {
  loadNaverMapsSdk,
  setNaverMapsAuthFailureHandler,
  type NaverMap,
  type NaverMapsNamespace,
  type NaverMarker,
} from '../../core/naverMaps/naverMapsSdk';
import { Button } from '../../components/Button';
import { SubPageHeader } from '../../components/SubPageHeader';
import { ErrorState } from '../../components/StateViews';
import './DiveLogLocationPickerPage.css';

// Seoul City Hall — a safe default viewport when no coordinate is selected
// yet and the user hasn't requested their current location (Docs/13).
const DEFAULT_CENTER = { lat: 37.5665, lng: 126.978 };

type LatLng = { lat: number; lng: number };

interface NavigationState {
  initialSiteName?: string;
  initialLatitude?: number;
  initialLongitude?: number;
}

/** Map picker page for a dive log's location (Docs/13). Click-to-select and
 * "use current location" both just produce a name + coordinate that the
 * create page feeds into the existing create API — no search, no reverse
 * geocoding, no favorites. */
export function DiveLogLocationPickerPage() {
  const navigate = useNavigate();
  const routerLocation = useLocation();
  const passedState = routerLocation.state as NavigationState | null;

  const [siteName, setSiteName] = useState(passedState?.initialSiteName ?? '');
  const [selectedLatLng, setSelectedLatLng] = useState<LatLng | null>(
    passedState?.initialLatitude != null && passedState?.initialLongitude != null
      ? { lat: passedState.initialLatitude, lng: passedState.initialLongitude }
      : null,
  );
  const [isLocating, setIsLocating] = useState(false);
  const [locationErrorMessage, setLocationErrorMessage] = useState<string | null>(null);
  const [authErrorMessage, setAuthErrorMessage] = useState<string | null>(null);

  const mapContainerRef = useRef<HTMLDivElement | null>(null);
  const naverMapsRef = useRef<NaverMapsNamespace | null>(null);
  const mapRef = useRef<NaverMap | null>(null);
  const markerRef = useRef<NaverMarker | null>(null);
  const selectedLatLngRef = useRef(selectedLatLng);
  selectedLatLngRef.current = selectedLatLng;

  const syncMarker = useCallback((point: LatLng | null) => {
    const naverMaps = naverMapsRef.current;
    const map = mapRef.current;
    if (!naverMaps || !map) return;

    if (!point) {
      markerRef.current?.setMap(null);
      return;
    }
    const latLng = new naverMaps.LatLng(point.lat, point.lng);
    if (!markerRef.current) {
      markerRef.current = new naverMaps.Marker({ position: latLng, map });
    } else {
      markerRef.current.setPosition(latLng);
      markerRef.current.setMap(map);
    }
    map.setCenter(latLng);
  }, []);

  // Load the SDK and create the map exactly once.
  useEffect(() => {
    const ncpKeyId = import.meta.env.VITE_NAVER_MAP_NCP_KEY_ID as string | undefined;
    if (!ncpKeyId) {
      setAuthErrorMessage('네이버 지도 키(VITE_NAVER_MAP_NCP_KEY_ID)가 설정되지 않았습니다.');
      return;
    }

    const clearAuthFailureHandler = setNaverMapsAuthFailureHandler(() => {
      setAuthErrorMessage('네이버 지도 인증에 실패했습니다. 키 또는 등록된 웹 서비스 URL을 확인해주세요.');
    });

    let cancelled = false;
    loadNaverMapsSdk(ncpKeyId)
      .then((naverMaps) => {
        if (cancelled || !mapContainerRef.current) return;
        const initial = selectedLatLngRef.current ?? DEFAULT_CENTER;
        const map = new naverMaps.Map(mapContainerRef.current, {
          center: new naverMaps.LatLng(initial.lat, initial.lng),
          zoom: 14,
        });
        naverMapsRef.current = naverMaps;
        mapRef.current = map;
        naverMaps.Event.addListener(map, 'click', (event) => {
          setLocationErrorMessage(null);
          setSelectedLatLng({ lat: event.coord.lat(), lng: event.coord.lng() });
        });
        syncMarker(selectedLatLngRef.current);
      })
      .catch((error: unknown) => {
        if (!cancelled) {
          setAuthErrorMessage(error instanceof Error ? error.message : '네이버 지도를 불러오지 못했습니다.');
        }
      });

    return () => {
      cancelled = true;
      clearAuthFailureHandler();
      mapRef.current?.destroy();
      mapRef.current = null;
      markerRef.current = null;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    syncMarker(selectedLatLng);
  }, [selectedLatLng, syncMarker]);

  async function handleUseCurrentLocation() {
    setLocationErrorMessage(null);
    setIsLocating(true);
    try {
      const position = await requestCurrentPosition();
      setSelectedLatLng({ lat: position.coords.latitude, lng: position.coords.longitude });
    } catch (error) {
      setLocationErrorMessage(error instanceof GeolocationError ? error.message : '현재 위치를 가져오지 못했습니다.');
    } finally {
      setIsLocating(false);
    }
  }

  const isValidCoordinate =
    selectedLatLng !== null &&
    selectedLatLng.lat >= -90 &&
    selectedLatLng.lat <= 90 &&
    selectedLatLng.lng >= -180 &&
    selectedLatLng.lng <= 180;
  const canConfirm = isValidCoordinate && siteName.trim().length > 0;

  function handleConfirm() {
    if (!canConfirm || !selectedLatLng) return;
    const picked: PickedDiveLocation = {
      siteName: siteName.trim(),
      latitude: selectedLatLng.lat,
      longitude: selectedLatLng.lng,
    };
    navigate('/dive-logs/new', { replace: true, state: { pickedLocation: picked } });
  }

  return (
    <div className="location-picker-page">
      <SubPageHeader title="위치 선택" onBack={() => navigate(-1)} backLabel="취소" />

      <div className="location-picker-page__map-area">
        {authErrorMessage ? (
          <ErrorState message={authErrorMessage} />
        ) : (
          <>
            <div ref={mapContainerRef} className="location-picker-page__map" />
            <Button
              variant="secondary"
              onClick={handleUseCurrentLocation}
              disabled={isLocating}
              className="location-picker-page__locate-button"
            >
              {isLocating ? '위치 확인 중...' : '현재 위치 사용'}
            </Button>
          </>
        )}
      </div>

      <div className="location-picker-page__form">
        {selectedLatLng && (
          <p className="location-picker-page__coords">
            위도 {selectedLatLng.lat.toFixed(7)}, 경도 {selectedLatLng.lng.toFixed(7)}
          </p>
        )}
        {locationErrorMessage && <p className="form-error">{locationErrorMessage}</p>}

        <label className="form-field">
          다이빙 장소 이름
          <input value={siteName} onChange={(event) => setSiteName(event.target.value)} />
        </label>

        <Button onClick={handleConfirm} disabled={!canConfirm} style={{ marginTop: 'var(--space-lg)' }}>
          위치 확정
        </Button>
      </div>
    </div>
  );
}
