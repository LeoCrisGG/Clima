package com.example.clima

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.clima.ui.screens.ImprovedWeatherScreen
import com.example.clima.ui.theme.ClimaTheme
import com.example.clima.viewmodel.WeatherViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

class MainActivity : ComponentActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            ClimaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    WeatherApp(
                        onRequestLocation = { onLocationPermissionGranted ->
                            requestLocationAndLoadWeather(onLocationPermissionGranted)
                        },
                        onShowToast = { message ->
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    private fun requestLocationAndLoadWeather(onLocationObtained: (Double, Double) -> Unit) {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation(onLocationObtained)
        } else {
            Toast.makeText(
                this,
                "Se necesita permiso de ubicación para obtener el clima local",
                Toast.LENGTH_LONG
            ).show()
            // Usar ubicación por defecto (Ciudad de México)
            onLocationObtained(19.4326, -99.1332)
        }
    }

    private fun getCurrentLocation(onLocationObtained: (Double, Double) -> Unit) {
        try {
            Toast.makeText(this, "📍 Obteniendo tu ubicación...", Toast.LENGTH_SHORT).show()

            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    Toast.makeText(
                        this,
                        "✅ Ubicación detectada correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    onLocationObtained(location.latitude, location.longitude)
                } else {
                    // Intentar última ubicación conocida
                    getLastKnownLocation(onLocationObtained)
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "⚠️ Error al obtener ubicación. Usando ubicación por defecto.",
                    Toast.LENGTH_LONG
                ).show()
                // Ubicación por defecto
                onLocationObtained(19.4326, -99.1332)
            }
        } catch (e: SecurityException) {
            Toast.makeText(
                this,
                "⚠️ Permiso de ubicación denegado.",
                Toast.LENGTH_LONG
            ).show()
            // Ubicación por defecto
            onLocationObtained(19.4326, -99.1332)
        }
    }

    private fun getLastKnownLocation(onLocationObtained: (Double, Double) -> Unit) {
        try {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    Toast.makeText(
                        this,
                        "📍 Usando última ubicación conocida",
                        Toast.LENGTH_SHORT
                    ).show()
                    onLocationObtained(location.latitude, location.longitude)
                } else {
                    Toast.makeText(
                        this,
                        "⚠️ No se pudo obtener la ubicación. Usando ubicación por defecto.",
                        Toast.LENGTH_LONG
                    ).show()
                    onLocationObtained(19.4326, -99.1332)
                }
            }
        } catch (e: SecurityException) {
            onLocationObtained(19.4326, -99.1332)
        }
    }
}

@Composable
fun WeatherApp(
    onRequestLocation: (onLocationObtained: (Double, Double) -> Unit) -> Unit,
    onShowToast: (String) -> Unit,
    viewModel: WeatherViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val citySuggestions by viewModel.citySuggestions.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    var currentLat by remember { mutableStateOf(0.0) }
    var currentLon by remember { mutableStateOf(0.0) }
    var hasRequestedLocation by remember { mutableStateOf(false) }

    // Solicitar permisos de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            onRequestLocation { lat, lon ->
                currentLat = lat
                currentLon = lon
                viewModel.loadWeatherData(lat, lon)
            }
        } else {
            onShowToast("⚠️ Permiso de ubicación denegado. Usando ubicación por defecto.")
            // Si no se conceden permisos, usar ubicación por defecto
            currentLat = 19.4326
            currentLon = -99.1332
            viewModel.loadWeatherData(currentLat, currentLon)
        }
    }

    // Solicitar ubicación al iniciar
    LaunchedEffect(Unit) {
        if (!hasRequestedLocation) {
            hasRequestedLocation = true
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    ImprovedWeatherScreen(
        uiState = uiState,
        searchQuery = searchQuery,
        citySuggestions = citySuggestions,
        isSearching = isSearching,
        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
        onSelectCity = { city ->
            onShowToast("📍 Cargando clima de ${city.name}...")
            viewModel.selectCity(city)
        },
        onClearSuggestions = {
            viewModel.clearSuggestions()
        },
        onRetry = {
            if (currentLat != 0.0 && currentLon != 0.0) {
                viewModel.retry(currentLat, currentLon)
            } else {
                viewModel.retry(19.4326, -99.1332)
            }
        },
        onRefresh = {
            onShowToast("🔄 Actualizando datos...")
            if (currentLat != 0.0 && currentLon != 0.0) {
                viewModel.loadWeatherData(currentLat, currentLon)
            }
        },
        onRequestLocationRefresh = {
            onShowToast("📍 Actualizando ubicación...")
            onRequestLocation { lat, lon ->
                currentLat = lat
                currentLon = lon
                viewModel.loadWeatherData(lat, lon)
            }
        }
    )
}