# Dive Log Location Picker with NAVER Maps

## Purpose

Add a location picker to the Android dive-log creation flow. A user selects a
dive site on a NAVER map, confirms a site name and coordinates, and saves that
data with the dive log.

This document is an implementation specification. Do not add unrelated map,
search, or social features as part of this task.

## MVP scope

### Include

1. A `Select location on map` action in the dive-log creation screen.
2. A separate map picker screen using the NAVER Maps Android SDK.
3. A movable/replaceable marker representing the selected coordinate.
4. A `Use current location` action when location permission is granted.
5. Manual map navigation and map-tap selection even when permission is denied.
6. A site-name text field that remains editable.
7. Returning `siteName`, `latitude`, and `longitude` to the creation screen.
8. Persisting those values through the existing dive-log create API.

### Exclude from MVP

- Place/keyword search
- Reverse geocoding and automatic address generation
- Favorite dive sites
- A map in the log detail screen
- A map containing all dive logs
- Background location tracking

## Existing project state

The backend and Android API models already support location coordinates.

| Area | Existing support |
| --- | --- |
| `Backend/app/models/dive_location.py` | A dive location has `name`, `latitude`, and `longitude`. |
| `Backend/app/models/dive_log.py` | A dive log can store its own nullable `latitude` and `longitude`. |
| `Backend/app/schemas/dive_log.py` | Create requests validate latitude and longitude ranges. |
| `Android/.../model/DiveLogModels.kt` | `DiveLocationInput` and `DiveLogCreateRequest` carry location data. |
| `Android/.../ui/divelog/DiveLogCreateViewModel.kt` | The create flow already submits site name and coordinates. |

The current Android form exposes latitude and longitude as text fields. Replace
or supplement that manual input with the map picker; keep the existing API
payload contract unless a backend change is explicitly needed.

## Required setup

### NAVER Cloud Platform

1. Create or select a NAVER Cloud Platform Maps application.
2. Enable **Dynamic Map**.
3. Register the Android package name: `com.diveapp.android`.
4. Issue an NCP Key ID.

### Android SDK configuration

1. Add the NAVER Maps Maven repository.
2. Add the NAVER Maps Android SDK dependency.
3. Configure the NCP Key ID in `AndroidManifest.xml` using
   `com.naver.maps.map.NCP_KEY_ID`.
4. Do not commit a real key. Read it from a local, ignored build configuration
   or an environment-provided CI secret.

### Android permissions

Declare and request only foreground location permissions:

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

Do not request background location permission. The feature only needs the
user's current position while the picker is open.

## User flow

```text
Dive log create screen
  -> User chooses "Select location on map"
  -> Map picker opens
  -> Optional: user chooses "Use current location"
  -> User taps map or moves marker
  -> User enters/confirms site name
  -> User chooses "Confirm location"
  -> Create screen receives siteName, latitude, longitude
  -> User saves dive log through existing API
```

## Map picker behavior

### Initial camera position

- If a valid coordinate already exists in the form, center on it and display a
  marker.
- Otherwise use the device's last/current location after permission is granted.
- If current location is unavailable or denied, open at a safe default camera
  position. The user must still be able to select a point manually.

### Selection

- Tapping the map moves the selected marker to the tapped coordinate.
- The selected coordinate is the value returned to the creation screen.
- Display the latitude and longitude for confirmation, using up to 7 decimal
  places when sent to the API.
- `Confirm location` is disabled until a coordinate is selected.

### Name

- The user can type and edit the dive-site name.
- The name must not be silently inferred from GPS or map coordinates in the MVP.
- The existing create validation requires a non-empty site name and valid
  coordinates; preserve that behavior.

### Error handling

- Show a clear error when NAVER Map authentication fails.
- Show a non-blocking message when location permission is denied.
- Show a retry action when current location cannot be obtained.
- Do not prevent manual map selection because GPS is unavailable.

## Data contract

Use the existing create request shape.

```json
{
  "location": {
    "name": "Dive site name",
    "latitude": 37.1234567,
    "longitude": 129.1234567
  },
  "latitude": 37.1234567,
  "longitude": 129.1234567
}
```

The exact surrounding dive-log fields are omitted above. Continue to populate
them as the current create screen does.

## Backend follow-up (not required for MVP UI)

The current location repository appears to reuse a `DiveLocation` by name,
city, and country. Two different dive sites can share a similar name. Before
adding saved-site reuse or search, define a coordinate-aware reuse rule (for
example, a distance threshold plus normalized name). Do not change this logic
as part of the initial map-picker UI unless the API returns incorrect data.

If reverse geocoding or place search is added later, call those REST APIs from
the backend. Never put a NAVER API Client Secret in the Android app.

## Suggested Android file-level work

| File or area | Change |
| --- | --- |
| Root Gradle settings | Add NAVER Maps Maven repository. |
| `Android/app/build.gradle.kts` | Add NAVER Maps SDK and any approved location dependency. |
| `Android/app/src/main/AndroidManifest.xml` | Add map key metadata and foreground location permissions. |
| `ui/divelog/DiveLogCreateScreen.kt` | Replace/supplement manual coordinate fields with navigation to the picker. |
| `ui/divelog/DiveLogCreateViewModel.kt` | Accept and retain the selected site name and coordinates. |
| `ui/divelog/` | Add a map picker composable/screen and its state holder. |
| `ui/divelog/DiveLogScreen.kt` | Add navigation route and result handling for the picker. |

Follow the project’s current Compose, ViewModel, repository, and navigation
patterns. Do not introduce a second networking architecture.

## Acceptance criteria

- A user can select a coordinate by tapping a NAVER map.
- A user can move to their current location after granting foreground location
  permission.
- A user can select a coordinate manually after denying location permission.
- The user can edit the dive-site name before confirmation.
- The create request contains the selected name, latitude, and longitude.
- An invalid/missing coordinate cannot be confirmed.
- No real NCP key or Client Secret is committed to Git.
- No background location permission is requested.
- Do not run a project build unless explicitly requested.

## References

- [NAVER Maps Android SDK: Get Started](https://navermaps.github.io/android-map-sdk/guide-ko/1.html)
- [NAVER Cloud Maps overview](https://api.ncloud-docs.com/docs/en/ainaverapi-maps-overview)
