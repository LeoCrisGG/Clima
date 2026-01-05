package com.example.clima1.utils

import android.Manifest
import android.content.Context
import android.location.Location
import androidx.compose.runtime.*
import com.google.accompanist.permissions.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequestLocationPermission(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            onPermissionGranted()
        }
    }

    if (!locationPermissionsState.allPermissionsGranted) {
        LaunchedEffect(Unit) {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }
}

suspend fun getCurrentLocation(context: Context): Location? {
    return try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).await()
    } catch (e: SecurityException) {
        null
    } catch (e: Exception) {
        null
    }
}

