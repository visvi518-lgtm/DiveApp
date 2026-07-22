package com.diveapp.android.ui.divelog

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diveapp.android.core.location.requestCurrentLocation
import com.diveapp.android.ui.ViewModelFactory
import com.diveapp.android.ui.components.ErrorStateView
import com.diveapp.android.ui.components.PrimaryButton
import com.diveapp.android.ui.components.SecondaryButton
import com.diveapp.android.ui.theme.AppSpacing
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapSdk
import com.naver.maps.map.overlay.Marker
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

/** Map picker screen for a dive log's location (Docs/12). Tap-to-select and
 * "use current location" both just produce a LatLng + site name that the
 * caller feeds into the existing create API — no search, no reverse
 * geocoding, no favorites. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiveLogLocationPickerScreen(
    initialSiteName: String,
    initialLatitude: Double?,
    initialLongitude: Double?,
    onConfirm: (PickedDiveLocation) -> Unit,
    onBack: () -> Unit,
) {
    val viewModel: DiveLogLocationPickerViewModel = viewModel(
        factory = ViewModelFactory { DiveLogLocationPickerViewModel(initialSiteName, initialLatitude, initialLongitude) },
    )

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var naverMap by remember { mutableStateOf<NaverMap?>(null) }
    var marker by remember { mutableStateOf<Marker?>(null) }

    fun hasLocationPermission(): Boolean = LOCATION_PERMISSIONS.any {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    fun useCurrentLocation() {
        viewModel.permissionDeniedMessage = null
        viewModel.locationErrorMessage = null
        viewModel.isLocating = true
        scope.launch {
            val location = try {
                withTimeoutOrNull(15_000) { requestCurrentLocation(context) }
            } catch (e: Exception) {
                null
            }
            viewModel.isLocating = false
            if (location == null) {
                viewModel.locationErrorMessage = "현재 위치를 가져오지 못했습니다. 다시 시도하거나 지도를 눌러 직접 선택해주세요."
            } else {
                val latLng = LatLng(location.latitude, location.longitude)
                viewModel.selectPoint(latLng)
                naverMap?.moveCamera(CameraUpdate.scrollTo(latLng))
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { results ->
        if (results.values.any { it }) {
            useCurrentLocation()
        } else {
            viewModel.permissionDeniedMessage = "위치 권한이 없어 현재 위치를 사용할 수 없습니다. 지도를 눌러 직접 선택해주세요."
        }
    }

    fun onUseCurrentLocationClick() {
        if (hasLocationPermission()) {
            useCurrentLocation()
        } else {
            permissionLauncher.launch(LOCATION_PERMISSIONS)
        }
    }

    // Auto-center on the device location once, only if the form didn't
    // already carry a coordinate in and permission is already granted.
    LaunchedEffect(Unit) {
        if (viewModel.selectedLatLng == null && hasLocationPermission()) {
            useCurrentLocation()
        }
    }

    // Keep the single marker in sync with the selected point.
    LaunchedEffect(viewModel.selectedLatLng, naverMap) {
        val map = naverMap ?: return@LaunchedEffect
        val latLng = viewModel.selectedLatLng
        if (latLng == null) {
            marker?.map = null
        } else {
            val currentMarker = marker ?: Marker().also { marker = it }
            currentMarker.position = latLng
            currentMarker.map = map
        }
    }

    DisposableEffect(Unit) {
        NaverMapSdk.getInstance(context).onAuthFailedListener =
            NaverMapSdk.OnAuthFailedListener { exception ->
                viewModel.authErrorMessage = "네이버 지도 인증에 실패했습니다: ${exception.message}"
            }
        onDispose {
            NaverMapSdk.getInstance(context).onAuthFailedListener = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("위치 선택") },
                navigationIcon = { TextButton(onClick = onBack) { Text("취소") } },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (viewModel.authErrorMessage != null) {
                    ErrorStateView(message = viewModel.authErrorMessage.orEmpty())
                } else {
                    val mapView = remember { MapView(context).apply { onCreate(Bundle()) } }

                    DisposableEffect(lifecycleOwner) {
                        val lifecycle = lifecycleOwner.lifecycle
                        val observer = LifecycleEventObserver { _, event ->
                            when (event) {
                                Lifecycle.Event.ON_START -> mapView.onStart()
                                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                                Lifecycle.Event.ON_STOP -> mapView.onStop()
                                else -> {}
                            }
                        }
                        lifecycle.addObserver(observer)
                        // ON_DESTROY is handled here, not via the lifecycle
                        // observer: this Compose NavHost never destroys the
                        // hosting Activity when the user navigates back, so
                        // the map must be torn down when this composable
                        // itself leaves composition instead.
                        onDispose {
                            lifecycle.removeObserver(observer)
                            mapView.onDestroy()
                        }
                    }

                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = {
                            mapView.apply {
                                getMapAsync { map ->
                                    naverMap = map
                                    map.setOnMapClickListener { _, coord -> viewModel.selectPoint(coord) }
                                    viewModel.selectedLatLng?.let { map.moveCamera(CameraUpdate.scrollTo(it)) }
                                }
                            }
                        },
                    )

                    SecondaryButton(
                        text = if (viewModel.isLocating) "위치 확인 중..." else "현재 위치 사용",
                        onClick = ::onUseCurrentLocationClick,
                        enabled = !viewModel.isLocating,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(AppSpacing.md),
                    )
                }
            }

            Column(modifier = Modifier.padding(AppSpacing.lg)) {
                viewModel.selectedLatLng?.let { latLng ->
                    Text(
                        "위도 ${"%.7f".format(latLng.latitude)}, 경도 ${"%.7f".format(latLng.longitude)}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                listOfNotNull(viewModel.locationErrorMessage, viewModel.permissionDeniedMessage).forEach { message ->
                    Text(
                        message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = AppSpacing.xs),
                    )
                }

                OutlinedTextField(
                    value = viewModel.siteName,
                    onValueChange = { viewModel.siteName = it },
                    label = { Text("다이빙 장소 이름") },
                    modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.md),
                )

                PrimaryButton(
                    text = "위치 확정",
                    onClick = { viewModel.confirmedLocation()?.let(onConfirm) },
                    enabled = viewModel.canConfirm,
                    modifier = Modifier.padding(top = AppSpacing.lg),
                )
            }
        }
    }
}
