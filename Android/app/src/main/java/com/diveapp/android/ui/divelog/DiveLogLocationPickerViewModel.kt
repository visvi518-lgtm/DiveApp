package com.diveapp.android.ui.divelog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.naver.maps.geometry.LatLng

data class PickedDiveLocation(val siteName: String, val latitude: Double, val longitude: Double)

/** State holder for the NAVER Maps location picker screen (Docs/12). Holds
 * only the selected point and the editable site name — the map/marker itself
 * lives in the composable since it wraps a classic Android View. */
class DiveLogLocationPickerViewModel(
    initialSiteName: String,
    initialLatitude: Double?,
    initialLongitude: Double?,
) : ViewModel() {
    var siteName by mutableStateOf(initialSiteName)

    var selectedLatLng by mutableStateOf(
        if (initialLatitude != null && initialLongitude != null) LatLng(initialLatitude, initialLongitude) else null,
    )
        private set

    var isLocating by mutableStateOf(false)
    var locationErrorMessage by mutableStateOf<String?>(null)
    var permissionDeniedMessage by mutableStateOf<String?>(null)
    var authErrorMessage by mutableStateOf<String?>(null)

    val canConfirm: Boolean
        get() = selectedLatLng != null && siteName.isNotBlank()

    fun selectPoint(latLng: LatLng) {
        selectedLatLng = latLng
        locationErrorMessage = null
    }

    fun confirmedLocation(): PickedDiveLocation? {
        val latLng = selectedLatLng ?: return null
        if (siteName.isBlank()) return null
        return PickedDiveLocation(siteName.trim(), latLng.latitude, latLng.longitude)
    }
}
