# Configuración Rápida - API Keys

## ⚠️ IMPORTANTE: Debes configurar las API Keys antes de ejecutar la aplicación

### ✨ Configuración Simplificada (Solo un archivo)

**Paso Único: Configurar `local.properties`**

1. Abre el archivo: `local.properties` (en la raíz del proyecto)
2. Encuentra estas líneas al final del archivo:
   ```properties
   OPENWEATHER_API_KEY=TU_API_KEY_DE_OPENWEATHERMAP
   GOOGLE_MAPS_API_KEY=TU_API_KEY_DE_GOOGLE_MAPS
   ```
3. Reemplaza los valores con tus API keys reales

**Ejemplo:**
```properties
OPENWEATHER_API_KEY=abc123def456ghi789jkl012mno345
GOOGLE_MAPS_API_KEY=AIzaSyD1234567890abcdefghijklmnopqrstu
```

4. **¡Listo!** Las API keys se usarán automáticamente en toda la app

### 🔒 Seguridad

- ✅ El archivo `local.properties` NO se sube a Git
- ✅ Las API keys están centralizadas en un solo lugar
- ✅ Se inyectan automáticamente en BuildConfig
- ✅ Se usan a través de `Constants.kt` en todo el código

### Paso 2: Sincronizar Gradle

1. En Android Studio, haz clic en "Sync Now" cuando aparezca el mensaje
2. O ve a: `File > Sync Project with Gradle Files`

### Paso 3: Ejecutar la aplicación

1. Conecta tu dispositivo Android o inicia un emulador
2. Haz clic en el botón "Run" (▶️)
3. Acepta los permisos de ubicación cuando la app lo solicite

---

## 📝 Cómo obtener las API Keys (si no las tienes)

### OpenWeatherMap:
- URL: https://openweathermap.org/api
- Plan recomendado: Free (suficiente para desarrollo)
- Límite: 60 llamadas/minuto, 1,000,000 llamadas/mes

### Google Maps:
- URL: https://console.cloud.google.com/
- APIs a habilitar:
  - Maps SDK for Android
  - Places API
- Plan: $200 USD de crédito gratis cada mes

---

## ✅ Verificación

Después de configurar las API Keys, la aplicación debe:
- ✅ Mostrar el clima de tu ubicación actual al iniciar
- ✅ Permitir buscar ciudades
- ✅ Mostrar un mapa interactivo
- ✅ Guardar hasta 5 ubicaciones favoritas

Si algo no funciona, revisa que las API Keys estén correctamente escritas (sin espacios extra).

---

## 🔧 Detalles Técnicos

El sistema funciona así:
1. **`local.properties`**: Aquí defines tus API keys (una sola vez)
2. **`build.gradle.kts`**: Lee las keys y las inyecta en BuildConfig
3. **`Constants.kt`**: Exporta las keys para usarlas en toda la app
4. **Tu código**: Importa desde `Constants.kt` para usar las API keys
