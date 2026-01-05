# 🔑 Configuración de API Keys - Guía Rápida

## 📍 Ubicación Centralizada

Todas tus API keys se configuran **una sola vez** en el archivo:
```
local.properties
```

## ⚡ Configuración en 3 Pasos

### 1️⃣ Edita `local.properties`

Abre el archivo `local.properties` en la raíz del proyecto y agrega tus API keys al final:

```properties
# Tus API Keys
OPENWEATHER_API_KEY=tu_api_key_de_openweathermap_aqui
GOOGLE_MAPS_API_KEY=tu_api_key_de_google_maps_aqui
```

### 2️⃣ Sincroniza Gradle

En Android Studio:
- Clic en **"Sync Now"** cuando aparezca el banner
- O usa: `File > Sync Project with Gradle Files`

### 3️⃣ ¡Listo!

Tus API keys estarán disponibles en toda la app automáticamente.

---

## 💡 Cómo Usar las API Keys en tu Código

Simplemente importa `Constants`:

```kotlin
import com.example.clima1.utils.Constants

// En cualquier parte de tu código
val weatherApiKey = Constants.OPENWEATHER_API_KEY
val mapsApiKey = Constants.GOOGLE_MAPS_API_KEY
```

---

## 🔒 Seguridad

✅ **Ventajas de este método:**
- `local.properties` no se sube a Git (está en `.gitignore`)
- Las API keys no están hardcodeadas en el código
- Fácil de cambiar sin modificar código
- Ideal para trabajo en equipo (cada desarrollador tiene sus propias keys)

---

## 🆘 Solución de Problemas

### Error: "BuildConfig cannot be resolved"
**Solución:** Sincroniza el proyecto con Gradle:
```
File > Sync Project with Gradle Files
```

### Error: "API Key is empty"
**Solución:** Verifica que `local.properties` tenga las keys correctamente:
```properties
OPENWEATHER_API_KEY=abc123...
GOOGLE_MAPS_API_KEY=AIza...
```
(Sin espacios extra, sin comillas)

### El mapa no se muestra
**Solución:** Verifica que Google Maps API Key esté en `local.properties` y hayas habilitado:
- Maps SDK for Android
- Places API

En: https://console.cloud.google.com/

---

## 📚 Arquitectura del Sistema

```
┌─────────────────────┐
│  local.properties   │ ← AQUÍ defines tus API keys
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│ build.gradle.kts    │ ← Lee y genera BuildConfig
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│   BuildConfig       │ ← Clase generada automáticamente
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│   Constants.kt      │ ← Exporta las constantes
└──────────┬──────────┘
           │
           ▼
┌─────────────────────┐
│   Tu Código (🚀)    │ ← Usa Constants.OPENWEATHER_API_KEY
└─────────────────────┘
```

---

## 🎯 Ejemplo Completo

**1. Configuración en `local.properties`:**
```properties
OPENWEATHER_API_KEY=9a8b7c6d5e4f3g2h1i0j9k8l7m6n5o4p
GOOGLE_MAPS_API_KEY=AIzaSyBqwxyz123456789ABCDEFGHIJK
```

**2. Uso en Retrofit (WeatherApiService.kt):**
```kotlin
import com.example.clima1.utils.Constants

@GET("weather")
suspend fun getWeather(
    @Query("lat") lat: Double,
    @Query("lon") lon: Double,
    @Query("appid") apiKey: String = Constants.OPENWEATHER_API_KEY,
    @Query("units") units: String = "metric",
    @Query("lang") lang: String = "es"
): WeatherResponse
```

**3. Uso en Maps (MapScreen.kt):**
```kotlin
import com.example.clima1.utils.Constants

GoogleMap(
    apiKey = Constants.GOOGLE_MAPS_API_KEY,
    // ... resto de propiedades
)
```

---

## ✨ Beneficios

1. **Un solo lugar:** Cambia las API keys en un solo archivo
2. **Seguro:** No expones las keys en el código fuente
3. **Profesional:** Sigue las mejores prácticas de Android
4. **Flexible:** Fácil de actualizar en cualquier momento

---

**¿Necesitas ayuda?** Revisa `CONFIGURACION_API_KEYS.md` para más detalles.

