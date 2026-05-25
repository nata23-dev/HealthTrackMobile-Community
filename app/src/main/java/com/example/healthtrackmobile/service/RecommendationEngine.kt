package com.example.healthtrackmobile.service

import com.example.healthtrackmobile.model.Metrica
import com.example.healthtrackmobile.model.Recomendacion
import com.example.healthtrackmobile.model.Usuario
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.util.Calendar
import java.util.Locale

data class ClimaResponse(
    var ciudad: String = "",
    var temperatura: Double = 0.0,
    var humedad: Double = 0.0,
    var condicion: String = "",
    var disponible: Boolean = false,
    var mensajeError: String? = null,
    var calidadAire: String = "",
    var calidadAireRiesgosa: Boolean = false,
    var aqiValue: Int = 0
)

data class AlertaSanitariaResponse(
    var region: String = "",
    var descripcion: String = "",
    var nivelRiesgo: String = "",
    var activa: Boolean = false
)

class RecommendationEngine {

    private val geoUrl = "https://geocoding-api.open-meteo.com/v1/search"
    private val forecastUrl = "https://api.open-meteo.com/v1/forecast"
    private val airUrl = "https://air-quality-api.open-meteo.com/v1/air-quality"
    private val diseaseUrl = "https://disease.sh/v3/covid-19/countries/"
    private val fruityviceUrl = "https://www.fruityvice.com/api/fruit/"
    private val ipApiUrl = "https://ipapi.co/json/"

    private val consejosNutricionales = listOf(
        "Recuerda mantenerte hidratado: bebe al menos 2 litros de agua al día.",
        "Incluye una porción de vegetales verdes en tu comida principal.",
        "Evita consumir alimentos ultraprocesados ricos en azúcar.",
        "Las frutas frescas son una excelente opción para tus snacks.",
        "Prefiere grasas saludables como el aceite de oliva o el aguacate."
    )

    private val frutasConsejo = listOf(
        "apple", "banana", "orange", "strawberry", "blueberry", "raspberry", "apricot", "pear", "peach"
    )

    private fun httpGet(urlString: String, timeoutMs: Int = 4000): String {
        try {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = timeoutMs
            conn.readTimeout = timeoutMs
            if (conn.responseCode in 200..299) {
                conn.inputStream.use { stream ->
                    return stream.bufferedReader().use { it.readText() }
                }
            } else {
                throw IOException("HTTP Error: ${conn.responseCode}")
            }
        } catch (e: SocketTimeoutException) {
            throw IOException("Timeout de red al conectar con el servicio externo")
        } catch (e: Exception) {
            throw IOException("Falla de conexión: ${e.message}")
        }
    }

