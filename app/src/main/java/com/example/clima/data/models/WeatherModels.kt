package com.example.clima.data.models

import com.google.gson.annotations.SerializedName

// Respuesta principal del clima actual
data class WeatherResponse(
    @SerializedName("coord") val coord: Coord,
    @SerializedName("weather") val weather: List<Weather>,
    @SerializedName("base") val base: String,
    @SerializedName("main") val main: Main,
    @SerializedName("visibility") val visibility: Int,
    @SerializedName("wind") val wind: Wind,
    @SerializedName("clouds") val clouds: Clouds,
    @SerializedName("dt") val dt: Long,
    @SerializedName("sys") val sys: Sys,
    @SerializedName("timezone") val timezone: Int,
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("cod") val cod: Int
)

data class Coord(
    @SerializedName("lon") val lon: Double,
    @SerializedName("lat") val lat: Double
)

data class Weather(
    @SerializedName("id") val id: Int,
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

data class Main(
    @SerializedName("temp") val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double,
    @SerializedName("pressure") val pressure: Int,
    @SerializedName("humidity") val humidity: Int,
    @SerializedName("sea_level") val seaLevel: Int? = null,
    @SerializedName("grnd_level") val grndLevel: Int? = null
)

data class Wind(
    @SerializedName("speed") val speed: Double,
    @SerializedName("deg") val deg: Int,
    @SerializedName("gust") val gust: Double? = null
)

data class Clouds(
    @SerializedName("all") val all: Int
)

data class Sys(
    @SerializedName("type") val type: Int? = null,
    @SerializedName("id") val id: Int? = null,
    @SerializedName("country") val country: String,
    @SerializedName("sunrise") val sunrise: Long,
    @SerializedName("sunset") val sunset: Long
)

// Pronóstico de 5 días / 3 horas
data class ForecastResponse(
    @SerializedName("cod") val cod: String,
    @SerializedName("message") val message: Int,
    @SerializedName("cnt") val cnt: Int,
    @SerializedName("list") val list: List<ForecastItem>,
    @SerializedName("city") val city: City
)

data class ForecastItem(
    @SerializedName("dt") val dt: Long,
    @SerializedName("main") val main: Main,
    @SerializedName("weather") val weather: List<Weather>,
    @SerializedName("clouds") val clouds: Clouds,
    @SerializedName("wind") val wind: Wind,
    @SerializedName("visibility") val visibility: Int,
    @SerializedName("pop") val pop: Double, // Probabilidad de precipitación
    @SerializedName("sys") val sys: ForecastSys,
    @SerializedName("dt_txt") val dtTxt: String
)

data class ForecastSys(
    @SerializedName("pod") val pod: String // Parte del día: d = día, n = noche
)

data class City(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("coord") val coord: Coord,
    @SerializedName("country") val country: String,
    @SerializedName("population") val population: Int,
    @SerializedName("timezone") val timezone: Int,
    @SerializedName("sunrise") val sunrise: Long,
    @SerializedName("sunset") val sunset: Long
)

// Calidad del aire
data class AirQualityResponse(
    @SerializedName("coord") val coord: Coord,
    @SerializedName("list") val list: List<AirQualityData>
)

data class AirQualityData(
    @SerializedName("main") val main: AirQualityMain,
    @SerializedName("components") val components: AirComponents,
    @SerializedName("dt") val dt: Long
)

data class AirQualityMain(
    @SerializedName("aqi") val aqi: Int // 1 = Bueno, 2 = Regular, 3 = Moderado, 4 = Malo, 5 = Muy malo
)

data class AirComponents(
    @SerializedName("co") val co: Double,
    @SerializedName("no") val no: Double,
    @SerializedName("no2") val no2: Double,
    @SerializedName("o3") val o3: Double,
    @SerializedName("so2") val so2: Double,
    @SerializedName("pm2_5") val pm2_5: Double,
    @SerializedName("pm10") val pm10: Double,
    @SerializedName("nh3") val nh3: Double
)

