package com.example.healthtrackmobile.ui.tendencias

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthtrackmobile.model.Metrica
import com.example.healthtrackmobile.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TendenciasScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    onInicioClick: () -> Unit,
    onCitasClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEmbedded: Boolean = false,
    viewModel: TendenciasViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(userId) {
        viewModel.cargarMetricas(userId)
    }

    Scaffold(
        topBar = {
            if (!isEmbedded) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "GOBIERNO DE MÉXICO",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Dorado4T,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Tendencias de Salud",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Guinda4T)
                )
            }
        },
        bottomBar = {
            if (!isEmbedded) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = false,
                        onClick = onInicioClick,
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("Inicio") }
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = { },
                        icon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, null) },
                        label = { Text("Tendencias") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Guinda4T,
                            indicatorColor = Dorado4T.copy(alpha = 0.2f)
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = onCitasClick,
                        icon = { Icon(Icons.Default.DateRange, null) },
                        label = { Text("Citas") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = onProfileClick,
                        icon = { Icon(Icons.Default.Person, null) },
                        label = { Text("Perfil") }
                    )
                }
            }
        },
        containerColor = Fondo4T
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GuindaOficial)
            }
        } else if (state.error != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.error ?: "Error desconocido",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else if (state.glucosaMetricas.isEmpty() && state.presionMetricas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = DoradoOficial,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Sin Mediciones Registradas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GuindaOficial
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Registra tus tomas de Glucosa y Presión Arterial en el Dashboard de Inicio para ver las tendencias gráficas en tiempo real.",
                            fontSize = 13.sp,
                            color = Color.DarkGray,
                            lineHeight = 18.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Tarjetas de Resumen Estadístico (KPIs)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // KPI Glucosa
                        val avgGlucosa = if (state.glucosaMetricas.isNotEmpty()) {
                            "${state.glucosaMetricas.map { it.valor }.average().toInt()}"
                        } else "--"
                        
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Bloodtype, null, tint = GuindaOficial, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Promedio Glucosa", fontSize = 11.sp, color = Color.Gray)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("$avgGlucosa mg/dL", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GuindaOficial)
                            }
                        }

                        // KPI Presión
                        val avgPresion = if (state.presionMetricas.isNotEmpty()) {
                            val sis = state.presionMetricas.map { it.valor }.average().toInt()
                            val dia = state.presionMetricas.map { it.valorSecundario }.average().toInt()
                            "$sis/$dia"
                        } else "--"

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Speed, null, tint = DoradoOficial, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Promedio Presión", fontSize = 11.sp, color = Color.Gray)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("$avgPresion mmHg", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GuindaOficial)
                            }
                        }
                    }
                }

                // Gráfico Glucosa
                if (state.glucosaMetricas.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Tendencia de Glucosa",
                                    fontWeight = FontWeight.Bold,
                                    color = GuindaOficial,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Últimas ${state.glucosaMetricas.size} tomas clínicas (Rango normal: 70 - 125 mg/dL)",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                GlucoseChart(metrics = state.glucosaMetricas)
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(12.dp).background(Color(0xFFE2F0D9)))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Zona Normal (70-125)", fontSize = 10.sp, color = Color.DarkGray)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(12.dp, 2.dp).background(GuindaOficial))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Toma de Glucosa", fontSize = 10.sp, color = Color.DarkGray)
                                    }
                                }
                            }
                        }
                    }
                }

                // Gráfico Presión
                if (state.presionMetricas.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Tendencia de Presión Arterial",
                                    fontWeight = FontWeight.Bold,
                                    color = GuindaOficial,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Últimas ${state.presionMetricas.size} mediciones (Límites recomendados: 120 / 80)",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                PressureChart(metrics = state.presionMetricas)

                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(12.dp, 2.dp).background(GuindaOficial))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Sistólica (Límite 120)", fontSize = 10.sp, color = Color.DarkGray)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(12.dp, 2.dp).background(DoradoOficial))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Diastólica (Límite 80)", fontSize = 10.sp, color = Color.DarkGray)
                                    }
                                }
                            }
                        }
                    }
                }

                // Título de Historial
                item {
                    Text(
                        text = "Historial Detallado",
                        fontWeight = FontWeight.Bold,
                        color = GuindaOficial,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Listado de Historial
                val todasMetricas = (state.glucosaMetricas + state.presionMetricas).sortedByDescending { it.timestamp }
                items(todasMetricas) { metrica ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            if (metrica.tipo?.uppercase()?.contains("GLUCOSA") == true) GuindaOficial.copy(alpha = 0.1f)
                                            else DoradoOficial.copy(alpha = 0.2f),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (metrica.tipo?.uppercase()?.contains("GLUCOSA") == true) Icons.Default.Bloodtype else Icons.Default.Speed,
                                        contentDescription = null,
                                        tint = if (metrica.tipo?.uppercase()?.contains("GLUCOSA") == true) GuindaOficial else DoradoOficial,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (metrica.tipo?.uppercase()?.contains("GLUCOSA") == true) "Glucosa" else "Presión Arterial",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = GuindaOficial
                                    )
                                    val fechaStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(metrica.timestamp))
                                    Text(fechaStr, fontSize = 11.sp, color = Color.Gray)
                                    if (!metrica.comentario.isNullOrBlank()) {
                                        Text(metrica.comentario ?: "", fontSize = 11.sp, color = Color.DarkGray, maxLines = 1)
                                    }
                                }
                            }
                            Text(
                                text = if (metrica.tipo?.uppercase()?.contains("GLUCOSA") == true) "${metrica.valor.toInt()} mg/dL" else "${metrica.valor.toInt()}/${metrica.valorSecundario.toInt()} mmHg",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = GuindaOficial
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GlucoseChart(metrics: List<Metrica>, modifier: Modifier = Modifier) {
    val paintText = remember {
        Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 24f
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
    }
    val paintDate = remember {
        Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 24f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }
    val paintValueText = remember {
        Paint().apply {
            color = 0xFF621132.toInt() // Guinda
            textSize = 26f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(Color(0xFFFBFBFB))
    ) {
        val leftPadding = 70f
        val rightPadding = 45f
        val topPadding = 45f
        val bottomPadding = 65f

        val graphWidth = size.width - leftPadding - rightPadding
        val graphHeight = size.height - topPadding - bottomPadding

        val minY = 50.0
        val maxY = 200.0
        val yScale = graphHeight / (maxY - minY)

        // 1. Dibujar rango normal (70 a 125 mg/dL)
        val yNormalMin = size.height - bottomPadding - (125.0 - minY) * yScale
        val yNormalMax = size.height - bottomPadding - (70.0 - minY) * yScale
        drawRect(
            color = Color(0xFFE2F0D9),
            topLeft = androidx.compose.ui.geometry.Offset(leftPadding, yNormalMin.toFloat()),
            size = androidx.compose.ui.geometry.Size(graphWidth, (yNormalMax - yNormalMin).toFloat())
        )

        // 2. Dibujar líneas de cuadrícula horizontales
        val gridLines = listOf(70.0, 100.0, 125.0, 150.0, 180.0)
        gridLines.forEach { valY ->
            val y = size.height - bottomPadding - (valY - minY) * yScale
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = androidx.compose.ui.geometry.Offset(leftPadding, y.toFloat()),
                end = androidx.compose.ui.geometry.Offset(size.width - rightPadding, y.toFloat()),
                strokeWidth = 1f
            )
            // Etiqueta del valor Y
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawText(
                    "${valY.toInt()}",
                    leftPadding - 10f,
                    y.toFloat() + 8f,
                    paintText
                )
            }
        }

        // 3. Dibujar curva y puntos
        if (metrics.isNotEmpty()) {
            val points = metrics.mapIndexed { index, m ->
                val xStep = if (metrics.size > 1) graphWidth / (metrics.size - 1) else 0f
                val x = leftPadding + index * xStep
                val y = size.height - bottomPadding - (m.valor - minY) * yScale
                androidx.compose.ui.geometry.Offset(x, y.toFloat())
            }

            // Dibujar gradiente debajo de la curva
            val fillPath = Path().apply {
                if (points.isNotEmpty()) {
                    moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                    lineTo(points.last().x, size.height - bottomPadding)
                    lineTo(points.first().x, size.height - bottomPadding)
                    close()
                }
            }
            if (points.isNotEmpty()) {
                drawPath(
                    path = fillPath,
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            GuindaOficial.copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        startY = points.minOf { it.y },
                        endY = size.height - bottomPadding
                    )
                )
            }

            // Dibujar línea conectora
            val path = Path().apply {
                if (points.isNotEmpty()) {
                    moveTo(points[0].x, points[0].y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }
            }
            drawPath(
                path = path,
                color = GuindaOficial,
                style = Stroke(width = 3.dp.toPx())
            )

            // Dibujar puntos interactivos y sus valores
            metrics.forEachIndexed { index, m ->
                val pt = points[index]
                // Determinar color del punto (Rojo si supera 125)
                val ptColor = if (m.valor > 125.0) Color.Red else GuindaOficial

                drawCircle(
                    color = ptColor,
                    radius = 5.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = pt
                )

                // Dibujar valor numérico
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(
                        "${m.valor.toInt()}",
                        pt.x,
                        pt.y - 15f,
                        paintValueText.apply { color = ptColor.hashCode() }
                    )
                }

                // Dibujar fecha en el eje X
                val dateStr = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(m.timestamp))
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(
                        dateStr,
                        pt.x,
                        size.height - 15f,
                        paintDate
                    )
                }
            }
        }
    }
}