    fun getClimaActual(ciudad: String?, estado: String?): ClimaResponse {
        var queryName = ciudad ?: ""
        if (queryName.contains(",")) {
            // Ya contiene el formato adecuado
        } else if (!estado.isNullOrBlank()) {
            queryName = "$ciudad, $estado"
        }

        if (queryName.isBlank()) {
            try {
                // Intento detectar ubicación por IP
                val ipRes = httpGet(ipApiUrl)
                val ipJson = JSONObject(ipRes)
                queryName = ipJson.optString("city", "Celaya")
            } catch (e: Exception) {
                queryName = "Celaya"
            }
        }

        try {
            // 1. Geocodificación: Ciudad / Estado -> Latitud, Longitud
            val encodedQuery = URLEncoder.encode(queryName.trim(), "UTF-8")
            val geoResponseStr = httpGet("$geoUrl?name=$encodedQuery&count=1&language=es")
            var geoJson = JSONObject(geoResponseStr)

            // Fallback 1: Si falló, intentar dividiendo
            if (!geoJson.has("results") || geoJson.getJSONArray("results").length() == 0) {
                if (queryName.contains(",")) {
                    val parts = queryName.split(",")
                    if (parts.size >= 2) {
                        val fallbackQuery = parts[parts.size - 2].trim() + ", " + parts[parts.size - 1].trim()
                        val encodedFallback = URLEncoder.encode(fallbackQuery, "UTF-8")
                        val res = httpGet("$geoUrl?name=$encodedFallback&count=1&language=es")
                        geoJson = JSONObject(res)
                    }
                }
            }

            // Fallback 2: Intentar solo con la última parte
            if (!geoJson.has("results") || geoJson.getJSONArray("results").length() == 0) {
                if (queryName.contains(",")) {
                    val parts = queryName.split(",")
                    if (parts.size >= 2) {
                        val cityOnly = parts[parts.size - 2].trim()
                        if (cityOnly.isNotBlank()) {
                            val encodedCityOnly = URLEncoder.encode(cityOnly, "UTF-8")
                            val res = httpGet("$geoUrl?name=$encodedCityOnly&count=1&language=es")
                            geoJson = JSONObject(res)
                        }
                    }
                }
            }

            // Fallback 3: Intentar con la ciudad original sola
            if (!geoJson.has("results") || geoJson.getJSONArray("results").length() == 0) {
                if (!ciudad.isNullOrBlank()) {
                    val encodedCity = URLEncoder.encode(ciudad.trim(), "UTF-8")
                    val res = httpGet("$geoUrl?name=$encodedCity&count=1&language=es")
                    geoJson = JSONObject(res)
                }
            }

            if (!geoJson.has("results") || geoJson.getJSONArray("results").length() == 0) {
                return getSimulatedWeather(queryName)
            }

            val result = geoJson.getJSONArray("results").getJSONObject(0)
            val lat = result.getDouble("latitude")
            val lon = result.getDouble("longitude")
            val nombreCiudadFormateado = result.getString("name")

            // 2. Obtener Clima
            val forecastResponseStr = httpGet("$forecastUrl?latitude=$lat&longitude=$lon&current=temperature_2m,relative_humidity_2m,weather_code")
            val forecastJson = JSONObject(forecastResponseStr)
            val current = forecastJson.getJSONObject("current")
            val temp = current.getDouble("temperature_2m")
            val humidity = current.getDouble("relative_humidity_2m")
            val weatherCode = current.getInt("weather_code")

            val clima = ClimaResponse(
                ciudad = nombreCiudadFormateado,
                temperatura = temp,
                humedad = humidity,
                condicion = interpretWeatherCode(weatherCode),
                disponible = true
            )

            // 3. Obtener Calidad del Aire
            try {
                val airResponseStr = httpGet("$airUrl?latitude=$lat&longitude=$lon&current=us_aqi")
                val airJson = JSONObject(airResponseStr)
                val airCurrent = airJson.getJSONObject("current")
                val aqi = airCurrent.getInt("us_aqi")
                clima.aqiValue = aqi

                val label = when {
                    aqi <= 50 -> "Buena"
                    aqi <= 100 -> "Aceptable"
                    aqi <= 150 -> "Moderada"
                    aqi <= 200 -> "Mala"
                    else -> "Muy mala"
                }

                clima.calidadAire = "Calidad del aire: $label (AQI $aqi)"
                clima.calidadAireRiesgosa = aqi > 100
            } catch (e: Exception) {
                clima.calidadAire = "Calidad del aire no disponible"
                clima.calidadAireRiesgosa = false
            }

            return clima
        } catch (e: Exception) {
            return getSimulatedWeather(queryName)
        }
    }

    fun obtenerAlertasActivas(region: String?): List<AlertaSanitariaResponse> {
        val reg = region ?: "Mexico"
        val alertas = mutableListOf<AlertaSanitariaResponse>()
        try {
            val encodedRegion = URLEncoder.encode(reg.trim(), "UTF-8")
            val resStr = httpGet("$diseaseUrl$encodedRegion")
            val root = JSONObject(resStr)

            val active = root.getLong("active")
            val activePerMillion = root.getDouble("activePerOneMillion")
            val country = root.getString("country")

            var nivelRiesgo = "BAJA"
            var activa = false
            var descripcion = ""

            if (activePerMillion > 2000) {
                nivelRiesgo = "ALTA"
                activa = true
                descripcion = "Riesgo Elevado sanitario en $country: $active casos activos en seguimiento epidemiológico (${activePerMillion.toInt()} por millón). Tome precauciones en lugares cerrados."
            } else if (activePerMillion > 500) {
                nivelRiesgo = "MEDIA"
                activa = true
                descripcion = "Aviso epidemiológico moderado en $country: $active casos activos de COVID-19 registrados (${activePerMillion.toInt()} por millón). Mantenga higiene de manos."
            } else {
                nivelRiesgo = "BAJA"
                activa = false
                descripcion = "Situación epidemiológica controlada en $country: $active casos activos (${activePerMillion.toInt()} por millón)."
            }

            if (activa) {
                alertas.add(AlertaSanitariaResponse(country, descripcion, nivelRiesgo, true))
            }
        } catch (e: Exception) {
            // Silently fail to load local seasonal fallbacks
        }

        // Si no hay alertas externas activas, aplicar las del calendario estacional
        if (alertas.isEmpty()) {
            val month = Calendar.getInstance().get(Calendar.MONTH) // 0-indexed: 0=Jan, 1=Feb, ...
            if (month in Calendar.MAY..Calendar.AUGUST) {
                return listOf(
                    AlertaSanitariaResponse(reg, "Golpe de Calor: Temperaturas elevadas registradas. Manténgase hidratado.", "ALTA", true),
                    AlertaSanitariaResponse(reg, "Contaminación por ozono en superficie: Alta radiación solar y nulo viento.", "ALTA", true),
                    AlertaSanitariaResponse(reg, "Dengue: Incremento estacional por lluvias. Evite criaderos de mosquitos.", "MEDIA", true)
                )
            }
            if (month == Calendar.NOVEMBER || month == Calendar.DECEMBER || month == Calendar.JANUARY || month == Calendar.FEBRUARY) {
                return listOf(
                    AlertaSanitariaResponse(reg, "Influenza estacional: Descenso de temperaturas e incremento de infecciones respiratorias.", "MEDIA", true)
                )
            }
        }

        return alertas
    }

