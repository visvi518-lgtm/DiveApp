// Minimal ambient shape for the NAVER Maps JS API v3 surface this app
// actually uses. No official/maintained @types package is bundled — see
// Docs/13 ("Do not use a map React wrapper unless its maintenance and NAVER
// Maps API support are confirmed"), so a small local declaration is used
// instead of pulling in an unmaintained third-party typings package.
export interface NaverLatLng {
  lat(): number;
  lng(): number;
}

export interface NaverMap {
  setCenter(latLng: NaverLatLng): void;
  destroy(): void;
}

export interface NaverMarker {
  setPosition(position: NaverLatLng): void;
  setMap(map: NaverMap | null): void;
}

export interface NaverMapClickEvent {
  coord: NaverLatLng;
}

export interface NaverMapsNamespace {
  Map: new (element: HTMLElement, options: { center: NaverLatLng; zoom: number }) => NaverMap;
  Marker: new (options: { position: NaverLatLng; map?: NaverMap }) => NaverMarker;
  LatLng: new (lat: number, lng: number) => NaverLatLng;
  Event: {
    addListener: (target: NaverMap, eventName: 'click', listener: (event: NaverMapClickEvent) => void) => unknown;
  };
}

// `window.naver` is already augmented in core/auth/socialAuth.ts for the
// Naver Login SDK (a different, unrelated `window.naver.LoginWithNaverId`
// global). TypeScript's declaration merging requires every `Window.naver`
// augmentation to agree on one exact type, so this file reads the maps
// namespace through a local cast instead of adding a second, conflicting
// `declare global` for the same property.
interface NaverMapsGlobal {
  naver?: { maps?: NaverMapsNamespace };
  navermap_authFailure?: () => void;
}

function naverMapsGlobal(): NaverMapsGlobal {
  return window as unknown as NaverMapsGlobal;
}

let sdkPromise: Promise<NaverMapsNamespace> | null = null;

/** Loads the NAVER Maps JS SDK v3 exactly once and caches the in-flight
 * promise so remounting the picker page never injects the script twice
 * (Docs/13: "Load the NAVER Maps script once"). */
export function loadNaverMapsSdk(ncpKeyId: string): Promise<NaverMapsNamespace> {
  const existing = naverMapsGlobal().naver?.maps;
  if (existing) {
    return Promise.resolve(existing);
  }
  if (sdkPromise) {
    return sdkPromise;
  }

  sdkPromise = new Promise<NaverMapsNamespace>((resolve, reject) => {
    const script = document.createElement('script');
    script.src = `https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=${encodeURIComponent(ncpKeyId)}`;
    script.async = true;
    script.onload = () => {
      const maps = naverMapsGlobal().naver?.maps;
      if (maps) {
        resolve(maps);
      } else {
        reject(new Error('네이버 지도 SDK를 불러오지 못했습니다.'));
      }
    };
    script.onerror = () => reject(new Error('네이버 지도 SDK 로드에 실패했습니다.'));
    document.head.appendChild(script);
  }).catch((error: unknown) => {
    sdkPromise = null;
    throw error;
  });

  return sdkPromise;
}

/** Registers the SDK's global auth-failure hook for the caller's lifetime.
 * Returns a cleanup function that clears it — callers should invoke this
 * from a component's own unmount, since the hook is a single global slot. */
export function setNaverMapsAuthFailureHandler(handler: () => void): () => void {
  naverMapsGlobal().navermap_authFailure = handler;
  return () => {
    naverMapsGlobal().navermap_authFailure = undefined;
  };
}
