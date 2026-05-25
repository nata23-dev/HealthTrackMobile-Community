package com.example.healthtrackmobile.ui.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthtrackmobile.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilClinicoScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    isEmbedded: Boolean = false,
    onProfileSaved: () -> Unit = {},
    viewModel: PerfilClinicoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var fechaNacimiento by remember { mutableStateOf("") }
    var estaturaStr by remember { mutableStateOf("") }
    var pesoInicialStr by remember { mutableStateOf("") }
    var grupoSanguineo by remember { mutableStateOf("O+") }
    var alergias by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    
    // Antecedentes states
    var diabetesChecked by remember { mutableStateOf(false) }
    var hipertensionChecked by remember { mutableStateOf(false) }
    var asmaChecked by remember { mutableStateOf(false) }
    var ningunaChecked by remember { mutableStateOf(true) }

    // Cargar perfil al iniciar
    LaunchedEffect(userId) {
        viewModel.cargarPerfil(userId)
    }

    // Rellenar campos al recibir perfil exitosamente
    LaunchedEffect(uiState) {
        if (uiState is PerfilClinicoUiState.Success) {
            val perfil = (uiState as PerfilClinicoUiState.Success).perfil
            fechaNacimiento = perfil.fechaNacimiento ?: ""
            estaturaStr = if (perfil.estatura > 0) perfil.estatura.toString() else ""
            pesoInicialStr = if (perfil.pesoInicial > 0) perfil.pesoInicial.toString() else ""
            grupoSanguineo = perfil.grupoSanguineo ?: "O+"
            alergias = perfil.alergias ?: ""
            direccion = perfil.direccion ?: ""
            
            val ant = perfil.antecedentes ?: "Ninguna"
            diabetesChecked = ant.contains("Diabetes")
            hipertensionChecked = ant.contains("Hipertensión") || ant.contains("Hipertension")
            asmaChecked = ant.contains("Asma")
            ningunaChecked = ant.contains("Ninguna") || ant.isBlank() || ant == "Ninguno"
        } else if (uiState is PerfilClinicoUiState.SavedSuccess) {
            viewModel.clearState()
            android.widget.Toast.makeText(context, "Ficha clínica guardada con éxito", android.widget.Toast.LENGTH_SHORT).show()
            onProfileSaved()
            if (!isEmbedded) {
                onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            if (!isEmbedded) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Ficha Clínica",
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
            }
        },
        containerColor = Fondo4T,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        if (uiState is PerfilClinicoUiState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Guinda4T)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Institucional
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "GOBIERNO DE MÉXICO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Guinda4T,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Estrategia Nacional de Monitoreo Crónico",
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

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Datos Clínicos Generales",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Guinda4T
                        )

                        // Fecha Nacimiento
                        OutlinedTextField(
                            value = fechaNacimiento,
                            onValueChange = { 
                                fechaNacimiento = it
                                viewModel.clearState()
                            },
                            label = { Text("Fecha de Nacimiento (YYYY-MM-DD)") },
                            placeholder = { Text("ej: 1990-05-24") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Guinda4T,
                                focusedLabelColor = Guinda4T,
                                cursorColor = Guinda4T
                            )
                        )

                        // Estatura
                        OutlinedTextField(
                            value = estaturaStr,
                            onValueChange = { 
                                estaturaStr = it
                                viewModel.clearState()
                            },
                            label = { Text("Estatura (cm)") },
                            placeholder = { Text("ej: 175.5") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Guinda4T,
                                focusedLabelColor = Guinda4T,
                                cursorColor = Guinda4T
                            )
                        )

                        // Peso Inicial
                        OutlinedTextField(
                            value = pesoInicialStr,
                            onValueChange = { 
                                pesoInicialStr = it
                                viewModel.clearState()
                            },
                            label = { Text("Peso Inicial (kg)") },
                            placeholder = { Text("ej: 70.2") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Guinda4T,
                                focusedLabelColor = Guinda4T,
                                cursorColor = Guinda4T
                            )
                        )

                        // Grupo Sanguíneo (Exponer dropdown de manera simple o chips selectores)
                        var expanded by remember { mutableStateOf(false) }
                        val grupos = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = grupoSanguineo,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Grupo Sanguíneo") },
                                trailingIcon = {
                                    IconButton(onClick = { expanded = !expanded }) {
                                        Icon(
                                            imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                            contentDescription = "Expandir"
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expanded = !expanded },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Guinda4T,
                                    focusedLabelColor = Guinda4T
                                )
                            )
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                grupos.forEach { grupo ->
                                    DropdownMenuItem(
                                        text = { Text(grupo) },
                                        onClick = {
                                            grupoSanguineo = grupo
                                            expanded = false
                                            viewModel.clearState()
                                        }
                                    )
                                }
                            }
                        }

                        // Alergias
                        OutlinedTextField(
                            value = alergias,
                            onValueChange = { alergias = it },
                            label = { Text("Alergias") },
                            placeholder = { Text("ej: Penicilina, Nueces, etc. / Ninguna") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Guinda4T,
                                focusedLabelColor = Guinda4T,
                                cursorColor = Guinda4T
                            )
                        )

                        // Dirección
                        OutlinedTextField(
                            value = direccion,
                            onValueChange = { direccion = it },
                            label = { Text("Dirección") },
                            placeholder = { Text("ej: Av. Reforma 123, CDMX") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Guinda4T,
                                focusedLabelColor = Guinda4T,
                                cursorColor = Guinda4T
                            )
                        )
                    }
                }

                // Antecedentes Médicos (Card)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Antecedentes Médicos",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Guinda4T,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Diabetes
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    diabetesChecked = !diabetesChecked
                                    if (diabetesChecked) ningunaChecked = false
                                    viewModel.clearState()
                                }
                        ) {
                            Checkbox(
                                checked = diabetesChecked,
                                onCheckedChange = {
                                    diabetesChecked = it
                                    if (it) ningunaChecked = false
                                    viewModel.clearState()
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Guinda4T)
                            )
                            Text("Diabetes")
                        }

                        // Hipertensión
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    hipertensionChecked = !hipertensionChecked
                                    if (hipertensionChecked) ningunaChecked = false
                                    viewModel.clearState()
                                }
                        ) {
                            Checkbox(
                                checked = hipertensionChecked,
                                onCheckedChange = {
                                    hipertensionChecked = it
                                    if (it) ningunaChecked = false
                                    viewModel.clearState()
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Guinda4T)
                            )
                            Text("Hipertensión")
                        }

                        // Asma
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    asmaChecked = !asmaChecked
                                    if (asmaChecked) ningunaChecked = false
                                    viewModel.clearState()
                                }
                        ) {
                            Checkbox(
                                checked = asmaChecked,
                                onCheckedChange = {
                                    asmaChecked = it
                                    if (it) ningunaChecked = false
                                    viewModel.clearState()
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Guinda4T)
                            )
                            Text("Asma")
                        }

                        // Ninguna
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    ningunaChecked = !ningunaChecked
                                    if (ningunaChecked) {
                                        diabetesChecked = false
                                        hipertensionChecked = false
                                        asmaChecked = false
                                    }
                                    viewModel.clearState()
                                }
                        ) {
                            Checkbox(
                                checked = ningunaChecked,
                                onCheckedChange = {
                                    ningunaChecked = it
                                    if (it) {
                                        diabetesChecked = false
                                        hipertensionChecked = false
                                        asmaChecked = false
                                    }
                                    viewModel.clearState()
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Guinda4T)
                            )
                            Text("Ninguno / Ninguna")
                        }
                    }
                }

                // Mensajes de Error
                if (uiState is PerfilClinicoUiState.Error) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = (uiState as PerfilClinicoUiState.Error).message,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Botón Guardar
                Button(
                    onClick = {
                        val listAnt = mutableListOf<String>()
                        if (diabetesChecked) listAnt.add("Diabetes")
                        if (hipertensionChecked) listAnt.add("Hipertensión")
                        if (asmaChecked) listAnt.add("Asma")
                        if (ningunaChecked || listAnt.isEmpty()) listAnt.add("Ninguna")
                        
                        val antStr = listAnt.joinToString(", ")

                        viewModel.guardarPerfil(
                            userId = userId,
                            grupoSanguineo = grupoSanguineo,
                            alergias = alergias,
                            fechaNacimiento = fechaNacimiento,
                            direccion = direccion,
                            estaturaStr = estaturaStr,
                            pesoInicialStr = pesoInicialStr,
                            antecedentes = antStr
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Guinda4T,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Guardar",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Guardar Ficha Clínica",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