    fun generarSugerenciasIAPaciente(
        usuario: Usuario?,
        metricas: List<Metrica>?,
        clima: ClimaResponse?
    ): List<Recomendacion> {
        val recs = mutableListOf<Recomendacion>()
        if (usuario == null) return recs

        val ordenadas = metricas?.sortedBy { it.timestamp } ?: emptyList()

        // CRUCE 1: Clínico-Ambiental (Meteo + Métricas cardiovasculares)
        var hasHypertension = false
        val presiones = ordenadas.filter { "PRESION" == it.tipo }
        if (presiones.isNotEmpty()) {
            val ultPres = presiones.last()
            if (ultPres.valor > 130.0 || ultPres.valorSecundario > 85.0) {
                hasHypertension = true
            }
        }

        if (clima != null && clima.disponible) {
            val climaAdverso = clima.condicion.lowercase().let {
                it.contains("lluv") || it.contains("tormenta") || it.contains("nieve") ||
                        it.contains("rain") || it.contains("storm")
            }
            val aireDeficiente = clima.calidadAireRiesgosa

            if ((climaAdverso || aireDeficiente) && hasHypertension) {
                val r = Recomendacion(
                    medicoNombre = "Prevención IA",
                    prioridad = "ALTA",
                    mensaje = "Clima adverso o calidad del aire baja detectada. Debido a su estado cardiovascular, se recomienda suspender actividades al aire libre y realizar cardio funcional en casa.",
                    fechaEnvio = System.currentTimeMillis()
                )
                recs.add(r)
            } else if (!climaAdverso && !aireDeficiente) {
                val r = Recomendacion(
                    medicoNombre = "Prevención IA",
                    prioridad = "BAJA",
                    mensaje = "El clima es óptimo hoy. Se sugiere realizar una caminata ligera para mantener su presión arterial estable.",
                    fechaEnvio = System.currentTimeMillis()
                )
                recs.add(r)
            }
        } else {
            // Contingencia offline
            val r = Recomendacion(
                medicoNombre = "Prevención IA",
                prioridad = "MEDIA",
                mensaje = "Clima no disponible (Modo Offline). Manténgase hidratado y evite salir en horas de extremo calor.",
                fechaEnvio = System.currentTimeMillis()
            )
            recs.add(r)
        }

        // CRUCE 2: Clínico-Epidemiológico (Disease.sh + Alertas)
        try {
            val alertas = obtenerAlertasActivas("Mexico")
            for (alerta in alertas) {
                if ("ALTA".equals(alerta.nivelRiesgo, ignoreCase = true) && alerta.activa) {
                    val r = Recomendacion(
                        medicoNombre = "Prevención IA",
                        prioridad = "ALTA",
                        mensaje = "Alerta Sanitaria Activa: Elevación de casos epidemiológicos. Se sugiere el uso de mascarilla en espacios cerrados y lavado frecuente de manos.",
                        fechaEnvio = System.currentTimeMillis()
                    )
                    recs.add(r)
                }
            }
        } catch (e: Exception) {
            // Fallback Contingencia
        }

        // CRUCE 3: Clínico-Nutricional Inteligente (Fruityvice + Glucosa)
        val ultGlucosa = ordenadas.lastOrNull { "GLUCOSA" == it.tipo }
        val glucosaVal = ultGlucosa?.valor ?: 0.0

        if (glucosaVal > 125.0) {
            val r = Recomendacion(
                medicoNombre = "Prevención IA",
                prioridad = "MEDIA",
                fechaEnvio = System.currentTimeMillis()
            )
            try {
                val consejoNutricion = recomendarAlimentosBajoIndiceGlucemico(glucosaVal)
                r.mensaje = "Sugerencia Nutricional: Glucosa elevada. $consejoNutricion"
            } catch (e: Exception) {
                r.mensaje = "Sugerencia Nutricional (Modo Offline): Mantenga una dieta balanceada baja en azúcares refinados. Priorice verduras y proteínas magras."
            }
            recs.add(r)
        } else {
            // Consejo general
            val r = Recomendacion(
                medicoNombre = "Prevención IA",
                prioridad = "BAJA",
                fechaEnvio = System.currentTimeMillis()
            )
            try {
                r.mensaje = obtenerConsejoNutricional()
            } catch (e: Exception) {
                r.mensaje = "Sugerencia Nutricional (Modo Offline): Mantenga una dieta balanceada baja en azúcares refinados."
            }
            recs.add(r)
        }

        return recs
    }

