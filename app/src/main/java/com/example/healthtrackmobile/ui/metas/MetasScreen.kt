package com.example.healthtrackmobile.ui.metas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthtrackmobile.model.Meta
import com.example.healthtrackmobile.theme.Guinda4T
import com.example.healthtrackmobile.theme.Fondo4T
import com.example.healthtrackmobile.theme.Dorado4T
import com.example.healthtrackmobile.util.shimmerEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetasScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    viewModel: MetasViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(userId) {
        viewModel.cargarMetas(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Metas de Salud", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Guinda4T)
            )
        },
        containerColor = Fondo4T
    ) { padding ->
        if (state.isLoading) {
            MetasShimmer(padding)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (state.metas.isEmpty()) {
                    item {
                        Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Aún no tienes metas registradas", color = Color.Gray)
                        }
                    }
                } else {
                    items(state.metas) { meta ->
                        MetaCard(meta)
                    }
                }
            }
        }
    }
}

@Composable
fun MetaCard(meta: Meta) {
    val progress = remember(meta.valorInicial, meta.valorActual, meta.objetivoNumerico) {
        val vIni = meta.valorInicial
        val vAct = meta.valorActual
        val vObj = meta.objetivoNumerico
        
        val totalRange = vObj - vIni
        if (totalRange == 0.0) 0f
        else {
            val progressDone = vAct - vIni
            (progressDone / totalRange).coerceIn(0.0, 1.0).toFloat()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = meta.titulo ?: "Meta de Salud",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Guinda4T
            )
            Spacer(Modifier.height(12.dp))
            
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Progreso: ${(progress * 100).toInt()}%", fontSize = 14.sp, color = Color.Gray)
                Text("${meta.valorActual} / ${meta.objetivoNumerico} ${meta.unidad ?: ""}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            
            Spacer(Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = Guinda4T,
                trackColor = Dorado4T.copy(alpha = 0.3f)
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = if (progress >= 1f) "¡Meta Alcanzada!" else "Sigue así, falta poco para tu objetivo.",
                fontSize = 12.sp,
                color = if (progress >= 1f) Color(0xFF1B5E20) else Color.Gray,
                fontWeight = if (progress >= 1f) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun MetasShimmer(padding: PaddingValues) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(4) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .shimmerEffect()
            )
        }
    }
}
