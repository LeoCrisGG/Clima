package com.example.clima1.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

/**
 * MapScreen - Pantalla interactiva con Google Maps para seleccionar ubicaciones.
 *
 * Esta pantalla permite al usuario:
 * 1. Ver un mapa de Google Maps
 * 2. Tocar cualquier punto del mapa para seleccionar una ubicación
 * 3. Ver marcadores de ubicaciones (actual y seleccionada)
 * 4. Obtener el clima de la ubicación seleccionada
 *
 * Componentes visuales:
 * - Card superior: Instrucciones de uso
 * - GoogleMap: Mapa interactivo con marcadores
 * - Card inferior: Botón para ver clima (aparece solo cuando hay selección)
 *
 * @param currentLocation Ubicación actual obtenida del GPS o de una búsqueda previa.
 *                        Se usa para centrar el mapa y mostrar un marcador.
 *                        Si es null, usa Ciudad de México como ubicación por defecto.
 *
 * @param onLocationSelected Callback que se ejecuta cuando el usuario presiona
 *                           "Ver clima de esta ubicación". Recibe las coordenadas
 *                           seleccionadas (LatLng) y típicamente cambia a la
 *                           pantalla del clima.
 *
 * @param modifier Modificador opcional para personalizar el layout
 */
@Composable
fun MapScreen(
    currentLocation: LatLng?,
    onLocationSelected: (LatLng) -> Unit,
    modifier: Modifier = Modifier
) {
    // ===== CONFIGURACIÓN INICIAL =====

    /**
     * Ubicación por defecto: Ciudad de México (19.4326°N, 99.1332°W)
     * Se usa cuando currentLocation es null (ej: primera vez que abres la app
     * sin haber dado permisos de ubicación).
     *
     * Operador Elvis (?: ): Si currentLocation es null, usa defaultLocation
     */
    val defaultLocation = currentLocation ?: LatLng(19.4326, -99.1332)

    /**
     * rememberCameraPositionState - Estado que controla la posición y zoom de la cámara del mapa.
     *
     * remember: Persiste el estado durante recomposiciones (cuando la UI se redibuja)
     * CameraPosition.fromLatLngZoom: Crea una posición de cámara con:
     *   - Coordenadas: defaultLocation
     *   - Zoom: 12f (escala 1-21, donde 1 es mundo completo y 21 es nivel de calle)
     *
     * Este estado permite:
     * - Centrar el mapa en una ubicación específica
     * - Animar movimientos de la cámara
     * - Leer la posición actual del usuario en el mapa
     */
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    /**
     * selectedPosition - Guarda las coordenadas del punto que el usuario tocó en el mapa.
     *
     * mutableStateOf: Estado que cuando cambia, Compose redibuja la UI automáticamente
     * remember: Mantiene el valor entre recomposiciones
     * by: Delegación de propiedades, permite usar "selectedPosition = ..." en lugar de "selectedPosition.value = ..."
     *
     * Inicializa con currentLocation para que si ya hay una ubicación cargada,
     * también aparezca como seleccionada en el mapa.
     */
    var selectedPosition by remember { mutableStateOf<LatLng?>(currentLocation) }

    /**
     * LaunchedEffect - Efecto secundario que se ejecuta cuando cambia currentLocation.
     *
     * ¿Cuándo se ejecuta?
     * - Al cargar la pantalla por primera vez
     * - Cada vez que currentLocation cambia (ej: usuario busca otra ciudad)
     *
     * ¿Por qué es necesario?
     * Cuando el usuario busca una ciudad en WeatherScreen y luego va al mapa,
     * necesitamos:
     * 1. Actualizar selectedPosition para que aparezca el marcador
     * 2. Mover la cámara del mapa a esa ubicación
     *
     * Ejemplo de flujo:
     * 1. Usuario busca "Madrid" → currentLocation = Madrid
     * 2. Usuario va a la pestaña Mapa → LaunchedEffect detecta el cambio
     * 3. El mapa se centra en Madrid y aparece el marcador
     */
    LaunchedEffect(currentLocation) {
        currentLocation?.let { location ->
            selectedPosition = location
            // Animar la cámara suavemente a la nueva ubicación
            cameraPositionState.position = CameraPosition.fromLatLngZoom(location, 12f)
        }
    }

    // ===== INTERFAZ DE USUARIO =====

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ----- CARD DE INSTRUCCIONES (SUPERIOR) -----
            /**
             * Card informativa que explica al usuario cómo usar el mapa.
             * Siempre visible en la parte superior.
             */
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Ícono de ubicación
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))

                    // Textos instructivos
                    Column {
                        Text(
                            text = "🗺️ Selecciona una ubicación",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Toca en el mapa para elegir dónde ver el clima",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // ----- MAPA DE GOOGLE MAPS -----
            /**
             * GoogleMap - Componente interactivo de Google Maps Compose.
             *
             * Configuración:
             *
             * modifier:
             *   - fillMaxWidth(): Ocupa todo el ancho disponible
             *   - weight(1f): Ocupa todo el espacio vertical restante (después del card superior
             *                 y antes del card inferior). Esto hace que el mapa se adapte al
             *                 tamaño de pantalla.
             *
             * cameraPositionState:
             *   - Vincula el estado de la cámara creado arriba
             *   - Permite controlar dónde mira el usuario en el mapa
             *
             * onMapClick:
             *   - Callback que se ejecuta cuando el usuario toca el mapa
             *   - Recibe las coordenadas (LatLng) del punto tocado
             *   - Actualiza selectedPosition para mostrar el marcador
             *
             * properties (Propiedades del mapa):
             *   - isMyLocationEnabled = false: No mostrar botón "Mi ubicación" de Google
             *     (lo manejamos nosotros con permisos)
             *   - mapType = NORMAL: Tipo de mapa (NORMAL/SATELLITE/TERRAIN/HYBRID)
             *
             * uiSettings (Configuración de controles):
             *   - zoomControlsEnabled = true: Botones +/- para zoom
             *   - myLocationButtonEnabled = false: Sin botón de ubicación de Google
             *   - compassEnabled = true: Brújula cuando el mapa está rotado
             *   - mapToolbarEnabled = true: Herramientas de Google Maps (abrir en app)
             */
            GoogleMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),  // Clave para que el mapa ocupe el espacio disponible
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    // Cuando el usuario toca el mapa, guardar las coordenadas
                    selectedPosition = latLng
                },
                properties = MapProperties(
                    isMyLocationEnabled = false,
                    mapType = MapType.NORMAL
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = false,
                    compassEnabled = true,
                    mapToolbarEnabled = true
                )
            ) {
                // ===== MARCADORES EN EL MAPA =====

                /**
                 * Marcador 1: UBICACIÓN SELECCIONADA
                 *
                 * Aparece donde el usuario tocó el mapa.
                 *
                 * let: Solo se ejecuta si selectedPosition no es null
                 *
                 * Marker: Pin/marcador en el mapa
                 *   - state: Posición del marcador (MarkerState con coordenadas)
                 *   - title: Título que aparece al tocar el marcador
                 *   - snippet: Texto adicional debajo del título
                 */
                selectedPosition?.let { position ->
                    Marker(
                        state = MarkerState(position = position),
                        title = "📍 Ubicación seleccionada",
                        snippet = "Lat: ${String.format("%.4f", position.latitude)}, Lon: ${String.format("%.4f", position.longitude)}"
                    )
                }

                /**
                 * Marcador 2: UBICACIÓN ACTUAL (GPS o búsqueda previa)
                 *
                 * Aparece solo si:
                 * 1. currentLocation existe (no es null)
                 * 2. currentLocation es diferente de selectedPosition
                 *
                 * ¿Por qué la validación if (location != selectedPosition)?
                 * Para no mostrar DOS marcadores en el mismo punto.
                 * Si el usuario selecciona exactamente donde está currentLocation,
                 * solo se muestra el marcador de "seleccionada".
                 */
                currentLocation?.let { location ->
                    if (location != selectedPosition) {
                        Marker(
                            state = MarkerState(position = location),
                            title = "📱 Tu ubicación actual",
                            snippet = "Ubicación detectada por GPS"
                        )
                    }
                }
            }

            // ----- CARD DE ACCIÓN (INFERIOR) -----
            /**
             * Esta sección muestra DOS posibles estados:
             *
             * 1. SI hay ubicación seleccionada (selectedPosition != null):
             *    Muestra un Card con las coordenadas y botón "Ver clima"
             *
             * 2. SI NO hay ubicación seleccionada (selectedPosition == null):
             *    Muestra un Card con mensaje "Toca el mapa"
             *
             * Operador Elvis con run (?:):
             * - selectedPosition?.let { } se ejecuta si selectedPosition NO es null
             * - ?: run { } se ejecuta si selectedPosition ES null
             */
            selectedPosition?.let { position ->
                // ===== CASO 1: HAY UBICACIÓN SELECCIONADA =====

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        // Información de coordenadas
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "📍 Ubicación seleccionada",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                // Mostrar coordenadas con 4 decimales de precisión
                                // String.format("%.4f", ...) redondea a 4 decimales
                                Text(
                                    text = "Lat: ${String.format("%.4f", position.latitude)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Lon: ${String.format("%.4f", position.longitude)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        /**
                         * Botón principal de acción:
                         * Al hacer clic:
                         * 1. Ejecuta onLocationSelected(position)
                         * 2. MainActivity recibe las coordenadas
                         * 3. Llama a viewModel.selectLocationFromMap(position)
                         * 4. Se obtiene el clima de esas coordenadas
                         * 5. Se cambia automáticamente a la pestaña "Clima" (selectedTab = 0)
                         * 6. WeatherScreen muestra el clima de la ubicación seleccionada
                         */
                        Button(
                            onClick = { onLocationSelected(position) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ver clima de esta ubicación",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } ?: run {
                // ===== CASO 2: NO HAY UBICACIÓN SELECCIONADA =====

                /**
                 * Mensaje de ayuda cuando el usuario aún no ha tocado el mapa.
                 * Aparece inicialmente o después de cambiar de pestaña sin seleccionar nada.
                 */
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👆 Toca el mapa para seleccionar una ubicación",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * ===== RESUMEN DEL FLUJO DE MAPSCREEN =====
 *
 * 1. INICIALIZACIÓN:
 *    - Se carga el mapa centrado en currentLocation (o Ciudad de México por defecto)
 *    - Se muestra el marcador de currentLocation si existe
 *    - selectedPosition se inicializa con currentLocation
 *
 * 2. USUARIO TOCA EL MAPA:
 *    - onMapClick captura las coordenadas del toque
 *    - selectedPosition se actualiza con las nuevas coordenadas
 *    - Compose redibuja automáticamente: aparece el marcador en el nuevo punto
 *    - El card inferior cambia de "Toca el mapa" a "Ver clima"
 *
 * 3. USUARIO PRESIONA "VER CLIMA":
 *    - Se ejecuta onLocationSelected(selectedPosition)
 *    - MainActivity recibe las coordenadas
 *    - ViewModel obtiene el clima de esas coordenadas
 *    - Se cambia a la pestaña "Clima"
 *    - WeatherScreen muestra los datos del clima
 *
 * 4. SINCRONIZACIÓN CON CURRENTLOCATION:
 *    - Si en WeatherScreen el usuario busca una ciudad
 *    - currentLocation cambia a esa ciudad
 *    - LaunchedEffect detecta el cambio
 *    - El mapa se centra automáticamente en la nueva ciudad
 *    - Aparece el marcador en la nueva ubicación
 *
 * ===== CONCEPTOS TÉCNICOS =====
 *
 * • LatLng: Clase de Google Maps que representa coordenadas geográficas
 *   - latitude: Double (-90 a 90, Sur a Norte)
 *   - longitude: Double (-180 a 180, Oeste a Este)
 *
 * • CameraPositionState: Estado que controla la vista del mapa
 *   - position: Dónde está mirando el usuario
 *   - zoom: Nivel de acercamiento (1-21)
 *   - Se puede animar con cameraPositionState.animate()
 *
 * • rememberCameraPositionState: Función de Compose que:
 *   - Crea el estado de la cámara
 *   - Persiste entre recomposiciones
 *   - Permite controlar el mapa programáticamente
 *
 * • LaunchedEffect: Efecto secundario en Compose
 *   - Se ejecuta en una coroutine
 *   - Se relanza cuando cambian las claves (en este caso: currentLocation)
 *   - Útil para sincronizar estado externo con Compose
 *
 * • Marker: Pin visual en el mapa
 *   - MarkerState: Posición del marcador
 *   - title y snippet: Info que se muestra al tocar
 *   - Se puede personalizar con iconos custom
 */
