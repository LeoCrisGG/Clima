package com.example.clima1.utils

import com.example.clima1.BuildConfig

object Constants {
    // Las API Keys se configuran en local.properties
    // y se inyectan automáticamente en BuildConfig
    const val OPENWEATHER_API_KEY = BuildConfig.OPENWEATHER_API_KEY
    const val GOOGLE_MAPS_API_KEY = BuildConfig.GOOGLE_MAPS_API_KEY

    // URLs
    const val WEATHER_ICON_URL = "https://openweathermap.org/img/wn/"
}
