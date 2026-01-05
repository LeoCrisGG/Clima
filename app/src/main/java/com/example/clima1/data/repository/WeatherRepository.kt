package com.example.clima1.data.repository

import com.example.clima1.data.local.FavoriteLocationDao
import com.example.clima1.data.model.FavoriteLocation
import com.example.clima1.data.model.WeatherResponse
import com.example.clima1.data.remote.WeatherApiService
import com.example.clima1.utils.Constants
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio que maneja todas las operaciones relacionadas con datos del clima y favoritos.
 * Actúa como intermediario entre la API de clima, la base de datos local y el ViewModel.
 *
 * @param weatherApi Servicio de la API de OpenWeatherMap para obtener datos del clima
 * @param favoriteLocationDao DAO para operaciones de base de datos de ubicaciones favoritas
 */
class WeatherRepository(
    private val weatherApi: WeatherApiService,
    private val favoriteLocationDao: FavoriteLocationDao
) {

    // Obtener la clave de API de OpenWeatherMap desde las constantes
    private val apiKey = Constants.OPENWEATHER_API_KEY

    /**
     * Diccionario de mapeo de nombres de ciudades.
     * Se usa para reemplazar nombres de ciudades que la API devuelve con nombres preferidos.
     * Ejemplo: Si la API devuelve "Monteros", se mostrará "Castilla" al usuario.
     */
    private val cityNameMapping = mapOf(
        "Monteros" to "Castilla",
        "monteros" to "Castilla"
    )

    /**
     * Función privada que mapea el nombre de una ciudad según el diccionario cityNameMapping.
     * Si el nombre está en el diccionario, devuelve el valor mapeado; si no, devuelve el nombre original.
     *
     * @param apiCityName Nombre de la ciudad devuelto por la API
     * @return Nombre mapeado o el nombre original si no hay mapeo
     */
    private fun mapCityName(apiCityName: String): String {
        return cityNameMapping[apiCityName] ?: apiCityName
    }

    /**
     * Obtiene los datos del clima para unas coordenadas geográficas específicas.
     * Realiza una llamada a la API de OpenWeatherMap y mapea el nombre de la ciudad.
     *
     * @param lat Latitud de la ubicación
     * @param lon Longitud de la ubicación
     * @return Result con WeatherResponse si es exitoso, o una excepción si falla
     */
    suspend fun getWeatherByCoordinates(lat: Double, lon: Double): Result<WeatherResponse> {
        return try {
            // Llamar a la API con las coordenadas y la API key
            val response = weatherApi.getCurrentWeather(lat, lon, apiKey)

            // Mapear el nombre de la ciudad si es necesario (ej: Monteros -> Castilla)
            val mappedResponse = response.copy(name = mapCityName(response.name))

            // Devolver el resultado exitoso con los datos mapeados
            Result.success(mappedResponse)
        } catch (e: Exception) {
            // Si hay error (sin internet, API caída, etc.), devolver el error
            Result.failure(e)
        }
    }

    /**
     * Obtiene los datos del clima para una ciudad específica por su nombre.
     * Realiza una llamada a la API de OpenWeatherMap y mapea el nombre de la ciudad.
     *
     * @param cityName Nombre de la ciudad a buscar
     * @return Result con WeatherResponse si es exitoso, o una excepción si falla
     */
    suspend fun getWeatherByCity(cityName: String): Result<WeatherResponse> {
        return try {
            // Llamar a la API con el nombre de la ciudad y la API key
            val response = weatherApi.getCurrentWeatherByCity(cityName, apiKey)

            // Mapear el nombre de la ciudad si es necesario
            val mappedResponse = response.copy(name = mapCityName(response.name))

            // Devolver el resultado exitoso
            Result.success(mappedResponse)
        } catch (e: Exception) {
            // Si hay error (ciudad no encontrada, sin internet, etc.), devolver el error
            Result.failure(e)
        }
    }

    /**
     * Busca ciudades que coincidan con la consulta del usuario.
     * Retorna una lista de sugerencias de ciudades basadas en una lista predefinida.
     * No hace llamadas a la API, funciona offline con datos locales.
     *
     * @param query Texto de búsqueda ingresado por el usuario
     * @return Result con lista de nombres de ciudades que coinciden, o error si falla
     */
    suspend fun searchCities(query: String): Result<List<String>> {
        return try {
            // Si la búsqueda tiene menos de 2 caracteres, no mostrar sugerencias
            if (query.length < 2) {
                return Result.success(emptyList())
            }

            // Obtener sugerencias de ciudades que coincidan con la búsqueda
            val suggestions = getCitySuggestions(query)
            Result.success(suggestions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Función privada que filtra ciudades de una lista predefinida basándose en la búsqueda.
     * Lista de más de 100 ciudades populares de España y Latinoamérica.
     *
     * @param query Texto de búsqueda del usuario
     * @return Lista de hasta 5 ciudades que coinciden con la búsqueda
     */
    private fun getCitySuggestions(query: String): List<String> {
        // Lista ampliada de ciudades comunes en español para sugerencias
        val cities = listOf(
            // España - Ciudades principales
            "Madrid", "Barcelona", "Valencia", "Sevilla", "Zaragoza",
            "Málaga", "Murcia", "Palma", "Las Palmas", "Bilbao",
            "Alicante", "Córdoba", "Valladolid", "Vigo", "Gijón",
            "Hospitalet", "Granada", "Vitoria", "Elche", "Oviedo",
            "Cartagena", "Jerez", "Sabadell", "Móstoles", "Almería",
            "Pamplona", "Castellón", "Burgos", "Santander", "San Sebastián",
            "Salamanca", "Albacete", "Logroño", "León", "Badajoz",
            "Cádiz", "Huelva", "Tarragona", "Lleida", "Marbella",
            "Castilla", "Toledo", "Ávila", "Segovia", "Cuenca",

            // Argentina
            "Buenos Aires", "Córdoba", "Rosario", "Mendoza", "La Plata",
            "San Miguel de Tucumán", "Mar del Plata", "Salta", "Santa Fe", "San Juan",

            // México
            "Mexico City", "Guadalajara", "Monterrey", "Puebla", "Tijuana",
            "León", "Juárez", "Zapopan", "Mérida", "Mexicali",

            // Perú
            "Lima", "Arequipa", "Trujillo", "Cusco", "Chiclayo",
            "Piura", "Iquitos", "Huancayo", "Tacna", "Pucallpa",

            // Colombia
            "Bogotá", "Medellín", "Cali", "Barranquilla", "Cartagena",
            "Cúcuta", "Bucaramanga", "Pereira", "Santa Marta", "Ibagué",

            // Chile
            "Santiago", "Valparaíso", "Concepción", "Viña del Mar", "La Serena",
            "Antofagasta", "Temuco", "Rancagua", "Talca", "Puerto Montt",

            // Otros países latinoamericanos
            "Quito", "Guayaquil", "Cuenca",
            "Caracas", "Maracaibo", "Valencia",
            "La Paz", "Santa Cruz", "Cochabamba",
            "Montevideo", "Asunción",
            "San José", "Panama City", "Guatemala City",
            "San Salvador", "Tegucigalpa", "Managua",
            "San Juan", "Santo Domingo", "La Habana"
        )

        // Convertir la búsqueda a minúsculas para comparación case-insensitive
        val lowerQuery = query.lowercase().trim()

        return cities
            // Filtrar ciudades que contengan el texto de búsqueda
            .filter { it.lowercase().contains(lowerQuery) }
            // Ordenar: las que empiezan con la búsqueda van primero
            .sortedBy {
                if (it.lowercase().startsWith(lowerQuery)) 0 else 1
            }
            // Tomar solo las primeras 5 coincidencias
            .take(5)
    }

    // ===== OPERACIONES DE FAVORITOS =====

    /**
     * Obtiene todas las ubicaciones favoritas guardadas en la base de datos.
     * Retorna un Flow que emite automáticamente cuando hay cambios en los favoritos.
     *
     * @return Flow que emite lista de ubicaciones favoritas
     */
    fun getAllFavorites(): Flow<List<FavoriteLocation>> {
        return favoriteLocationDao.getAllFavorites()
    }

    /**
     * Agrega una nueva ubicación a la lista de favoritos.
     * Valida que no se excedan los 5 favoritos máximos y que no existan duplicados.
     *
     * @param location Ubicación favorita a agregar
     * @return Result.success si se agregó correctamente, Result.failure con error si falla
     */
    suspend fun addFavorite(location: FavoriteLocation): Result<Unit> {
        return try {
            // Obtener el número actual de favoritos
            val count = favoriteLocationDao.getFavoritesCount()

            // Validar que no se excedan los 5 favoritos
            if (count >= 5) {
                Result.failure(Exception("Máximo 5 ubicaciones favoritas alcanzado"))
            } else {
                // Verificar si ya existe un favorito con el mismo nombre de ciudad
                val existing = favoriteLocationDao.getFavoriteByCity(location.cityName)

                if (existing != null) {
                    // Si ya existe, devolver error
                    Result.failure(Exception("Esta ciudad ya está en favoritos"))
                } else {
                    // Si todo está bien, insertar el favorito en la base de datos
                    favoriteLocationDao.insertFavorite(location)
                    Result.success(Unit)
                }
            }
        } catch (e: Exception) {
            // Capturar cualquier error de base de datos
            Result.failure(e)
        }
    }

    /**
     * Elimina una ubicación favorita de la base de datos.
     *
     * @param location Ubicación favorita a eliminar
     */
    suspend fun deleteFavorite(location: FavoriteLocation) {
        favoriteLocationDao.deleteFavorite(location)
    }

    /**
     * Busca un favorito específico por nombre de ciudad.
     *
     * @param cityName Nombre de la ciudad a buscar
     * @return FavoriteLocation si existe, null si no existe
     */
    suspend fun getFavoriteByCity(cityName: String): FavoriteLocation? {
        return favoriteLocationDao.getFavoriteByCity(cityName)
    }

    /**
     * Verifica si una ciudad ya está guardada en favoritos.
     *
     * @param cityName Nombre de la ciudad a verificar
     * @return true si existe en favoritos, false si no
     */
    suspend fun isFavorite(cityName: String): Boolean {
        return favoriteLocationDao.getFavoriteByCity(cityName) != null
    }
}
