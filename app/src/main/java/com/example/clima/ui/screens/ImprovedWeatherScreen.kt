package com.example.clima.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.clima.data.api.CitySearchResult
import com.example.clima.data.models.*
import com.example.clima.viewmodel.WeatherUiState
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImprovedWeatherScreen(
    uiState: WeatherUiState,
    searchQuery: String,
    citySuggestions: List<CitySearchResult>,
    isSearching: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onSelectCity: (CitySearchResult) -> Unit,
    onClearSuggestions: () -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit = {},
    onRequestLocationRefresh: () -> Unit = {}
) {
    var showSearchDialog by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState !is WeatherUiState.Loading) {
            isRefreshing = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (uiState) {
            is WeatherUiState.Loading -> {
                LoadingScreen()
            }
            is WeatherUiState.Error -> {
                ModernErrorScreen(
                    message = uiState.message,
                    onRetry = onRetry
                )
            }
            is WeatherUiState.Success -> {
                ModernWeatherContent(
                    weather = uiState.currentWeather,
                    forecast = uiState.forecast,
                    airQuality = uiState.airQuality,
                    lastUpdated = uiState.lastUpdated,
                    onSearchClick = { showSearchDialog = true },
                    onLocationClick = onRequestLocationRefresh,
                    onRefresh = {
                        isRefreshing = true
                        onRefresh()
                    },
                    isRefreshing = isRefreshing
                )
            }
        }

        // Diálogo de búsqueda con sugerencias
        if (showSearchDialog) {
            ModernSearchDialog(
                query = searchQuery,
                suggestions = citySuggestions,
                isSearching = isSearching,
                onQueryChange = onSearchQueryChange,
                onSelectCity = { city ->
                    onSelectCity(city)
                    showSearchDialog = false
                },
                onDismiss = {
                    showSearchDialog = false
                    onClearSuggestions()
                }
            )
        }
    }
}

