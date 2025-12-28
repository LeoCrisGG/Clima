package com.example.clima.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clima.data.models.*
import com.example.clima.viewmodel.WeatherUiState
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    uiState: WeatherUiState,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchCity: (String) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit = {},
    onRequestLocationRefresh: () -> Unit = {}
) {
    var showSearchBar by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Manejar el refresh
    LaunchedEffect(uiState) {
        if (uiState !is WeatherUiState.Loading) {
            isRefreshing = false
        }
    }

    Scaffold(
        topBar = {
            if (showSearchBar) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchQueryChange,
                    onSearch = {
                        if (searchQuery.isNotBlank()) {
                            onSearchCity(searchQuery)
                            showSearchBar = false
                        }
                    },
                    onDismiss = { showSearchBar = false }
                )
            } else {
                TopAppBar(
                    title = { },
                    actions = {
                        IconButton(onClick = { showSearchBar = true }) {
                            Icon(
                                Icons.Default.Search,
                                "Buscar ciudad",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = onRequestLocationRefresh) {
                            Icon(
                                Icons.Default.LocationOn,
                                "Actualizar ubicación",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        }
    ) { padding ->
        when (uiState) {
            is WeatherUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Cargando datos del clima...",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
            is WeatherUiState.Error -> {
                ErrorScreen(
                    message = uiState.message,
                    onRetry = onRetry,
                    modifier = Modifier.padding(padding)
                )
            }
            is WeatherUiState.Success -> {
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        isRefreshing = true
                        onRefresh()
                    },
                    modifier = Modifier.padding(padding)
                ) {
                    WeatherContent(
                        weather = uiState.currentWeather,
                        forecast = uiState.forecast,
                        airQuality = uiState.airQuality
                    )
                }
            }
        }
    }
}

@Composable
fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Error",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reintentar")
        }
    }
}

