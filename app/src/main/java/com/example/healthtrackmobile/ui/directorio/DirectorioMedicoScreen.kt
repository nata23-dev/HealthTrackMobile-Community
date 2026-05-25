package com.example.healthtrackmobile.ui.directorio

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthtrackmobile.model.ClinicaHospital
import com.example.healthtrackmobile.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectorioMedicoScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    isEmbedded: Boolean = false,
    viewModel: DirectorioMedicoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.cargarDirectorio()
    }

    Scaffold(
        topBar = {
            if (!isEmbedded) {
                TopAppBar(
                    title = { Text("Directorio Médico", color = Color.White) },
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
                Text("Directorio de Hospitales y Unidades Médicas", fontSize = 13.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Dorado4T))
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            when (val state = uiState) {
                is DirectorioUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Guinda4T)
                    }
                }
                is DirectorioUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                            items(state.clinicas) { hospital ->
                                HospitalCard(
                                    hospital = hospital,
                                    onCall = { tel ->
                                        try {
                                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                                data = Uri.parse("tel:$tel")
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // Fallback o mensaje
                                        }
                                    },
                                    onMap = { nombre ->
                                        try {
                                            val query = Uri.encode("$nombre Celaya, Guanajuato")
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                data = Uri.parse("geo:0,0?q=$query")
                                            }
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // Fallback si no hay app de mapas
                                        }
                                    }
                                )
                            }
                    }
                }
                is DirectorioUiState.Error -> {
                    Text("Error al cargar el directorio", color = Color.Red, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}

@Composable
fun HospitalCard(
    hospital: ClinicaHospital,
    onCall: (String) -> Unit,
    onMap: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(hospital.nombre ?: "", fontWeight = FontWeight.Bold, color = Guinda4T, fontSize = 16.sp)
                Text(hospital.direccion ?: "", fontSize = 13.sp, color = Color.DarkGray)
                Text(hospital.emailContacto ?: "", fontSize = 12.sp, color = Dorado4T, fontWeight = FontWeight.SemiBold)
            }
            IconButton(onClick = { hospital.telefono?.let { onCall(it) } }) {
                Icon(Icons.Default.Phone, "Llamar", tint = VerdeSalud4T)
            }
            IconButton(onClick = { hospital.nombre?.let { onMap(it) } }) {
                Icon(Icons.Default.Map, "Mapa", tint = Guinda4T)
            }
        }
    }
}