@Composable
fun PressureChart(metrics: List<Metrica>, modifier: Modifier = Modifier) {
    val paintText = remember {
        Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 24f
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
    }
    val paintDate = remember {
        Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 24f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }
    val paintValueTextSis = remember {
        Paint().apply {
            color = 0xFF621132.toInt() // Guinda
            textSize = 25f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
    }
    val paintValueTextDia = remember {
        Paint().apply {
            color = 0xFFB38E5D.toInt() // Dorado
            textSize = 25f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(Color(0xFFFBFBFB))
    ) {
        val leftPadding = 70f
        val rightPadding = 45f
        val topPadding = 45f
        val bottomPadding = 65f

        val graphWidth = size.width - leftPadding - rightPadding
        val graphHeight = size.height - topPadding - bottomPadding

        val minY = 50.0
        val maxY = 180.0
        val yScale = graphHeight / (maxY - minY)

        // 1. Dibujar líneas de referencia recomendadas (120 y 80)
        val limits = listOf(80.0, 120.0)
        limits.forEach { limit ->
            val y = size.height - bottomPadding - (limit - minY) * yScale
            drawLine(
                color = Color.Red.copy(alpha = 0.3f),
                start = androidx.compose.ui.geometry.Offset(leftPadding, y.toFloat()),
                end = androidx.compose.ui.geometry.Offset(size.width - rightPadding, y.toFloat()),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
            )
        }

        // 2. Dibujar líneas de cuadrícula horizontales
        val gridLines = listOf(60.0, 80.0, 100.0, 120.0, 140.0, 160.0)
        gridLines.forEach { valY ->
            val y = size.height - bottomPadding - (valY - minY) * yScale
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = androidx.compose.ui.geometry.Offset(leftPadding, y.toFloat()),
                end = androidx.compose.ui.geometry.Offset(size.width - rightPadding, y.toFloat()),
                strokeWidth = 1f
            )
            // Etiqueta del valor Y
            drawIntoCanvas { canvas ->
                canvas.nativeCanvas.drawText(
                    "${valY.toInt()}",
                    leftPadding - 10f,
                    y.toFloat() + 8f,
                    paintText
                )
            }
        }

        // 3. Dibujar curvas y puntos
        if (metrics.isNotEmpty()) {
            val xStep = if (metrics.size > 1) graphWidth / (metrics.size - 1) else 0f

            val pointsSis = metrics.mapIndexed { index, m ->
                val x = leftPadding + index * xStep
                val y = size.height - bottomPadding - (m.valor - minY) * yScale
                androidx.compose.ui.geometry.Offset(x, y.toFloat())
            }

            val pointsDia = metrics.mapIndexed { index, m ->
                val x = leftPadding + index * xStep
                val y = size.height - bottomPadding - (m.valorSecundario - minY) * yScale
                androidx.compose.ui.geometry.Offset(x, y.toFloat())
            }

            // Dibujar gradiente Sistólica
            val fillPathSis = Path().apply {
                if (pointsSis.isNotEmpty()) {
                    moveTo(pointsSis[0].x, pointsSis[0].y)
                    for (i in 1 until pointsSis.size) {
                        lineTo(pointsSis[i].x, pointsSis[i].y)
                    }
                    lineTo(pointsSis.last().x, size.height - bottomPadding)
                    lineTo(pointsSis.first().x, size.height - bottomPadding)
                    close()
                }
            }
            if (pointsSis.isNotEmpty()) {
                drawPath(
                    path = fillPathSis,
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            GuindaOficial.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        startY = pointsSis.minOf { it.y },
                        endY = size.height - bottomPadding
                    )
                )
            }

            // Dibujar gradiente Diastólica
            val fillPathDia = Path().apply {
                if (pointsDia.isNotEmpty()) {
                    moveTo(pointsDia[0].x, pointsDia[0].y)
                    for (i in 1 until pointsDia.size) {
                        lineTo(pointsDia[i].x, pointsDia[i].y)
                    }
                    lineTo(pointsDia.last().x, size.height - bottomPadding)
                    lineTo(pointsDia.first().x, size.height - bottomPadding)
                    close()
                }
            }
            if (pointsDia.isNotEmpty()) {
                drawPath(
                    path = fillPathDia,
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            DoradoOficial.copy(alpha = 0.20f),
                            Color.Transparent
                        ),
                        startY = pointsDia.minOf { it.y },
                        endY = size.height - bottomPadding
                    )
                )
            }

            // Dibujar línea conectora Sistólica (Guinda)
            val pathSis = Path().apply {
                if (pointsSis.isNotEmpty()) {
                    moveTo(pointsSis[0].x, pointsSis[0].y)
                    for (i in 1 until pointsSis.size) {
                        lineTo(pointsSis[i].x, pointsSis[i].y)
                    }
                }
            }
            drawPath(
                path = pathSis,
                color = GuindaOficial,
                style = Stroke(width = 3.dp.toPx())
            )

            // Dibujar línea conectora Diastólica (Dorado)
            val pathDia = Path().apply {
                if (pointsDia.isNotEmpty()) {
                    moveTo(pointsDia[0].x, pointsDia[0].y)
                    for (i in 1 until pointsDia.size) {
                        lineTo(pointsDia[i].x, pointsDia[i].y)
                    }
                }
            }
            drawPath(
                path = pathDia,
                color = DoradoOficial,
                style = Stroke(width = 3.dp.toPx())
            )

            // Dibujar puntos y etiquetas
            metrics.forEachIndexed { index, m ->
                val ptSis = pointsSis[index]
                val ptDia = pointsDia[index]

                // Puntos Sistólica
                drawCircle(color = GuindaOficial, radius = 4.5f.dp.toPx(), center = ptSis)
                drawCircle(color = Color.White, radius = 1.5f.dp.toPx(), center = ptSis)
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(
                        "${m.valor.toInt()}",
                        ptSis.x,
                        ptSis.y - 12f,
                        paintValueTextSis
                    )
                }

                // Puntos Diastólica
                drawCircle(color = DoradoOficial, radius = 4.5f.dp.toPx(), center = ptDia)
                drawCircle(color = Color.White, radius = 1.5f.dp.toPx(), center = ptDia)
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(
                        "${m.valorSecundario.toInt()}",
                        ptDia.x,
                        ptDia.y + 25f,
                        paintValueTextDia
                    )
                }

                // Dibujar fecha en el eje X
                val dateStr = SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(m.timestamp))
                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawText(
                        dateStr,
                        ptSis.x,
                        size.height - 15f,
                        paintDate
                    )
                }
            }
        }
    }
}
