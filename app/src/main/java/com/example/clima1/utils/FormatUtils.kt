package com.example.clima1.utils

import java.text.SimpleDateFormat
import java.util.*

// Funciones de utilidad para formatear datos

fun formatTemperature(temp: Double): String {
    return "${temp.toInt()}°C"
}

fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp * 1000))
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp * 1000))
}

fun formatCoordinates(lat: Double, lon: Double): String {
    return "Lat: ${String.format("%.4f", lat)}, Lon: ${String.format("%.4f", lon)}"
}

fun getWindDirection(degrees: Int): String {
    return when (degrees) {
        in 0..22 -> "N"
        in 23..67 -> "NE"
        in 68..112 -> "E"
        in 113..157 -> "SE"
        in 158..202 -> "S"
        in 203..247 -> "SO"
        in 248..292 -> "O"
        in 293..337 -> "NO"
        else -> "N"
    }
}

fun getWeatherEmoji(weatherMain: String): String {
    return when (weatherMain.lowercase()) {
        "clear" -> "☀️"
        "clouds" -> "☁️"
        "rain" -> "🌧️"
        "drizzle" -> "🌦️"
        "thunderstorm" -> "⛈️"
        "snow" -> "❄️"
        "mist", "fog" -> "🌫️"
        else -> "🌡️"
    }
}

