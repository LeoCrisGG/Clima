package com.example.clima.data.api

import com.example.clima.data.models.AirQualityResponse
import com.example.clima.data.models.ForecastResponse
import com.example.clima.data.models.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    // Clima actual por coordenadas
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "es"
    ): WeatherResponse

    // Clima actual por nombre de ciudad
    @GET("weather")
    suspend fun getCurrentWeatherByCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "es"
    ): WeatherResponse

    // Pronóstico de 5 días
    @GET("forecast")
    suspend fun getForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "es"
    ): ForecastResponse

    // Calidad del aire
    @GET("air_pollution")
    suspend fun getAirQuality(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String
    ): AirQualityResponse
}

// Nueva interfaz separada para Geocoding API
interface GeocodingApiService {
    // Geocoding API - Buscar ciudades
    @GET("direct")
    suspend fun searchCities(
        @Query("q") query: String,
        @Query("limit") limit: Int = 5,
        @Query("appid") apiKey: String
    ): List<CitySearchResult>

    // Geocoding Reverso - Obtener ciudad desde coordenadas
    @GET("reverse")
    suspend fun reverseCitySearch(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("limit") limit: Int = 1,
        @Query("appid") apiKey: String
    ): List<CitySearchResult>
}

data class CitySearchResult(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String? = null,
    val local_names: Map<String, String>? = null
)
