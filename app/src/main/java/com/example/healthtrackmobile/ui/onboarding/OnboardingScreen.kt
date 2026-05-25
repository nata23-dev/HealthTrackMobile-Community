package com.example.healthtrackmobile.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.HealthAndSafety
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
import com.example.healthtrackmobile.theme.Dorado4T
import com.example.healthtrackmobile.theme.Guinda4T
import com.example.healthtrackmobile.util.OnboardingManager
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> 
        // Continuar independientemente del resultado para no bloquear el flujo
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> OnboardingPage(
                        title = "Bienvenido a HealthTrack",
                        description = "Estrategia Nacional de Monitoreo Crónico Inteligente.",
                        icon = Icons.Default.HealthAndSafety,
                        institution = "GOBIERNO DE MÉXICO"
                    )
                    1 -> OnboardingPage(
                        title = "Prevención con IA",
                        description = "Analizamos el clima, calidad del aire y alertas epidemiológicas para proteger su salud en tiempo real.",
                        icon = Icons.Default.Psychology
                    )
                    2 -> OnboardingPage(
                        title = "Alertas Médicas",
                        description = "Reciba recordatorios precisos de sus medicamentos y avisos preventivos importantes.",
                        icon = Icons.Default.NotificationsActive,
                        isLast = true,
                        onPermissionRequest = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    )
                }
            }

            // Indicadores y Botones
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicadores
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (pagerState.currentPage == index) Guinda4T else Color.LightGray)
                        )
                    }
                }

                Button(
                    onClick = {
                        if (pagerState.currentPage < 2) {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        } else {
                            scope.launch {
                                OnboardingManager.setOnboardingCompleted(context)
                                onFinished()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Guinda4T)
                ) {
                    Text(if (pagerState.currentPage == 2) "COMENZAR" else "CONTINUAR")
                }
            }
        }
    }
}

@Composable
fun OnboardingPage(
    title: String,
    description: String,
    icon: ImageVector,
    institution: String? = null,
    isLast: Boolean = false,
    onPermissionRequest: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (institution != null) {
            Text(
                text = institution,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Dorado4T,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(24.dp))
        }

        Box(
            modifier = Modifier
                .size(140.dp)
                .background(Guinda4T.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Guinda4T
            )
        }

        Spacer(Modifier.height(48.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Guinda4T,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        if (isLast) {
            Spacer(Modifier.height(32.dp))
            OutlinedButton(
                onClick = { onPermissionRequest?.invoke() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Guinda4T),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(brush = androidx.compose.ui.graphics.SolidColor(Guinda4T))
            ) {
                Text("HABILITAR ALERTAS MÉDICAS")
            }
        }
    }
}
