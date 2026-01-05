package com.example.clima1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.clima1.data.model.FavoriteLocation
import com.example.clima1.ui.screens.MapScreen
import com.example.clima1.ui.screens.WeatherScreen
import com.example.clima1.ui.theme.Clima1Theme
import com.example.clima1.ui.viewmodel.WeatherViewModel
import com.example.clima1.utils.RequestLocationPermission
import com.example.clima1.utils.getCurrentLocation
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

/**
 * MainActivity - Actividad principal de la aplicación.
 * Es el punto de entrada de la app y configura el contenido de la interfaz con Jetpack Compose.
 */
class MainActivity : ComponentActivity() {
    /**
     * onCreate - Se ejecuta cuando se crea la actividad.
     * Configura la UI con Compose y habilita el modo edge-to-edge.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Habilitar modo edge-to-edge (pantalla completa sin barras de sistema)
        enableEdgeToEdge()

        // Configurar el contenido de la UI con Jetpack Compose
        setContent {
            // Aplicar el tema de la aplicación
            Clima1Theme {
                // Llamar a la función composable principal
                WeatherApp()
            }
        }
    }
}

/**
 * WeatherApp - Función composable principal que define toda la estructura de la app.
 *
 * Estructura:
 * - TopAppBar: Barra superior con botones de ubicación y actualizar
 * - Contenido: Pestañas que alternan entre WeatherScreen y MapScreen
 * - BottomNavigation: Barra inferior con navegación entre pestañas
 * - Snackbar: Mensajes temporales de error/éxito
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun WeatherApp() {
    // Obtener instancia del ViewModel (se mantiene durante rotaciones de pantalla)
    val viewModel: WeatherViewModel = viewModel()

    // Observar el estado de la UI - se actualiza automáticamente cuando cambia
    val uiState by viewModel.uiState.collectAsState()

    // Obtener el contexto de Android para acceder al GPS
    val context = LocalContext.current

    // Scope de coroutines para operaciones asíncronas
    val scope = rememberCoroutineScope()

    // Estado para controlar qué pestaña está seleccionada (0 = Clima, 1 = Mapa)
    var selectedTab by remember { mutableStateOf(0) }

    // Estado para mostrar diálogo de permisos (actualmente no usado)
    var showPermissionDialog by remember { mutableStateOf(false) }

    // Manejar permisos de ubicación con Accompanist
    // Permite solicitar y verificar permisos de manera declarativa
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    /**
     * Scaffold - Estructura base de Material Design 3
     * Proporciona slots para TopBar, BottomBar, FAB, Snackbar, etc.
     */
    Scaffold(
        // BARRA SUPERIOR
        topBar = {
            TopAppBar(
                title = { Text("Clima App") },
                actions = {
                    // Botón 1: Obtener clima de la ubicación actual del GPS
                    IconButton(
                        onClick = {
                            // Verificar si tenemos permiso de ubicación
                            if (locationPermissionState.status.isGranted) {
                                // Si tenemos permiso, obtener ubicación
                                scope.launch {
                                    val location = getCurrentLocation(context)
                                    location?.let {
                                        // Obtener clima para las coordenadas actuales
                                        viewModel.getWeatherByCoordinates(
                                            it.latitude,
                                            it.longitude
                                        )
                                    }
                                }
                            } else {
                                // Si no tenemos permiso, solicitarlo
                                locationPermissionState.launchPermissionRequest()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Mi ubicación"
                        )
                    }

                    // Botón 2: Actualizar/Refrescar el clima de la ubicación actual
                    IconButton(
                        onClick = {
                            // Si hay una ubicación seleccionada, actualizar su clima
                            uiState.selectedLocation?.let { location ->
                                viewModel.getWeatherByCoordinates(
                                    location.latitude,
                                    location.longitude
                                )
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Actualizar"
                        )
                    }
                }
            )
        },

        // BARRA INFERIOR DE NAVEGACIÓN
        bottomBar = {
            NavigationBar {
                // Pestaña 1: Pantalla del Clima
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Clima") },
                    label = { Text("Clima") },
                    selected = selectedTab == 0,  // Resaltar si está seleccionada
                    onClick = { selectedTab = 0 }  // Cambiar a pestaña 0
                )

                // Pestaña 2: Pantalla del Mapa
                NavigationBarItem(
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Mapa") },
                    label = { Text("Mapa") },
                    selected = selectedTab == 1,  // Resaltar si está seleccionada
                    onClick = { selectedTab = 1 }  // Cambiar a pestaña 1
                )
            }
        },

        // SNACKBAR PARA MENSAJES
        snackbarHost = {
            // Mostrar Snackbar solo si hay un error/mensaje
            uiState.error?.let { error ->
                Snackbar(
                    action = {
                        // Botón para cerrar el mensaje
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(error)  // Mostrar el mensaje de error
                }
            }
        }
    ) { innerPadding ->
        // CONTENIDO PRINCIPAL - Cambia según la pestaña seleccionada
        when (selectedTab) {
            0 -> {
                // PANTALLA DE CLIMA
                WeatherScreen(
                    // Pasar todos los datos del estado al WeatherScreen
                    weatherData = uiState.weatherData,
                    isLoading = uiState.isLoading,
                    favorites = uiState.favorites,
                    searchQuery = uiState.searchQuery,
                    citySuggestions = uiState.citySuggestions,
                    showSuggestions = uiState.showSuggestions,

                    // CALLBACKS - Funciones que la UI llama cuando el usuario interactúa

                    // Cuando el usuario escribe en el buscador
                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },

                    // Cuando el usuario presiona el botón de buscar
                    onSearch = {
                        if (uiState.searchQuery.isNotBlank()) {
                            viewModel.searchCity(uiState.searchQuery)
                        }
                    },

                    // Cuando el usuario hace clic en una sugerencia
                    onSuggestionClick = { suggestion ->
                        // Buscar directamente la ciudad de la sugerencia
                        viewModel.searchCity(suggestion)
                    },

                    // Cuando el usuario quiere agregar la ciudad actual a favoritos
                    onAddFavorite = {
                        uiState.weatherData?.let { weather ->
                            // Crear objeto FavoriteLocation con los datos actuales
                            val favorite = FavoriteLocation(
                                cityName = weather.name,
                                latitude = weather.coord.lat,
                                longitude = weather.coord.lon,
                                country = weather.sys.country
                            )
                            // Intentar agregarlo a favoritos
                            viewModel.addToFavorites(favorite)
                        }
                    },

                    // Cuando el usuario elimina un favorito
                    onRemoveFavorite = { favorite ->
                        viewModel.removeFromFavorites(favorite)
                    },

                    // Cuando el usuario hace clic en una tarjeta de favorito
                    onFavoriteClick = { favorite ->
                        // Obtener el clima de ese favorito
                        viewModel.getWeatherByCoordinates(
                            favorite.latitude,
                            favorite.longitude
                        )
                    },

                    // Padding para no solapar con barras superior e inferior
                    modifier = Modifier.padding(innerPadding)
                )
            }

            1 -> {
                // PANTALLA DE MAPA
                MapScreen(
                    // Pasar la ubicación actual para mostrar marcador
                    currentLocation = uiState.selectedLocation,

                    // Cuando el usuario selecciona una ubicación en el mapa
                    onLocationSelected = { latLng ->
                        // Obtener clima para esas coordenadas
                        viewModel.selectLocationFromMap(latLng)
                        // Cambiar automáticamente a la pestaña del clima
                        selectedTab = 0
                    },

                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

    /**
     * LaunchedEffect - Efecto que se ejecuta una sola vez al iniciar la app.
     * Solicita permisos de ubicación y obtiene el clima de la ubicación actual.
     */
    LaunchedEffect(Unit) {
        if (!locationPermissionState.status.isGranted) {
            // Si no tenemos permiso, solicitarlo
            locationPermissionState.launchPermissionRequest()
        } else {
            // Si ya tenemos permiso, obtener ubicación actual
            val location = getCurrentLocation(context)
            location?.let {
                // Obtener clima de la ubicación inicial
                viewModel.getWeatherByCoordinates(it.latitude, it.longitude)
            }
        }
    }
}