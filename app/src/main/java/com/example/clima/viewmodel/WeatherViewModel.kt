package com.example.clima.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clima.data.api.CitySearchResult
import com.example.clima.data.models.AirQualityResponse
import com.example.clima.data.models.ForecastResponse
import com.example.clima.data.models.WeatherResponse
import com.example.clima.data.repository.WeatherRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(
        val currentWeather: WeatherResponse,
        val forecast: ForecastResponse?,
        val airQuality: AirQualityResponse?,
        val lastUpdated: Long = System.currentTimeMillis()
    ) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

class WeatherViewModel : ViewModel() {

    private val repository = WeatherRepository()

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _citySuggestions = MutableStateFlow<List<CitySearchResult>>(emptyList())
    val citySuggestions: StateFlow<List<CitySearchResult>> = _citySuggestions.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var searchJob: Job? = null
    private var autoRefreshJob: Job? = null

    private var currentLat: Double = 0.0
    private var currentLon: Double = 0.0

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query

        // Cancelar búsqueda anterior
        searchJob?.cancel()

        if (query.length < 2) {
            _citySuggestions.value = emptyList()
            return
        }

        // Debounce: esperar 500ms antes de buscar
        searchJob = viewModelScope.launch {
            delay(500)
            searchCities(query)
        }
    }

    private fun searchCities(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            val result = repository.searchCities(query)
            if (result.isSuccess) {
                _citySuggestions.value = result.getOrNull() ?: emptyList()
            } else {
                _citySuggestions.value = emptyList()
            }
            _isSearching.value = false
        }
    }

    fun selectCity(city: CitySearchResult) {
        _searchQuery.value = ""
        _citySuggestions.value = emptyList()
        loadWeatherData(city.lat, city.lon)
    }

    fun clearSuggestions() {
        _citySuggestions.value = emptyList()
        _searchQuery.value = ""
    }

    fun loadWeatherData(lat: Double, lon: Double) {
        currentLat = lat
        currentLon = lon

        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading

            // Obtener el nombre de la ciudad desde las coordenadas GPS
            val cityNameResult = repository.getCityNameFromCoordinates(lat, lon)
            val cityName = cityNameResult.getOrNull() ?: "Ubicación desconocida"

            val weatherResult = repository.getCurrentWeather(lat, lon)

            if (weatherResult.isSuccess) {
                var weather = weatherResult.getOrNull()!!

                // Reemplazar el nombre que viene de la API con el nombre correcto de la ciudad
                weather = weather.copy(name = cityName.substringBefore(","))

                // Cargar pronóstico y calidad del aire en paralelo
                val forecastResult = repository.getForecast(lat, lon)
                val airQualityResult = repository.getAirQuality(lat, lon)

                _uiState.value = WeatherUiState.Success(
                    currentWeather = weather,
                    forecast = forecastResult.getOrNull(),
                    airQuality = airQualityResult.getOrNull(),
                    lastUpdated = System.currentTimeMillis()
                )

                // Iniciar auto-refresh cada 10 minutos
                startAutoRefresh()
            } else {
                _uiState.value = WeatherUiState.Error(
                    weatherResult.exceptionOrNull()?.message ?: "Error desconocido"
                )
            }
        }
    }

    fun searchCity(city: String) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading

            val weatherResult = repository.getCurrentWeatherByCity(city)

            if (weatherResult.isSuccess) {
                val weather = weatherResult.getOrNull()!!
                val lat = weather.coord.lat
                val lon = weather.coord.lon

                currentLat = lat
                currentLon = lon

                val forecastResult = repository.getForecast(lat, lon)
                val airQualityResult = repository.getAirQuality(lat, lon)

                _uiState.value = WeatherUiState.Success(
                    currentWeather = weather,
                    forecast = forecastResult.getOrNull(),
                    airQuality = airQualityResult.getOrNull(),
                    lastUpdated = System.currentTimeMillis()
                )

                startAutoRefresh()
            } else {
                _uiState.value = WeatherUiState.Error(
                    "No se pudo encontrar la ciudad"
                )
            }
        }
    }

    fun retry(lat: Double, lon: Double) {
        loadWeatherData(lat, lon)
    }

    private fun startAutoRefresh() {
        // Cancelar refresh anterior si existe
        autoRefreshJob?.cancel()

        // Auto-refresh cada 10 minutos
        autoRefreshJob = viewModelScope.launch {
            while (true) {
                delay(10 * 60 * 1000) // 10 minutos
                if (currentLat != 0.0 && currentLon != 0.0) {
                    refreshWeatherData()
                }
            }
        }
    }

    private suspend fun refreshWeatherData() {
        if (currentLat == 0.0 && currentLon == 0.0) return

        // Obtener el nombre de la ciudad desde las coordenadas GPS
        val cityNameResult = repository.getCityNameFromCoordinates(currentLat, currentLon)
        val cityName = cityNameResult.getOrNull() ?: "Ubicación desconocida"

        val weatherResult = repository.getCurrentWeather(currentLat, currentLon)

        if (weatherResult.isSuccess) {
            var weather = weatherResult.getOrNull()!!

            // Reemplazar el nombre que viene de la API con el nombre correcto de la ciudad
            weather = weather.copy(name = cityName.substringBefore(","))

            val forecastResult = repository.getForecast(currentLat, currentLon)
            val airQualityResult = repository.getAirQuality(currentLat, currentLon)

            _uiState.value = WeatherUiState.Success(
                currentWeather = weather,
                forecast = forecastResult.getOrNull(),
                airQuality = airQualityResult.getOrNull(),
                lastUpdated = System.currentTimeMillis()
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoRefreshJob?.cancel()
        searchJob?.cancel()
    }
}
