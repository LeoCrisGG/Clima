package com.example.clima.data.repository

import com.example.clima.data.api.CitySearchResult
import com.example.clima.data.api.RetrofitClient
import com.example.clima.data.models.AirQualityResponse
import com.example.clima.data.models.ForecastResponse
import com.example.clima.data.models.WeatherResponse

class WeatherRepository {

    private val apiService = RetrofitClient.weatherApiService
    private val geocodingService = RetrofitClient.geocodingApiService

    // Nota: Reemplaza esta API key con tu propia key de OpenWeatherMap
    // Obtén una gratis en: https://openweathermap.org/api
    private val apiKey = "2e67bf6a615df96c15c9e1533cc73189"

    suspend fun getCurrentWeather(lat: Double, lon: Double): Result<WeatherResponse> {
        return try {
            val response = apiService.getCurrentWeather(lat, lon, apiKey)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentWeatherByCity(city: String): Result<WeatherResponse> {
        return try {
            val response = apiService.getCurrentWeatherByCity(city, apiKey)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getForecast(lat: Double, lon: Double): Result<ForecastResponse> {
        return try {
            val response = apiService.getForecast(lat, lon, apiKey)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAirQuality(lat: Double, lon: Double): Result<AirQualityResponse> {
        return try {
            val response = apiService.getAirQuality(lat, lon, apiKey)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchCities(query: String): Result<List<CitySearchResult>> {
        return try {
            if (query.length < 2) {
                return Result.success(emptyList())
            }
            val response = geocodingService.searchCities(query, 5, apiKey)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Nuevo método para obtener el nombre de la ciudad desde coordenadas
    suspend fun getCityNameFromCoordinates(lat: Double, lon: Double): Result<String> {
        return try {
            val response = geocodingService.reverseCitySearch(lat, lon, 1, apiKey)
            if (response.isNotEmpty()) {
                val city = response.first()
                // Retornar el nombre de la ciudad con el país
                Result.success("${city.name}, ${city.country}")
            } else {
                Result.success("Ubicación desconocida")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
