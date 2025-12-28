# Aplicación del Clima ☀️🌧️

Una aplicación completa del clima para Android con Jetpack Compose que muestra información meteorológica detallada.

## 🌟 Características

- **Clima Actual**: Temperatura, sensación térmica, descripción del clima
- **Pronóstico por Horas**: Próximas 24 horas con temperatura y probabilidad de lluvia
- **Pronóstico de 7 Días**: Temperaturas máximas/mínimas y condiciones
- **Detalles Meteorológicos**:
  - Humedad
  - Velocidad del viento
  - Presión atmosférica
  - Visibilidad
  - Nubosidad
- **Calidad del Aire**: Índice AQI con componentes PM2.5, PM10, y O₃
- **Amanecer y Atardecer**: Horarios exactos
- **Ubicación Automática**: Detecta tu ubicación actual
- **Búsqueda de Ciudades**: Busca el clima de cualquier ciudad del mundo
- **Interfaz Dinámica**: El fondo cambia según las condiciones climáticas

## 🔧 Configuración

### 1. Obtener una API Key de OpenWeatherMap (GRATIS)

1. Ve a [OpenWeatherMap](https://openweathermap.org/api)
2. Haz clic en "Sign Up" para crear una cuenta gratuita
3. Confirma tu email
4. Ve a "API Keys" en tu perfil
5. Copia tu API key (o genera una nueva)

### 2. Configurar la API Key en el Proyecto

Abre el archivo:
```
app/src/main/java/com/example/clima/data/repository/WeatherRepository.kt
```

Y reemplaza `"TU_API_KEY_AQUI"` con tu API key real:

```kotlin
private val apiKey = "t063c4f035798c46d38a39a9c231f4b11"
```

### 3. Sincronizar el Proyecto

En Android Studio:
1. Haz clic en "File" → "Sync Project with Gradle Files"
2. Espera a que se descarguen todas las dependencias

### 4. Ejecutar la Aplicación

1. Conecta un dispositivo Android o inicia un emulador
2. Haz clic en el botón "Run" (▶️)
3. La app solicitará permisos de ubicación
4. ¡Disfruta de tu app del clima!

## 📱 Permisos Requeridos

- **Internet**: Para obtener datos del clima
- **Ubicación (Fina y Aproximada)**: Para detectar tu ubicación actual

## 🛠️ Tecnologías Utilizadas

- **Kotlin**: Lenguaje de programación
- **Jetpack Compose**: UI moderna y declarativa
- **Retrofit**: Cliente HTTP para APIs REST
- **OpenWeatherMap API**: Datos meteorológicos
- **Location Services**: Ubicación del dispositivo
- **Coroutines & Flow**: Programación asíncrona
- **ViewModel**: Arquitectura MVVM

## 📚 API de OpenWeatherMap

La aplicación utiliza los siguientes endpoints de OpenWeatherMap:

- **Current Weather Data**: Clima actual
- **5 Day / 3 Hour Forecast**: Pronóstico extendido
- **Air Pollution API**: Calidad del aire

La cuenta gratuita incluye:
- ✅ 60 llamadas por minuto
- ✅ 1,000,000 llamadas por mes
- ✅ Todos los datos meteorológicos básicos

## 🎨 Características de UI

- Gradientes de fondo dinámicos según el clima
- Emojis visuales para condiciones climáticas
- Cards semitransparentes con efecto glassmorphism
- Animaciones suaves
- Diseño responsive
- Material Design 3

## 🌍 Ubicación por Defecto

Si no se conceden permisos de ubicación, la app usa Ciudad de México como ubicación predeterminada.

## 📝 Notas

- La API key gratuita puede tardar unos minutos en activarse después del registro
- Asegúrate de tener conexión a Internet
- La precisión de la ubicación depende del GPS del dispositivo

## 🐛 Solución de Problemas

**Error "Invalid API Key"**:
- Verifica que copiaste correctamente la API key
- Espera unos minutos si acabas de crear la cuenta
- Revisa que tu API key esté activa en OpenWeatherMap

**No se detecta la ubicación**:
- Verifica que los permisos de ubicación estén concedidos
- Asegúrate de tener el GPS activado
- Prueba buscar una ciudad manualmente

**Errores de compilación**:
- Sincroniza el proyecto con Gradle
- Limpia el proyecto: Build → Clean Project
- Rebuild: Build → Rebuild Project

## 📄 Licencia

Este proyecto es de código abierto y está disponible para fines educativos.

