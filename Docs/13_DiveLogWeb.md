# Dive Log Location Picker for the Web App

## Purpose

Add a NAVER Maps location picker to the web dive-log creation flow. A user
chooses a dive site on a map, confirms the site name and coordinates, and saves
the values through the existing dive-log API.

This is an implementation specification for the React/Vite application in
`Web/`. Keep the Android implementation and this web implementation separate.

## MVP scope

### Include

1. A `Select location on map` action on the web dive-log creation page.
2. A map picker presented as a dedicated route, modal, or dialog. Choose the
   approach that best matches existing UI patterns; do not add a new UI library
   only for this feature.
3. NAVER Maps JavaScript API v3 map rendering.
4. Map-tap selection with one marker at the selected coordinate.
5. A `Use current location` action using the browser Geolocation API.
6. An editable dive-site name field.
7. Returning the selected name, latitude, and longitude to the creation form.
8. Posting the existing `DiveLogCreateRequest` endpoint with both the reusable
   site coordinates and the exact coordinates selected for this dive log.

### Exclude from MVP

- Place/keyword search
- Reverse geocoding or automatic address creation
- Favorite/recent dive sites
- A map on the dive-log detail page
- A map containing multiple dive logs
- Continuous or background geolocation tracking

## Existing project state

| Area | Existing support |
| --- | --- |
| `Web/src/pages/divelog/DiveLogCreatePage.tsx` | Holds `locationName`, `latitude`, and `longitude` form state and submits a dive log. |
| `Web/src/models/diveLogModels.ts` | `DiveLocationInput` includes reusable site coordinates. `DiveLogCreateRequest` must be extended with top-level log coordinates. |
| `Web/src/services/diveLogService.ts` | Posts the create request to `POST /api/v1/dive-logs`. |
| Backend schemas/models | Validate and store location coordinates. |

The current web form uses manual latitude and longitude input. Replace or
supplement it with the map picker. Keep the existing endpoint, but extend the
web request and response TypeScript models to match the backend's top-level
`latitude` and `longitude` fields.

## Required NAVER Maps setup

1. Create or select a NAVER Cloud Platform Maps application.
2. Enable **Dynamic Map**.
3. Register all allowed web service URLs, including the local development URL
   and the production domain.
4. Issue an NCP Key ID.
5. Load NAVER Maps JavaScript API v3 using `ncpKeyId`.

Example script URL:

```text
https://oapi.map.naver.com/openapi/v3/maps.js?ncpKeyId=YOUR_NCP_KEY_ID
```

### Environment configuration

- Add `VITE_NAVER_MAP_NCP_KEY_ID` to `Web/.env.example` with no real value.
- Use `import.meta.env.VITE_NAVER_MAP_NCP_KEY_ID` in the web app.
- A JavaScript map key is visible to the browser by design; restrict it to the
  registered web origins in NAVER Cloud Platform.
- Never put a NAVER REST API Client Secret in `VITE_*` variables or any browser
  code. Secrets must only live in backend environment variables.

## Browser permissions and HTTPS

The `Use current location` action uses `navigator.geolocation`.

- It requires explicit browser permission.
- Production must be served over HTTPS.
- `localhost` can be used for local development.
- The map picker must still support manual map selection after permission is
  denied, unavailable, or times out.
- Do not request location until the user explicitly presses `Use current
  location`.

## User flow

```text
Dive log create page
  -> User chooses "Select location on map"
  -> Map picker opens
  -> Optional: user chooses "Use current location"
  -> User clicks/taps map to place or move marker
  -> User enters or confirms dive-site name
  -> User chooses "Confirm location"
  -> Create page receives siteName, latitude, longitude
  -> User saves dive log through existing API
```

## Map picker behavior

### Initial map view

- If the create form already has valid coordinates, center there and show a
  marker.
- Otherwise, use the browser location after the user explicitly requests it.
- If there is no selected point, open at a safe default viewport that can be
  configured in one place.

### Coordinate selection

- A map click sets the selected coordinate and moves the marker.
- The confirmation UI displays latitude and longitude.
- Send coordinates as finite numbers, with no more than 7 decimal places.
- Disable `Confirm location` until a valid point is selected.
- Validate latitude in `[-90, 90]` and longitude in `[-180, 180]` before
  returning data to the form.