    private fun recomendarAlimentosBajoIndiceGlucemico(ultimaGlucosaMgDl: Double): String {
        if (ultimaGlucosaMgDl <= 0) {
            return "Nutrición: Mantenga patrón mediterráneo mexicano (verduras, leguminosas, agua simple)."
        }

        val fruta = if (ultimaGlucosaMgDl > 125.0) "raspberry" else "apricot"
        val frutaNombreEs = if (ultimaGlucosaMgDl > 125.0) "Frambuesa" else "Chabacano"

        try {
            val resStr = httpGet("$fruityviceUrl$fruta")
            val root = JSONObject(resStr)
            val nutrition = root.getJSONObject("nutritions")
            val sugar = nutrition.getDouble("sugar")
            val carbs = nutrition.getDouble("carbohydrates")

            if (ultimaGlucosaMgDl > 180.0) {
                return String.format(
                    Locale.US,
                    "Priorizar %s (bajo aporte glucémico, solo %.1fg de azúcar y %.1fg de carbohidratos por 100g) y evitar jugos.",
                    frutaNombreEs, sugar, carbs
                )
            }
            if (ultimaGlucosaMgDl > 125.0) {
                return String.format(
                    Locale.US,
                    "Incorporar %s (solo %.1fg de azúcar por 100g) y vegetales; regular porción de carbohidratos simples.",
                    frutaNombreEs, sugar
                )
            }
        } catch (e: Exception) {
            // Fallback local
        }

        if (ultimaGlucosaMgDl > 180.0) {
            return "Priorizar avena integral sin azúcar, leguminosas (lenteja, garbanzo), verduras no almidonadas y evitar jugos; fraccionar hidratos de carbono complejos."
        }
        if (ultimaGlucosaMgDl > 125.0) {
            return "Incorporar quinoa, chícharo, ejotes y frutos rojos; sustituir pan blanco por tortillas de maíz integral en porción controlada."
        }
        return "Continuar con fibra soluble (cebada, linaza molida) y proteína magra para estabilidad glucémica."
    }

    private fun obtenerConsejoNutricional(): String {
        val randomFruit = frutasConsejo.random()
        try {
            val resStr = httpGet("$fruityviceUrl$randomFruit")
            val root = JSONObject(resStr)
            val nutrition = root.getJSONObject("nutritions")
            val calories = nutrition.getDouble("calories")
            val sugar = nutrition.getDouble("sugar")

            val frutaNombreEs = translateFruit(randomFruit)
            return String.format(
                Locale.US,
                "Tip de nutrición: ¿Sabías que 100g de %s contienen solo %.0f calorías y %.1fg de azúcar natural? ¡Excelente opción de snack!",
                frutaNombreEs, calories, sugar
            )
        } catch (e: Exception) {
            // Fallback local
        }
        return consejosNutricionales.random()
    }

    private fun translateFruit(englishName: String): String {
        return when (englishName) {
            "apple" -> "manzana"
            "banana" -> "plátano"
            "orange" -> "naranja"
            "strawberry" -> "fresa"
            "blueberry" -> "arándano"
            "raspberry" -> "frambuesa"
            "apricot" -> "chabacano"
            "pear" -> "pera"
            "peach" -> "durazno"
            else -> englishName
        }
    }

    private fun getSimulatedWeather(ciudad: String): ClimaResponse {
        val clima = ClimaResponse(ciudad = ciudad, disponible = true)
        if (ciudad.lowercase().contains("mexico")) {
            clima.temperatura = 24.5
            clima.humedad = 52.0
            clima.condicion = "Soleado con nubes dispersas"
            clima.calidadAire = "Calidad del aire moderada; ozono en aumento en horas centrales."
            clima.calidadAireRiesgosa = true
        } else {
            clima.temperatura = 18.0
            clima.humedad = 60.0
            clima.condicion = "Ligeramente nublado"
            clima.calidadAire = "Calidad del aire aceptable."
            clima.calidadAireRiesgosa = false
        }
        return clima
    }

    private fun interpretWeatherCode(code: Int): String {
        return when (code) {
            0 -> "Cielo despejado"
            1 -> "Principalmente despejado"
            2 -> "Parcialmente nublado"
            3 -> "Nublado"
            45, 48 -> "Neblina"
            51, 53, 55 -> "Llovizna"
            61, 63, 65 -> "Lluvia"
            71, 73, 75 -> "Nevada"
            80, 81, 82 -> "Lluvia torrencial"
            95, 96, 99 -> "Tormenta eléctrica"
            else -> "Despejado"
        }
    }
}
