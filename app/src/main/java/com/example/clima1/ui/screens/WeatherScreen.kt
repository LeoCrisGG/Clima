package com.example.clima1.ui.screens

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.clima1.data.model.FavoriteLocation
import com.example.clima1.data.model.WeatherResponse
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    weatherData: WeatherResponse?,
    isLoading: Boolean,
    favorites: List<FavoriteLocation>,
    searchQuery: String,
    citySuggestions: List<String> = emptyList(),
    showSuggestions: Boolean = false,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSuggestionClick: (String) -> Unit = {},
    onAddFavorite: () -> Unit,
    onRemoveFavorite: (FavoriteLocation) -> Unit,
    onFavoriteClick: (FavoriteLocation) -> Unit,
    modifier: Modifier = Modifier
) {
    // Sincronizar el campo de búsqueda con el estado del ViewModel
    LaunchedEffect(searchQuery) {
        // Este efecto se ejecuta cuando searchQuery cambia desde el ViewModel
        // Permite resetear el campo cuando se completa una búsqueda
    }

    // Animación de escala para el ícono del clima
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Search Bar con diseño mejorado y sugerencias
            item {
                SearchBarWithSuggestions(
                    searchText = searchQuery,
                    suggestions = citySuggestions,
                    showSuggestions = showSuggestions,
                    onSearchTextChange = {
                        onSearchQueryChange(it)
                    },
                    onSearch = {
                        if (searchQuery.isNotBlank()) {
                            onSearch()
                        }
                    },
                    onSuggestionClick = { suggestion ->
                        onSuggestionClick(suggestion)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Favorites Section con diseño mejorado
            if (favorites.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⭐ Favoritos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${favorites.size}/5",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(favorites) { favorite ->
                            FavoriteCard(
                                favorite = favorite,
                                onClick = { onFavoriteClick(favorite) },
                                onDelete = { onRemoveFavorite(favorite) }
                            )
                        }
                    }
                }
            }

            // Loading State con animación
            if (isLoading) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(48.dp),
                                    strokeWidth = 4.dp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Cargando datos del clima...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Weather Data con diseño mejorado
            weatherData?.let { weather ->
                item {
                    AnimatedWeatherInfoCard(
                        weather = weather,
                        onAddFavorite = onAddFavorite,
                        scale = scale
                    )
                }

                item {
                    EnhancedWeatherDetailsCard(weather = weather)
                }

                item {
                    EnhancedAdditionalInfoCard(weather = weather)
                }
            }

            // Mensaje cuando no hay datos
            if (!isLoading && weatherData == null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Busca una ciudad o permite el acceso a tu ubicación",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun FavoriteCard(
    favorite: FavoriteLocation,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .width(140.dp)
            .clickable {
                isPressed = true
                onClick()
            },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 2.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = favorite.cityName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 2
                    )
                    Text(
                        text = favorite.country,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Eliminar",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
fun AnimatedWeatherInfoCard(
    weather: WeatherResponse,
    onAddFavorite: () -> Unit,
    scale: Float,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        weather.main.temp > 30 -> listOf(Color(0xFFFF6B6B), Color(0xFFFFE66D))
        weather.main.temp > 20 -> listOf(Color(0xFF4ECDC4), Color(0xFF44A08D))
        weather.main.temp > 10 -> listOf(Color(0xFF5F9EA0), Color(0xFF2E8B57))
        else -> listOf(Color(0xFF4A5568), Color(0xFF2D3748))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(backgroundColor)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = weather.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = weather.sys.country,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    FilledIconButton(
                        onClick = onAddFavorite,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.White.copy(alpha = 0.3f)
                        )
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Agregar a favoritos",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Weather icon con animación
                val iconUrl = "https://openweathermap.org/img/wn/${weather.weather.firstOrNull()?.icon}@4x.png"
                AsyncImage(
                    model = iconUrl,
                    contentDescription = "Icono del clima",
                    modifier = Modifier
                        .size(140.dp)
                        .scale(scale)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Temperature con estilo grande
                Text(
                    text = "${weather.main.temp.toInt()}°",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )

                // Description
                Text(
                    text = weather.weather.firstOrNull()?.description?.capitalize() ?: "",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Feels like en un chip
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.3f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sensación térmica: ${weather.main.feelsLike.toInt()}°C",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedWeatherDetailsCard(
    weather: WeatherResponse,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Temperaturas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EnhancedWeatherDetailItem(
                    icon = Icons.Default.KeyboardArrowUp,
                    label = "Máxima",
                    value = "${weather.main.tempMax.toInt()}°C",
                    color = Color(0xFFFF6B6B)
                )
                Divider(
                    modifier = Modifier
                        .height(80.dp)
                        .width(1.dp)
                )
                EnhancedWeatherDetailItem(
                    icon = Icons.Default.KeyboardArrowDown,
                    label = "Mínima",
                    value = "${weather.main.tempMin.toInt()}°C",
                    color = Color(0xFF4ECDC4)
                )
            }
        }
    }
}

@Composable
fun EnhancedAdditionalInfoCard(
    weather: WeatherResponse,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Detalles",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Grid de información
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoCard(
                        icon = "💧",
                        label = "Humedad",
                        value = "${weather.main.humidity}%",
                        modifier = Modifier.weight(1f)
                    )
                    InfoCard(
                        icon = "🌪️",
                        label = "Viento",
                        value = "${weather.wind.speed} m/s",
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InfoCard(
                        icon = "📊",
                        label = "Presión",
                        value = "${weather.main.pressure} hPa",
                        modifier = Modifier.weight(1f)
                    )
                    InfoCard(
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
                    InfoCard(
                        icon = "☁️",
                        label = "Nubosidad",
                        value = "${weather.clouds.all}%",
                        modifier = Modifier.weight(1f)
                    )
                    InfoCard(
                        icon = "📍",
                        label = "Coordenadas",
                        value = "${String.format("%.2f", weather.coord.lat)}, ${String.format("%.2f", weather.coord.lon)}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Divider()

            Spacer(modifier = Modifier.height(16.dp))

            // Sunrise y Sunset
            val sunrise = SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(Date(weather.sys.sunrise * 1000))
            val sunset = SimpleDateFormat("HH:mm", Locale.getDefault())
                .format(Date(weather.sys.sunset * 1000))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SunTimeItem(
                    icon = "🌅",
                    label = "Amanecer",
                    time = sunrise
                )
                SunTimeItem(
                    icon = "🌇",
                    label = "Atardecer",
                    time = sunset
                )
            }
        }
    }
}

@Composable
fun EnhancedWeatherDetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.2f),
            modifier = Modifier.size(60.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun InfoCard(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SunTimeItem(
    icon: String,
    label: String,
    time: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.displaySmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = time,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}

@Composable
fun SearchBarWithSuggestions(
    searchText: String,
    suggestions: List<String>,
    showSuggestions: Boolean,
    onSearchTextChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = onSearchTextChange,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Buscar ciudad...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Buscar")
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = onSearch,
                        enabled = searchText.isNotBlank(),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "Buscar",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                // Mostrar sugerencias
                if (showSuggestions && suggestions.isNotEmpty()) {
                    Divider(modifier = Modifier.padding(horizontal = 12.dp))
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        suggestions.forEach { suggestion ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSuggestionClick(suggestion)
                                    },
                                color = Color.Transparent
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = suggestion,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
