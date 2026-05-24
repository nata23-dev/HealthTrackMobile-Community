package com.example.healthtrackmobile.ui.citas

import android.app.DatePickerDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthtrackmobile.model.Cita
import com.example.healthtrackmobile.model.Usuario
import com.example.healthtrackmobile.theme.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitasScreen(
    userId: String,
    userName: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CitasViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val agendarState by viewModel.agendarState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Cargar datos al iniciar
    LaunchedEffect(userId) {
        viewModel.cargarDatos(userId)
    }

    // Manejar estado de agendamiento
    LaunchedEffect(agendarState) {
        when (agendarState) {
            is AgendarState.Success -> {
                Toast.makeText(context, "Cita agendada con éxito", Toast.LENGTH_SHORT).show()
                viewModel.resetAgendarState()
            }
            is AgendarState.Error -> {
                Toast.makeText(context, (agendarState as AgendarState.Error).message, Toast.LENGTH_LONG).show()
                viewModel.resetAgendarState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis Citas Médicas",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Guinda4T
                )
            )
        },
        containerColor = Fondo4T,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        when (val state = uiState) {
            is CitasUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Guinda4T)
                }
            }
            is CitasUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                }
            }
            is CitasUiState.Success -> {
                CitasContent(
                    pacienteId = userId,
                    pacienteNombre = userName,
                    citas = state.citas,
                    medicos = state.medicos,
                    paddingValues = paddingValues,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun CitasContent(
    pacienteId: String,
    pacienteNombre: String,
    citas: List<Cita>,
    medicos: List<Usuario>,
    paddingValues: PaddingValues,
    viewModel: CitasViewModel
) {
    val context = LocalContext.current

    // Variables de agendamiento
    var medicoSeleccionado by remember { mutableStateOf<Usuario?>(null) }
    var fechaSeleccionada by remember { mutableStateOf<LocalDate?>(null) }
    var horaSeleccionada by remember { mutableStateOf<String?>(null) }

    // Dropdowns
    var medicoDropdownExpanded by remember { mutableStateOf(false) }
    var horaDropdownExpanded by remember { mutableStateOf(false) }

    // Diálogos de confirmación de cancelación
    var citaParaCancelar by remember { mutableStateOf<Cita?>(null) }

    val slotsHora = listOf(
        "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
        "12:00", "12:30", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30",
        "16:00", "16:30", "17:00", "17:30"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
    ) {
        // Encabezado
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "GOBIERNO DE MÉXICO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Guinda4T,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Sistema Nacional de Citas Médicas",
                    fontSize = 13.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Dorado4T)
                )
            }
        }

        // Formulario para Agendar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Agendar Nueva Cita",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Guinda4T
                    )

                    // 1. Selector de Médico
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = medicoSeleccionado?.nombre ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Médico Asignado") },
                            placeholder = { Text("Selecciona un médico") },
                            trailingIcon = {
                                IconButton(onClick = { medicoDropdownExpanded = !medicoDropdownExpanded }) {
                                    Icon(
                                        imageVector = if (medicoDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = "Expandir"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { medicoDropdownExpanded = !medicoDropdownExpanded },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Guinda4T,
                                focusedLabelColor = Guinda4T
                            )
                        )
                        DropdownMenu(
                            expanded = medicoDropdownExpanded,
                            onDismissRequest = { medicoDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            if (medicos.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No tienes médicos asignados") },
                                    onClick = { medicoDropdownExpanded = false }
                                )
                            } else {
                                medicos.forEach { medico ->
                                    DropdownMenuItem(
                                        text = { Text(medico.nombre ?: "") },
                                        onClick = {
                                            medicoSeleccionado = medico
                                            medicoDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 2. Selector de Fecha
                    val fechaFormateada = remember(fechaSeleccionada) {
                        fechaSeleccionada?.let {
                            val localDate = it
                            val cal = Calendar.getInstance().apply {
                                set(localDate.year, localDate.monthValue - 1, localDate.dayOfMonth)
                            }
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.time)
                        } ?: ""
                    }

                    OutlinedTextField(
                        value = fechaFormateada,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Fecha de la Cita") },
                        placeholder = { Text("Seleccionar fecha") },
                        trailingIcon = {
                            IconButton(onClick = {
                                val today = Calendar.getInstance()
                                val picker = DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        fechaSeleccionada = LocalDate.of(year, month + 1, dayOfMonth)
                                    },
                                    today.get(Calendar.YEAR),
                                    today.get(Calendar.MONTH),
                                    today.get(Calendar.DAY_OF_MONTH)
                                )
                                picker.datePicker.minDate = System.currentTimeMillis() - 1000
                                picker.show()
                            }) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Seleccionar fecha"
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val today = Calendar.getInstance()
                                val picker = DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        fechaSeleccionada = LocalDate.of(year, month + 1, dayOfMonth)
                                    },
                                    today.get(Calendar.YEAR),
                                    today.get(Calendar.MONTH),
                                    today.get(Calendar.DAY_OF_MONTH)
                                )
                                picker.datePicker.minDate = System.currentTimeMillis() - 1000
                                picker.show()
                            },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Guinda4T,
                            focusedLabelColor = Guinda4T
                        )
                    )

                    // 3. Selector de Hora
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = horaSeleccionada ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Hora de la Cita") },
                            placeholder = { Text("Selecciona una hora") },
                            trailingIcon = {
                                IconButton(onClick = { horaDropdownExpanded = !horaDropdownExpanded }) {
                                    Icon(
                                        imageVector = if (horaDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = "Expandir"
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { horaDropdownExpanded = !horaDropdownExpanded },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Guinda4T,
                                focusedLabelColor = Guinda4T
                            )
                        )
                        DropdownMenu(
                            expanded = horaDropdownExpanded,
                            onDismissRequest = { horaDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            slotsHora.forEach { slot ->
                                DropdownMenuItem(
                                    text = { Text(slot) },
                                    onClick = {
                                        horaSeleccionada = slot
                                        horaDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Botón para Agendar
                    Button(
                        onClick = {
                            val med = medicoSeleccionado
                            val date = fechaSeleccionada
                            val hourStr = horaSeleccionada

                            if (med == null || date == null || hourStr == null) {
                                Toast.makeText(context, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val timeParts = hourStr.split(":")
                            val time = LocalTime.of(timeParts[0].toInt(), timeParts[1].toInt())
                            val localLdt = LocalDateTime.of(date, time)
                            val epochMillis = localLdt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                            viewModel.agendarCita(
                                pacienteId = pacienteId,
                                pacienteNombre = pacienteNombre,
                                medico = med,
                                fechaHoraEpoch = epochMillis
                            )

                            // Limpiar selección
                            medicoSeleccionado = null
                            fechaSeleccionada = null
                            horaSeleccionada = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Guinda4T,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Agendar")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Agendar Cita", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }

        // Listado de Citas
        item {
            Text(
                text = "Mis Citas Programadas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Guinda4T,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (citas.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "No tienes citas médicas programadas.",
                        color = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(citas) { cita ->
                CitaRowCard(
                    cita = cita,
                    onCancelarClick = { citaParaCancelar = cita }
                )
            }
        }
    }

    // Diálogo de Confirmación para Cancelar
    citaParaCancelar?.let { cita ->
        AlertDialog(
            onDismissRequest = { citaParaCancelar = null },
            title = { Text("Cancelar Cita Médica", fontWeight = FontWeight.Bold) },
            text = {
                val formatted = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(cita.fechaHora))
                Text("¿Estás seguro de que deseas cancelar tu cita con el Dr(a). ${cita.medicoNombre} programada para el $formatted?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelarCita(pacienteId, cita.id ?: "")
                        citaParaCancelar = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cancelar Cita", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { citaParaCancelar = null }) {
                    Text("Volver", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun CitaRowCard(
    cita: Cita,
    onCancelarClick: () -> Unit
) {
    val formattedDate = remember(cita.fechaHora) {
        val date = Date(cita.fechaHora)
        val sdf = SimpleDateFormat("dd 'de' MMMM 'de' yyyy - HH:mm", Locale.forLanguageTag("es-MX"))
        sdf.format(date)
    }

    val isPendiente = cita.estado == "PENDIENTE"
    val badgeContainerColor = if (isPendiente) Color(0xFFFEF3C7) else Color(0xFFD1FAE5)
    val badgeContentColor = if (isPendiente) Color(0xFF92400E) else Color(0xFF065F46)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Médico: ${cita.medicoNombre}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Guinda4T,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    color = badgeContainerColor,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = cita.estado ?: "PENDIENTE",
                        color = badgeContentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Fecha y hora",
                    tint = Dorado4T,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formattedDate,
                    fontSize = 13.sp,
                    color = Color.DarkGray
                )
            }

            if (isPendiente) {
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onCancelarClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFC81E1E)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF87171)),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Cancelar",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Cancelar Cita", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