### Site name

- The site name is always editable.
- Do not automatically create an address or a place name in the MVP.
- Do not allow confirmation with an empty/whitespace-only name.

### Errors

- Report a missing map key or NAVER Maps load/authentication failure clearly.
- Report geolocation denial, timeout, or unavailable position without blocking
  manual map selection.
- Provide a retry action for geolocation errors.

## Data contract

The API distinguishes the reusable dive-site location from the coordinate that
was selected for one specific dive log. Send the same selected coordinate in
both places for the MVP:

```ts
{
  location: {
    name: siteName,
    latitude: selectedLatitude,
    longitude: selectedLongitude,
    city: city || null,
  },
  // Exact coordinate selected for this individual log.
  latitude: selectedLatitude,
  longitude: selectedLongitude,
}
```

Do not add a new backend endpoint for the web picker. Extend
`Web/src/models/diveLogModels.ts` so `DiveLogCreateRequest` includes optional
or required top-level `latitude` and `longitude`, matching the backend schema.
Also add those two fields to `DiveLogResponse`, because the backend already
returns them. The create page sends this completed request through
`diveLogService.create`.

## Suggested file-level work

| File or area | Change |
| --- | --- |
| `Web/.env.example` | Document `VITE_NAVER_MAP_NCP_KEY_ID` without a real key. |
| `Web/src/pages/divelog/DiveLogCreatePage.tsx` | Replace/supplement manual coordinate entry with a map-picker action and selected-location summary. |
| `Web/src/pages/divelog/` | Add the map picker component/page and its CSS if needed. |
| `Web/src/App.tsx` | Add a route only if the picker is implemented as a separate page. |
| `Web/src/models/diveLogModels.ts` | Add top-level `latitude` and `longitude` to `DiveLogCreateRequest` and `DiveLogResponse`; an optional UI-only selected-location type may also be added. |

Use React hooks and the existing CSS/theme conventions. Load the NAVER Maps
script once and clean up map event listeners when the picker unmounts. Do not
use a map React wrapper unless its maintenance and NAVER Maps API support are
confirmed.

## Location reuse safety

The backend currently reuses a `DiveLocation` by name, city, and country. It
does not compare coordinates. Therefore two distinct dive sites with the same
name and city/country can be merged, and the reusable location can retain an
older coordinate.

For MVP, this must not lose the exact selected point for a dive log: always
send and persist the top-level `latitude` and `longitude` described in the data
contract above. The detail view must prefer `DiveLogResponse.latitude` and
`DiveLogResponse.longitude` when both are present, and fall back to
`DiveLogResponse.location.latitude` and `.longitude` only for older logs.

Before adding saved-location reuse, location search, or map-based grouping,
implement a coordinate-aware reuse policy. For example, require both a
normalized name match and a defined geographic distance threshold. Do not use
the reusable `DiveLocation` coordinate as the authoritative coordinate for an
individual log.

If reverse geocoding or place search is added later, call it from the backend
so the API Client Secret remains private.

## Acceptance criteria

- A user can select a point by clicking/tapping the NAVER map.
- A marker accurately represents the currently selected point.
- A user can choose current location after granting browser permission.
- Manual selection works after browser geolocation is denied or unavailable.
- The user can edit the dive-site name.
- `DiveLogCreatePage` sends the selected name, latitude, and longitude through
  the existing create API both inside `location` and as top-level log fields.
- A saved log retains its selected top-level coordinates even when another
  `DiveLocation` with the same name and city/country already exists.
- Invalid or missing coordinates and an empty site name cannot be confirmed.
- The NAVER map key is read from a Vite environment variable; no REST Client
  Secret is exposed to the browser.
- No background/continuous geolocation is added.
- Do not run a project build unless explicitly requested.

## References

- [NAVER Maps JavaScript API v3: Get a Client ID](https://navermaps.github.io/maps.js.en/docs/tutorial-1-Getting-Client-ID.html)
- [NAVER Maps JavaScript API v3: Getting Started](https://navermaps.github.io/maps.js.ncp/docs/tutorial-2-Getting-Started.html)
- [NAVER Cloud Maps overview](https://api.ncloud-docs.com/docs/en/ainaverapi-maps-overview)
