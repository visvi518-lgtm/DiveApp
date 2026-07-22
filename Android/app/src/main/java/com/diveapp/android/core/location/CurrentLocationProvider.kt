package com.diveapp.android.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class LocationUnavailableException(message: String) : Exception(message)

/** Foreground-only current-location lookup for the dive log location picker
 * (Docs/12). Uses the platform LocationManager directly instead of adding
 * play-services-location, since a one-shot fix only needs GPS/network
 * providers already available on minSdk 26. Caller must hold
 * ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION before calling this. */
@SuppressLint("MissingPermission")
suspend fun requestCurrentLocation(context: Context): Location {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val providers = listOfNotNull(
        LocationManager.GPS_PROVIDER.takeIf { locationManager.isProviderEnabled(it) },
        LocationManager.NETWORK_PROVIDER.takeIf { locationManager.isProviderEnabled(it) },
    )
    if (providers.isEmpty()) {
        throw LocationUnavailableException("위치 서비스가 꺼져 있습니다.")
    }

    providers.firstNotNullOfOrNull { locationManager.getLastKnownLocation(it) }?.let { return it }

    return suspendCancellableCoroutine { continuation ->
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationManager.removeUpdates(this)
                if (continuation.isActive) continuation.resume(location)
            }
        }
        continuation.invokeOnCancellation { locationManager.removeUpdates(listener) }
        locationManager.requestSingleUpdate(providers.first(), listener, Looper.getMainLooper())
    }
}