@Composable
fun WeatherContent(
    weather: WeatherResponse,
    forecast: ForecastResponse?,
    airQuality: AirQualityResponse?,
    modifier: Modifier = Modifier
) {
    val backgroundColor = getBackgroundGradient(weather.weather.firstOrNull()?.main ?: "Clear")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(brush = backgroundColor)
            .padding(16.dp)
    ) {
        // Ubicación y temperatura principal
        item {
            CurrentWeatherHeader(weather)
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Pronóstico por horas
        item {
            if (forecast != null) {
                HourlyForecastSection(forecast)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Pronóstico de 7 días
        item {
            if (forecast != null) {
                DailyForecastSection(forecast)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Detalles del clima
        item {
            WeatherDetailsGrid(weather)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Calidad del aire
        item {
            if (airQuality != null) {
                AirQualityCard(airQuality)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Amanecer y atardecer
        item {
            SunriseSunsetCard(weather.sys)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CurrentWeatherHeader(weather: WeatherResponse) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = weather.name,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = weather.sys.country,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(32.dp))

        // Icono del clima
        Text(
            text = getWeatherIcon(weather.weather.firstOrNull()?.main ?: "Clear"),
            fontSize = 120.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Temperatura
        Text(
            text = "${weather.main.temp.roundToInt()}°",
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // Descripción
        Text(
            text = weather.weather.firstOrNull()?.description?.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            } ?: "",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White.copy(alpha = 0.9f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Máxima y mínima
        Text(
            text = "Máx: ${weather.main.tempMax.roundToInt()}° | Mín: ${weather.main.tempMin.roundToInt()}°",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun HourlyForecastSection(forecast: ForecastResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⏰",
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pronóstico por horas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(forecast.list.take(8)) { item ->
                    HourlyForecastItem(item)
                }
            }
        }
    }
}

@Composable
fun HourlyForecastItem(item: ForecastItem) {
    val time = SimpleDateFormat("HH:mm", Locale.getDefault())
        .format(Date(item.dt * 1000))

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = time,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = getWeatherIcon(item.weather.firstOrNull()?.main ?: "Clear"),
            fontSize = 32.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${item.main.temp.roundToInt()}°",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        if (item.pop > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "💧",
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${(item.pop * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF64B5F6)
                )
            }
        }
    }
}

@Composable
fun DailyForecastSection(forecast: ForecastResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pronóstico de 5 días",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Agrupar por día
            val dailyForecasts = forecast.list.groupBy { item ->
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Date(item.dt * 1000))
            }.values.take(5)

            dailyForecasts.forEachIndexed { index, dayItems ->
                DailyForecastItem(dayItems, index == 0)
                if (dayItems != dailyForecasts.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun DailyForecastItem(dayItems: List<ForecastItem>, isToday: Boolean = false) {
    val date = Date(dayItems.first().dt * 1000)
    val dayName = if (isToday) {
        "Hoy"
    } else {
        SimpleDateFormat("EEEE", Locale.getDefault()).format(date)
    }
    val maxTemp = dayItems.maxOf { it.main.tempMax }
    val minTemp = dayItems.minOf { it.main.tempMin }
    val mainWeather = dayItems[dayItems.size / 2].weather.firstOrNull()?.main ?: "Clear"
    val maxPop = dayItems.maxOf { it.pop }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = dayName.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            },
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )

        if (maxPop > 0) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(
                    text = "💧",
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${(maxPop * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64B5F6)
                )
            }
        }

        Text(
            text = getWeatherIcon(mainWeather),
            fontSize = 28.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Text(
            text = "${maxTemp.roundToInt()}°/${minTemp.roundToInt()}°",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
fun WeatherDetailsGrid(weather: WeatherResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WeatherDetailItem(
                    icon = "🌡️",
                    label = "Sensación",
                    value = "${weather.main.feelsLike.roundToInt()}°",
                    modifier = Modifier.weight(1f)
                )
                WeatherDetailItem(
                    icon = "💧",
                    label = "Humedad",
                    value = "${weather.main.humidity}%",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WeatherDetailItem(
                    icon = "💨",
                    label = "Viento",
                    value = "${weather.wind.speed} m/s",
                    modifier = Modifier.weight(1f)
                )
                WeatherDetailItem(
                    icon = "🔽",
                    label = "Presión",
                    value = "${weather.main.pressure} hPa",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WeatherDetailItem(
                    icon = "👁️",
                    label = "Visibilidad",
                    value = "${weather.visibility / 1000} km",
                    modifier = Modifier.weight(1f)
                )
                WeatherDetailItem(
                    icon = "☁️",
                    label = "Nubosidad",
                    value = "${weather.clouds.all}%",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun WeatherDetailItem(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun AirQualityCard(airQuality: AirQualityResponse) {
    val aqi = airQuality.list.firstOrNull()?.main?.aqi ?: 1
    val (aqiText, aqiColor) = getAirQualityInfo(aqi)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🌫️",
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Calidad del Aire",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(aqiColor)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = aqiText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val components = airQuality.list.firstOrNull()?.components
            if (components != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AirComponentItem("PM2.5", components.pm2_5.roundToInt().toString(), Modifier.weight(1f))
                    AirComponentItem("PM10", components.pm10.roundToInt().toString(), Modifier.weight(1f))
                    AirComponentItem("O₃", components.o3.roundToInt().toString(), Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun AirComponentItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun SunriseSunsetCard(sys: Sys) {
    val sunrise = SimpleDateFormat("HH:mm", Locale.getDefault())
        .format(Date(sys.sunrise * 1000))
    val sunset = SimpleDateFormat("HH:mm", Locale.getDefault())
        .format(Date(sys.sunset * 1000))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🌅",
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Amanecer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = sunrise,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🌇",
                    fontSize = 32.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Atardecer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = sunset,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onDismiss: () -> Unit
) {
    TopAppBar(
        title = {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Buscar ciudad...") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        navigationIcon = {
            IconButton(onClick = onDismiss) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
            }
        },
        actions = {
            IconButton(onClick = onSearch) {
                Icon(Icons.Default.Search, "Buscar")
            }
        }
    )
}

// Funciones auxiliares
fun getWeatherIcon(condition: String): String {
    return when (condition) {
        "Clear" -> "☀️"
        "Clouds" -> "☁️"
        "Rain" -> "🌧️"
        "Drizzle" -> "🌦️"
        "Thunderstorm" -> "⛈️"
        "Snow" -> "❄️"
        "Mist", "Fog", "Haze" -> "🌫️"
        else -> "🌤️"
    }
}

fun getBackgroundGradient(condition: String): Brush {
    return when (condition) {
        "Clear" -> Brush.verticalGradient(
            colors = listOf(Color(0xFF4A90E2), Color(0xFF50C9FF))
        )
        "Clouds" -> Brush.verticalGradient(
            colors = listOf(Color(0xFF607D8B), Color(0xFF90A4AE))
        )
        "Rain", "Drizzle" -> Brush.verticalGradient(
            colors = listOf(Color(0xFF546E7A), Color(0xFF78909C))
        )
        "Thunderstorm" -> Brush.verticalGradient(
            colors = listOf(Color(0xFF37474F), Color(0xFF546E7A))
        )
        "Snow" -> Brush.verticalGradient(
            colors = listOf(Color(0xFFB0BEC5), Color(0xFFCFD8DC))
        )
        else -> Brush.verticalGradient(
            colors = listOf(Color(0xFF4A90E2), Color(0xFF50C9FF))
        )
    }
}

fun getAirQualityInfo(aqi: Int): Pair<String, Color> {
    return when (aqi) {
        1 -> "Bueno" to Color(0xFF4CAF50)
        2 -> "Regular" to Color(0xFF8BC34A)
        3 -> "Moderado" to Color(0xFFFFC107)
        4 -> "Malo" to Color(0xFFFF9800)
        5 -> "Muy Malo" to Color(0xFFF44336)
        else -> "Desconocido" to Color.Gray
    }
}