@Composable
fun LoadingScreen() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF4A90E2), Color(0xFF50C9FF))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "☀️",
                fontSize = 80.sp,
                modifier = Modifier.scale(scale)
            )
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Obteniendo datos del clima...",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ModernErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF546E7A), Color(0xFF78909C))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "⚠️", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Oops!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A90E2)
                    )
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reintentar")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernWeatherContent(
    weather: WeatherResponse,
    forecast: ForecastResponse?,
    airQuality: AirQualityResponse?,
    lastUpdated: Long,
    onSearchClick: () -> Unit,
    onLocationClick: () -> Unit,
    onRefresh: () -> Unit,
    isRefreshing: Boolean
) {
    val backgroundColor = getModernBackgroundGradient(weather.weather.firstOrNull()?.main ?: "Clear")
    val timeAgo = getTimeAgo(lastUpdated)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = backgroundColor)
        ) {
            // Header con botones
            item {
                ModernTopBar(
                    onSearchClick = onSearchClick,
                    onLocationClick = onLocationClick,
                    timeAgo = timeAgo
                )
            }

            // Temperatura principal con animación
            item {
                AnimatedTemperatureHeader(weather)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Pronóstico por horas con diseño moderno
            item {
                if (forecast != null) {
                    ModernHourlyForecast(forecast)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Pronóstico de 5 días
            item {
                if (forecast != null) {
                    ModernDailyForecast(forecast)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Grid de detalles con glassmorphism
            item {
                GlassmorphicDetailsGrid(weather)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Calidad del aire moderna
            item {
                if (airQuality != null) {
                    ModernAirQualityCard(airQuality)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Sol con diseño atractivo
            item {
                ModernSunCard(weather.sys)
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Pull to refresh indicator
        if (isRefreshing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 80.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun ModernTopBar(
    onSearchClick: () -> Unit,
    onLocationClick: () -> Unit,
    timeAgo: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Clima Actual",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = timeAgo,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FloatingActionButton(
                onClick = onSearchClick,
                modifier = Modifier.size(48.dp),
                containerColor = Color.White.copy(alpha = 0.3f),
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = Color.White
                )
            }

            FloatingActionButton(
                onClick = onLocationClick,
                modifier = Modifier.size(48.dp),
                containerColor = Color.White.copy(alpha = 0.3f),
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Mi ubicación",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun AnimatedTemperatureHeader(weather: WeatherResponse) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ubicación
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${weather.name}, ${weather.sys.country}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Icono del clima grande y animado
            val scale by rememberInfiniteTransition(label = "icon").animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Text(
                text = getWeatherIcon(weather.weather.firstOrNull()?.main ?: "Clear"),
                fontSize = 140.sp,
                modifier = Modifier.scale(scale)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Temperatura con efecto de sombra
            Box {
                Text(
                    text = "${weather.main.temp.roundToInt()}°",
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.offset(x = 4.dp, y = 4.dp)
                )
                Text(
                    text = "${weather.main.temp.roundToInt()}°",
                    fontSize = 96.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Descripción
            Text(
                text = weather.weather.firstOrNull()?.description?.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                } ?: "",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White.copy(alpha = 0.95f),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Min/Max en chip
            Surface(
                color = Color.White.copy(alpha = 0.25f),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "↑", fontSize = 20.sp, color = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${weather.main.tempMax.roundToInt()}°",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(text = "|", color = Color.White.copy(alpha = 0.5f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "↓", fontSize = 20.sp, color = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${weather.main.tempMin.roundToInt()}°",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernHourlyForecast(forecast: ForecastResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.25f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Pronóstico por Horas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(forecast.list.take(12)) { item ->
                    ModernHourlyItem(item)
                }
            }
        }
    }
}

@Composable
fun ModernHourlyItem(item: ForecastItem) {
    val time = SimpleDateFormat("HH:mm", Locale.getDefault())
        .format(Date(item.dt * 1000))
    val isNow = abs(System.currentTimeMillis() - item.dt * 1000) < 3 * 60 * 60 * 1000

    Surface(
        color = if (isNow) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.2f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(80.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isNow) "Ahora" else time,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = if (isNow) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = getWeatherIcon(item.weather.firstOrNull()?.main ?: "Clear"),
                fontSize = 36.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${item.main.temp.roundToInt()}°",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (item.pop > 0.1) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "💧", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${(item.pop * 100).roundToInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64B5F6),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ModernDailyForecast(forecast: ForecastResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.25f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Próximos 5 Días",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            val dailyForecasts = forecast.list.groupBy { item ->
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(Date(item.dt * 1000))
            }.values.take(5)

            dailyForecasts.forEachIndexed { index, dayItems ->
                ModernDailyItem(dayItems, index == 0)
                if (index < dailyForecasts.size - 1) {
                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ModernDailyItem(dayItems: List<ForecastItem>, isToday: Boolean) {
    val date = Date(dayItems.first().dt * 1000)
    val dayName = if (isToday) "Hoy" else SimpleDateFormat("EEEE", Locale.getDefault()).format(date)
    val maxTemp = dayItems.maxOf { it.main.tempMax }
    val minTemp = dayItems.minOf { it.main.tempMin }
    val mainWeather = dayItems[dayItems.size / 2].weather.firstOrNull()?.main ?: "Clear"
    val maxPop = dayItems.maxOf { it.pop }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = dayName.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            },
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )

        if (maxPop > 0.1) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                Text(text = "💧", fontSize = 18.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${(maxPop * 100).roundToInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF64B5F6),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Text(
            text = getWeatherIcon(mainWeather),
            fontSize = 32.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${maxTemp.roundToInt()}°",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "${minTemp.roundToInt()}°",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun GlassmorphicDetailsGrid(weather: WeatherResponse) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassDetailCard(
                icon = "🌡️",
                label = "Sensación Térmica",
                value = "${weather.main.feelsLike.roundToInt()}°",
                modifier = Modifier.weight(1f)
            )
            GlassDetailCard(
                icon = "💧",
                label = "Humedad",
                value = "${weather.main.humidity}%",
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassDetailCard(
                icon = "💨",
                label = "Viento",
                value = "${weather.wind.speed} m/s",
                modifier = Modifier.weight(1f)
            )
            GlassDetailCard(
                icon = "👁️",
                label = "Visibilidad",
                value = "${weather.visibility / 1000} km",
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassDetailCard(
                icon = "🔽",
                label = "Presión",
                value = "${weather.main.pressure} hPa",
                modifier = Modifier.weight(1f)
            )
            GlassDetailCard(
                icon = "☁️",
                label = "Nubosidad",
                value = "${weather.clouds.all}%",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun GlassDetailCard(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.25f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = icon, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun ModernAirQualityCard(airQuality: AirQualityResponse) {
    val aqi = airQuality.list.firstOrNull()?.main?.aqi ?: 1
    val (aqiText, aqiColor) = getAirQualityInfo(aqi)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.25f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🌫️", fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Calidad del Aire",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "AQI Index",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Surface(
                    color = aqiColor,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = aqiText,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val components = airQuality.list.firstOrNull()?.components
            if (components != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AirComponentBox("PM2.5", components.pm2_5.roundToInt().toString(), Modifier.weight(1f))
                    AirComponentBox("PM10", components.pm10.roundToInt().toString(), Modifier.weight(1f))
                    AirComponentBox("O₃", components.o3.roundToInt().toString(), Modifier.weight(1f))
                    AirComponentBox("NO₂", components.no2.roundToInt().toString(), Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun AirComponentBox(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.2f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
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
}

@Composable
fun ModernSunCard(sys: Sys) {
    val sunrise = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(sys.sunrise * 1000))
    val sunset = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(sys.sunset * 1000))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.25f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = Color(0xFFFFA726).copy(alpha = 0.3f),
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🌅", fontSize = 32.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Amanecer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = sunrise,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            HorizontalDivider(
                color = Color.White.copy(alpha = 0.3f),
                modifier = Modifier
                    .height(100.dp)
                    .width(1.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = Color(0xFFFF7043).copy(alpha = 0.3f),
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🌇", fontSize = 32.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Atardecer",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = sunset,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernSearchDialog(
    query: String,
    suggestions: List<CitySearchResult>,
    isSearching: Boolean,
    onQueryChange: (String) -> Unit,
    onSelectCity: (CitySearchResult) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Buscar Ciudad",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Escribe el nombre de la ciudad...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else if (query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4A90E2),
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sugerencias
                if (suggestions.isNotEmpty()) {
                    Text(
                        text = "Sugerencias:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(suggestions) { city ->
                            SuggestionItem(city, onSelectCity)
                        }
                    }
                } else if (query.length >= 2 && !isSearching) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🔍", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No se encontraron ciudades",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SuggestionItem(
    city: CitySearchResult,
    onSelect: (CitySearchResult) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(city) },
        color = Color.Gray.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color(0xFF4A90E2).copy(alpha = 0.2f),
                shape = CircleShape,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF4A90E2),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = city.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = buildString {
                        city.state?.let { append("$it, ") }
                        append(city.country)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

// Funciones auxiliares
fun getModernBackgroundGradient(condition: String): Brush {
    return when (condition) {
        "Clear" -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF4A90E2),
                Color(0xFF50B5FF),
                Color(0xFF63C5FF)
            )
        )
        "Clouds" -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF546E7A),
                Color(0xFF607D8B),
                Color(0xFF78909C)
            )
        )
        "Rain", "Drizzle" -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF455A64),
                Color(0xFF546E7A),
                Color(0xFF607D8B)
            )
        )
        "Thunderstorm" -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF263238),
                Color(0xFF37474F),
                Color(0xFF455A64)
            )
        )
        "Snow" -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF90A4AE),
                Color(0xFFB0BEC5),
                Color(0xFFCFD8DC)
            )
        )
        "Mist", "Fog", "Haze" -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF78909C),
                Color(0xFF90A4AE),
                Color(0xFFB0BEC5)
            )
        )
        else -> Brush.verticalGradient(
            colors = listOf(
                Color(0xFF4A90E2),
                Color(0xFF50B5FF),
                Color(0xFF63C5FF)
            )
        )
    }
}

fun getTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val minutes = diff / (60 * 1000)
    return when {
        minutes < 1 -> "Actualizado ahora"
        minutes < 60 -> "Actualizado hace $minutes min"
        else -> {
            val hours = minutes / 60
            "Actualizado hace $hours h"
        }
    }
}

private fun abs(l: Long): Long = if (l < 0) -l else l
