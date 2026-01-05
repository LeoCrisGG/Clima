package com.example.clima1.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.clima1.data.local.WeatherDatabase
import com.example.clima1.data.model.FavoriteLocation
import com.example.clima1.data.model.WeatherResponse
import com.example.clima1.data.remote.RetrofitClient
import com.example.clima1.data.repository.WeatherRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Clase de datos que representa el estado de la interfaz de usuario.
 * Contiene todos los datos que la UI necesita mostrar.
 *
 * @property isLoading Indica si hay una operación en curso (para mostrar loading spinner)
 * @property weatherData Datos del clima actual (null si no hay datos cargados)
 * @property error Mensaje de error para mostrar al usuario (null si no hay error)
 * @property favorites Lista de ubicaciones favoritas guardadas
 * @property selectedLocation Ubicación actualmente seleccionada en el mapa
 * @property searchQuery Texto actual en el campo de búsqueda
 * @property citySuggestions Lista de sugerencias de ciudades para mostrar
 * @property showSuggestions Flag para controlar si mostrar el dropdown de sugerencias
 */
data class WeatherUiState(
    val isLoading: Boolean = false,
    val weatherData: WeatherResponse? = null,
    val error: String? = null,
    val favorites: List<FavoriteLocation> = emptyList(),
    val selectedLocation: LatLng? = null,
    val searchQuery: String = "",
    val citySuggestions: List<String> = emptyList(),
    val showSuggestions: Boolean = false
)

/**
 * ViewModel que maneja la lógica de negocio y el estado de la pantalla del clima.
 * Hereda de AndroidViewModel para tener acceso al contexto de la aplicación.
 *
 * Responsabilidades:
 * - Obtener datos del clima del repositorio
 * - Manejar la búsqueda de ciudades y sugerencias
 * - Gestionar favoritos (agregar/eliminar)
 * - Mantener el estado de la UI actualizado
 * - Ejecutar operaciones asíncronas en coroutines
 */
