package com.example.healthtrackmobile.ui.familia

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthtrackmobile.model.Metrica
import com.example.healthtrackmobile.model.PerfilPaciente
import com.example.healthtrackmobile.model.Usuario
import com.example.healthtrackmobile.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamiliaScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    isEmbedded: Boolean = false,
    viewModel: FamiliaViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        viewModel.cargarFamiliares(userId)
    }

    LaunchedEffect(state.error) {
        state.error?.let { err ->
            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            if (!isEmbedded) {
                TopAppBar(
                    title = { Text("Mi Familia", color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Guinda4T)
                )
            }
        },
        containerColor = Fondo4T
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Cabecera Institucional
            Column {
                Text("GOBIERNO DE MÉXICO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Guinda4T, letterSpacing = 1.sp)
                Text("Mi Familia — Red de Seguimiento de Salud", fontSize = 13.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Dorado4T))
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Guinda4T)
                }
            } else if (state.familiarSeleccionado != null) {
                // MODO DETALLE
                FamiliarDetailContent(
                    familiar = state.familiarSeleccionado!!,
                    perfil = state.perfilSeleccionado,
                    glucosa = state.ultimaGlucosa,
                    presion = state.ultimaPresion,
                    frecuencia = state.ultimaFrecuencia,
                    peso = state.ultimoPeso,
                    onBackToList = { viewModel.deseleccionarFamiliar() }
                )
            } else {
                // MODO LISTA Y BÚSQUEDA
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    // Card 1: Búsqueda
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Buscar Nuevos Miembros",
                                    fontWeight = FontWeight.Bold,
                                    color = Guinda4T,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        placeholder = { Text("Nombre del paciente...") },
                                        modifier = Modifier.weight(1f),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.White,
                                            unfocusedContainerColor = Color.White,
                                            focusedIndicatorColor = Dorado4T
                                        ),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = { viewModel.buscarPacientes(userId, searchQuery) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Guinda4T),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Buscar", color = Color.White)
                                    }
                                }

                                if (state.resultadosBusqueda.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Resultados de Búsqueda",
                                        fontWeight = FontWeight.Bold,
                                        color = DoradoOficial,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    state.resultadosBusqueda.forEach { result ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = result.nombre ?: "Paciente",
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 14.sp
                                            )
                                            Button(
                                                onClick = {
                                                    viewModel.agregarFamiliar(userId, result.id ?: "")
                                                    searchQuery = ""
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = VerdeSalud4T),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text("Agregar", color = Color.White, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Card 2: Lista de familiares
                    item {
                        Text(
                            text = "Miembros de mi Familia",
                            fontWeight = FontWeight.Bold,
                            color = Guinda4T,
                            fontSize = 15.sp
                        )
                    }

                    if (state.familiares.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Aún no tienes familiares registrados.",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        items(state.familiares) { familiar ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.seleccionarFamiliar(familiar) },
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Person, null, tint = DoradoOficial)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = familiar.nombre ?: "Familiar",
                                            fontWeight = FontWeight.Bold,
                                            color = TextoPrincipal,
                                            fontSize = 15.sp
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.eliminarFamiliar(userId, familiar.id ?: "") }
                                    ) {
                                        Icon(Icons.Default.Delete, "Eliminar", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FamiliarDetailContent(
    familiar: Usuario,
    perfil: PerfilPaciente?,
    glucosa: Metrica?,
    presion: Metrica?,
    frecuencia: Metrica?,
    peso: Metrica?,
    onBackToList: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Botón Volver
        item {
            Row(
                modifier = Modifier
                    .clickable { onBackToList() }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Guinda4T)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Volver a la lista de familiares",
                    color = Guinda4T,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // Ficha del Familiar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = familiar.nombre ?: "Familiar",
                        fontWeight = FontWeight.Bold,
                        color = Guinda4T,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Edad: ${perfil?.calcularEdad() ?: "--"} años",
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "Sangre: ${perfil?.grupoSanguineo ?: "--"}",
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.LightGray.copy(alpha = 0.5f)))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Alergias",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = DoradoOficial
                    )
                    Text(
                        text = perfil?.alergias ?: "Ninguna",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Antecedentes Médicos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = DoradoOficial
                    )
                    Text(
                        text = perfil?.antecedentes ?: "Ninguno",
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }
            }
        }

        // Mediciones
        item {
            Text(
                text = "Últimas Mediciones de Salud",
                fontWeight = FontWeight.Bold,
                color = Guinda4T,
                fontSize = 15.sp
            )
        }

        item {
            // Glucosa
            val gVal = glucosa?.valor ?: 0.0
            val gText = if (gVal > 0) "${gVal.toInt()} mg/dL" else "--"
            val gCat = when {
                gVal <= 0 -> "Sin registro"
                gVal > 200.0 -> "Crítico"
                gVal > 125.0 -> "Elevada"
                gVal >= 100.0 -> "Límite alto"
                else -> "En objetivo"
            }
            val gStyle = when {
                gVal <= 0 -> "muted"
                gVal > 200.0 -> "alert"
                gVal > 125.0 || gVal >= 100.0 -> "warning"
                else -> "normal"
            }

            // Presión
            val sys = presion?.valor ?: 0.0
            val dia = presion?.valorSecundario ?: 0.0
            val pText = if (sys > 0 && dia > 0) "${sys.toInt()}/${dia.toInt()} mmHg" else "--"
            val pCat = when {
                sys <= 0 || dia <= 0 -> "Sin registro"
                sys < 90 || dia < 60 -> "Hipotensión"
                sys > 180 || dia > 120 -> "Crisis"
                sys >= 140 || dia >= 90 -> "Hipertensión E2"
                sys >= 130 || dia >= 80 -> "Hipertensión E1"
                sys >= 120 -> "Elevada"
                else -> "Normal"
            }
            val pStyle = when {
                sys <= 0 || dia <= 0 -> "muted"
                sys < 90 || dia < 60 || sys > 180 || dia > 120 || sys >= 140 || dia >= 90 -> "alert"
                sys >= 130 || dia >= 80 || sys >= 120 -> "warning"
                else -> "normal"
            }

            // Peso
            val wVal = peso?.valor ?: 0.0
            val estCm = perfil?.estatura ?: 0.0
            val estM = estCm / 100.0
            val imc = if (wVal > 0 && estM > 0) wVal / (estM * estM) else 0.0
            val wText = if (wVal > 0) "${wVal.toInt()} kg" else "--"
            val wCat = when {
                wVal <= 0 -> "Sin registro"
                imc <= 0 -> "Registre estatura"
                imc < 18.5 -> "Bajo peso (${String.format(java.util.Locale.US, "%.1f", imc)})"
                imc < 25.0 -> "Normal (${String.format(java.util.Locale.US, "%.1f", imc)})"
                imc < 30.0 -> "Sobrepeso (${String.format(java.util.Locale.US, "%.1f", imc)})"
                else -> "Obesidad (${String.format(java.util.Locale.US, "%.1f", imc)})"
            }
            val wStyle = when {
                wVal <= 0 -> "muted"
                imc <= 0 -> "muted"
                imc < 18.5 -> "warning"
                imc < 25.0 -> "normal"
                imc < 30.0 -> "warning"
                else -> "alert"
            }

            // Frecuencia
            val fVal = frecuencia?.valor ?: 0.0
            val fText = if (fVal > 0) "${fVal.toInt()} lpm" else "--"
            val fCat = when {
                fVal <= 0 -> "Sin registro"
                fVal >= 130.0 -> "Taquicardia A"
                fVal > 100.0 -> "Taquicardia"
                fVal >= 60.0 -> "Normal"
                fVal >= 50.0 -> "Bradicardia"
                else -> "Bradicardia B"
            }
            val fStyle = when {
                fVal <= 0 -> "muted"
                fVal >= 130.0 || fVal < 50.0 -> "alert"
                fVal > 100.0 || fVal < 60.0 -> "warning"
                else -> "normal"
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FamiliarKPICard(
                        title = "Glucosa",
                        value = gText,
                        category = gCat,
                        style = gStyle,
                        icon = Icons.Default.Bloodtype,
                        modifier = Modifier.weight(1f)
                    )
                    FamiliarKPICard(
                        title = "Presión Arterial",
                        value = pText,
                        category = pCat,
                        style = pStyle,
                        icon = Icons.Default.Favorite,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FamiliarKPICard(
                        title = "Peso Corporal",
                        value = wText,
                        category = wCat,
                        style = wStyle,
                        icon = Icons.Default.Scale,
                        modifier = Modifier.weight(1f)
                    )
                    FamiliarKPICard(
                        title = "Frecuencia Cardíaca",
                        value = fText,
                        category = fCat,
                        style = fStyle,
                        icon = Icons.Default.Favorite,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun FamiliarKPICard(
    title: String,
    value: String,
    category: String,
    style: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val chipBgColor = when (style) {
        "normal" -> Color(0xFFE2F0D9)
        "warning" -> Color(0xFFFFF3E0)
        "alert" -> Color(0xFFFDE8E8)
        else -> Color(0xFFF1F1F1)
    }
    val chipTextColor = when (style) {
        "normal" -> Color(0xFF1B5E20)
        "warning" -> Color(0xFFE65100)
        "alert" -> Color(0xFFC62828)
        else -> Color(0xFF616161)
    }

    Card(
        modifier = modifier.height(115.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, null, tint = GuindaOficial, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    maxLines = 1
                )
            }

            Column {
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = GuindaOficial
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(chipBgColor)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = chipTextColor
                    )
                }
            }
        }
    }
}
