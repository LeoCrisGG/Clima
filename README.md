# Aplicación de Clima - Android

Una aplicación completa de clima para Android desarrollada con Jetpack Compose que utiliza las APIs de OpenWeatherMap y Google Maps.

## Características

✅ **Búsqueda de ciudades**: Busca el clima de cualquier ciudad del mundo
✅ **Ubicación actual**: Obtén el clima de tu ubicación actual con GPS
✅ **Mapa interactivo**: Selecciona ubicaciones en un mapa para ver su clima
✅ **Ubicaciones favoritas**: Guarda hasta 5 ubicaciones como favoritas
✅ **Datos completos del clima**:
  - Temperatura actual, mínima y máxima
  - Sensación térmica
  - Humedad y presión atmosférica
  - Velocidad del viento
  - Visibilidad y nubosidad
  - Horas de amanecer y atardecer
  - Coordenadas geográficas

## Configuración

### 1. Obtener las API Keys

#### OpenWeatherMap API Key
1. Visita [OpenWeatherMap](https://openweathermap.org/api)
2. Crea una cuenta gratuita
3. Ve a "API Keys" en tu perfil
4. Copia tu API Key

#### Google Maps API Key
1. Visita [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un nuevo proyecto
3. Habilita las siguientes APIs:
   - Maps SDK for Android
   - Places API
4. Ve a "Credenciales" y crea una API Key
5. Copia tu API Key

### 2. Configurar las API Keys en el proyecto

#### Para OpenWeatherMap:
Abre el archivo `app/src/main/java/com/example/clima1/utils/Constants.kt` y reemplaza:
```kotlin
const val OPENWEATHER_API_KEY = "TU_API_KEY_DE_OPENWEATHERMAP"
```

#### Para Google Maps:
Abre el archivo `app/build.gradle.kts` y reemplaza en la línea 19:
```kotlin
manifestPlaceholders["MAPS_API_KEY"] = "TU_GOOGLE_MAPS_API_KEY"
```

### 3. Sincronizar y compilar

1. Sincroniza el proyecto con Gradle
2. Espera a que se descarguen todas las dependencias
3. Compila y ejecuta la aplicación

## Permisos requeridos

La aplicación solicita los siguientes permisos:
- **INTERNET**: Para obtener datos del clima
- **ACCESS_FINE_LOCATION**: Para obtener tu ubicación precisa
- **ACCESS_COARSE_LOCATION**: Para obtener tu ubicación aproximada

## Tecnologías utilizadas

- **Kotlin**: Lenguaje de programación
- **Jetpack Compose**: UI moderna y declarativa
- **Material Design 3**: Diseño moderno y consistente
- **Retrofit**: Cliente HTTP para las APIs REST
- **Room**: Base de datos local para favoritos
- **Coroutines & Flow**: Programación asíncrona
- **Google Maps Compose**: Integración de mapas
- **Coil**: Carga de imágenes
- **MVVM Architecture**: Arquitectura limpia y mantenible

## Estructura del proyecto

```
app/src/main/java/com/example/clima1/
├── data/
│   ├── local/          # Room Database
│   ├── model/          # Modelos de datos
│   ├── remote/         # API Services
│   └── repository/     # Repositorio de datos
├── ui/
│   ├── screens/        # Pantallas de la app
│   ├── theme/          # Tema y estilos
│   └── viewmodel/      # ViewModels
└── utils/              # Utilidades y constantes
```

## Uso de la aplicación

### Pantalla Principal (Clima)
- **Buscador**: Escribe el nombre de una ciudad y presiona el botón de enviar
- **Ubicación actual**: Presiona el icono de ubicación en la barra superior
- **Actualizar**: Presiona el icono de actualizar para refrescar los datos
- **Favoritos**: Presiona la estrella para agregar la ubicación actual a favoritos
- **Lista de favoritos**: Toca una tarjeta de favorito para ver su clima

### Pantalla de Mapa
- **Seleccionar ubicación**: Toca cualquier punto del mapa
- **Ver clima**: Presiona el botón "Obtener clima de esta ubicación"
- La app cambiará automáticamente a la pantalla de clima con los datos

## Limitaciones

- Máximo 5 ubicaciones favoritas
- La API gratuita de OpenWeatherMap tiene un límite de 60 llamadas por minuto
- Se requiere conexión a Internet para obtener datos del clima

## Notas

- Los datos del clima se muestran en español
- Las temperaturas se muestran en grados Celsius
- La velocidad del viento se muestra en metros por segundo

## Soporte

Si encuentras algún problema, verifica:
1. Que las API Keys estén correctamente configuradas
2. Que tengas conexión a Internet
3. Que los permisos de ubicación estén otorgados
4. Que las APIs estén habilitadas en tus consolas de desarrollo