class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    // Instancia del repositorio para acceder a datos
    private val repository: WeatherRepository

    // StateFlow mutable privado - solo el ViewModel puede modificarlo
    private val _uiState = MutableStateFlow(WeatherUiState())

    // StateFlow público inmutable - la UI solo puede observarlo, no modificarlo
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    /**
     * Bloque init - se ejecuta cuando se crea el ViewModel.
     * Inicializa la base de datos y el repositorio, y carga los favoritos.
     */
    init {
        // Obtener la instancia de la base de datos
        val database = WeatherDatabase.getDatabase(application)

        // Crear el repositorio con la API y el DAO de favoritos
        repository = WeatherRepository(
            RetrofitClient.weatherApi,
            database.favoriteLocationDao()
        )

        // Cargar los favoritos al iniciar
        loadFavorites()
    }

    /**
     * Función privada que observa los cambios en la base de datos de favoritos.
     * Cada vez que hay un cambio, actualiza el estado de la UI automáticamente.
     */
    private fun loadFavorites() {
        viewModelScope.launch {
            // getAllFavorites() retorna un Flow que emite cada vez que cambia la BD
            repository.getAllFavorites().collect { favorites ->
                // Actualizar el estado con la nueva lista de favoritos
                _uiState.update { it.copy(favorites = favorites) }
            }
        }
    }

    /**
     * Obtiene los datos del clima para unas coordenadas geográficas específicas.
     * Usado cuando el usuario selecciona una ubicación en el mapa o usa su GPS.
     *
     * @param lat Latitud de la ubicación
     * @param lon Longitud de la ubicación
     */
    fun getWeatherByCoordinates(lat: Double, lon: Double) {
        // Lanzar una coroutine en el scope del ViewModel
        viewModelScope.launch {
            // Activar el indicador de carga y limpiar errores previos
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Llamar al repositorio para obtener los datos
            val result = repository.getWeatherByCoordinates(lat, lon)

            // Manejar el resultado (exitoso o fallido)
            result.fold(
                onSuccess = { weather ->
                    // Si es exitoso, actualizar el estado con los datos del clima
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            weatherData = weather,
                            selectedLocation = LatLng(lat, lon)
                        )
                    }
                },
                onFailure = { exception ->
                    // Si falla, actualizar el estado con el mensaje de error
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Error desconocido"
                        )
                    }
                }
            )
        }
    }

    /**
     * Busca una ciudad por nombre y obtiene sus datos del clima.
     * Al completar la búsqueda, resetea el campo de búsqueda y las sugerencias.
     *
     * @param cityName Nombre de la ciudad a buscar
     */
    fun searchCity(cityName: String) {
        // Validar que el nombre no esté vacío
        if (cityName.isBlank()) return

        viewModelScope.launch {
            // Activar loading y ocultar sugerencias
            _uiState.update { it.copy(isLoading = true, error = null, showSuggestions = false) }

            // Llamar al repositorio para buscar la ciudad
            val result = repository.getWeatherByCity(cityName)

            result.fold(
                onSuccess = { weather ->
                    // Si es exitoso, actualizar con los datos y RESETEAR el campo de búsqueda
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            weatherData = weather,
                            selectedLocation = LatLng(weather.coord.lat, weather.coord.lon),
                            searchQuery = "",  // RESETEAR: Limpiar el campo de búsqueda
                            citySuggestions = emptyList(),  // Limpiar sugerencias
                            showSuggestions = false  // Ocultar dropdown
                        )
                    }
                },
                onFailure = { exception ->
                    // Si falla, mostrar error
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = exception.message ?: "Ciudad no encontrada"
                        )
                    }
                }
            )
        }
    }

    /**
     * Agrega una ubicación a la lista de favoritos.
     * Valida que no existan duplicados antes de agregar.
     *
     * @param location Ubicación a agregar como favorita
     */
    fun addToFavorites(location: FavoriteLocation) {
        viewModelScope.launch {
            // Primero verificar si ya existe en favoritos
            val isFav = repository.isFavorite(location.cityName)

            if (isFav) {
                // Si ya existe, mostrar mensaje de advertencia
                _uiState.update {
                    it.copy(error = "⚠️ Esta ciudad ya está en tus favoritos")
                }
                return@launch
            }

            // Intentar agregar el favorito
            val result = repository.addFavorite(location)

            result.fold(
                onSuccess = {
                    // Si se agregó correctamente, mostrar mensaje de éxito
                    _uiState.update {
                        it.copy(error = "✅ Agregado a favoritos")
                    }
                },
                onFailure = { exception ->
                    // Si falla (ej: ya hay 5 favoritos), mostrar el error
                    _uiState.update {
                        it.copy(error = exception.message ?: "Error al agregar favorito")
                    }
                }
            )
        }
    }

    /**
     * Elimina una ubicación de la lista de favoritos.
     *
     * @param location Ubicación a eliminar de favoritos
     */
    fun removeFromFavorites(location: FavoriteLocation) {
        viewModelScope.launch {
            repository.deleteFavorite(location)
            // No es necesario actualizar el estado manualmente,
            // el Flow de getAllFavorites() lo hace automáticamente
        }
    }

    /**
     * Actualiza el texto del campo de búsqueda y obtiene sugerencias en tiempo real.
     * Se ejecuta cada vez que el usuario escribe un carácter.
     *
     * @param query Texto actual del campo de búsqueda
     */
    fun updateSearchQuery(query: String) {
        // Actualizar el query en el estado
        _uiState.update { it.copy(searchQuery = query) }

        // Obtener sugerencias solo si hay al menos 2 caracteres
        if (query.length >= 2) {
            viewModelScope.launch {
                // Llamar al repositorio para obtener sugerencias
                val result = repository.searchCities(query)

                result.fold(
                    onSuccess = { suggestions ->
                        // Si hay sugerencias, mostrarlas
                        _uiState.update {
                            it.copy(
                                citySuggestions = suggestions,
                                showSuggestions = suggestions.isNotEmpty()
                            )
                        }
                    },
                    onFailure = {
                        // Si falla, no mostrar sugerencias
                        _uiState.update {
                            it.copy(
                                citySuggestions = emptyList(),
                                showSuggestions = false
                            )
                        }
                    }
                )
            }
        } else {
            // Si hay menos de 2 caracteres, limpiar sugerencias
            _uiState.update {
                it.copy(
                    citySuggestions = emptyList(),
                    showSuggestions = false
                )
            }
        }
    }

    /**
     * Oculta el dropdown de sugerencias.
     * Usado cuando el usuario hace clic fuera del campo de búsqueda.
     */
    fun hideSuggestions() {
        _uiState.update { it.copy(showSuggestions = false) }
    }

    /**
     * Limpia el mensaje de error actual.
     * Usado cuando el usuario cierra el Snackbar de error.
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Maneja la selección de una ubicación en el mapa.
     * Obtiene el clima para las coordenadas seleccionadas.
     *
     * @param latLng Coordenadas seleccionadas en el mapa
     */
    fun selectLocationFromMap(latLng: LatLng) {
        getWeatherByCoordinates(latLng.latitude, latLng.longitude)
    }
}
