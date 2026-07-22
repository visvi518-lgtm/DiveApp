export class GeolocationError extends Error {}

/** Wraps the browser Geolocation API in a Promise (Docs/13). Only call this
 * in response to an explicit user action ("현재 위치 사용") — never on page
 * load, and never via watchPosition (no continuous/background tracking). */
export function requestCurrentPosition(): Promise<GeolocationPosition> {
  return new Promise((resolve, reject) => {
    if (!('geolocation' in navigator)) {
      reject(new GeolocationError('이 브라우저는 위치 정보를 지원하지 않습니다.'));
      return;
    }
    navigator.geolocation.getCurrentPosition(resolve, (error) => reject(toGeolocationError(error)), {
      enableHighAccuracy: true,
      timeout: 15_000,
      maximumAge: 60_000,
    });
  });
}

function toGeolocationError(error: GeolocationPositionError): GeolocationError {
  switch (error.code) {
    case error.PERMISSION_DENIED:
      return new GeolocationError('위치 권한이 거부되었습니다. 지도를 클릭해 직접 선택해주세요.');
    case error.POSITION_UNAVAILABLE:
      return new GeolocationError('현재 위치를 확인할 수 없습니다. 지도를 클릭해 직접 선택해주세요.');
    case error.TIMEOUT:
      return new GeolocationError('위치 확인이 시간 초과되었습니다. 다시 시도하거나 지도를 클릭해 직접 선택해주세요.');
    default:
      return new GeolocationError('현재 위치를 가져오지 못했습니다.');
  }
}
