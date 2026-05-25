package com.example.healthtrackmobile

import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.healthtrackmobile.ui.dashboard.DashboardScreen
import com.example.healthtrackmobile.ui.login.LoginScreen
import com.example.healthtrackmobile.ui.login.RegisterScreen
import com.example.healthtrackmobile.ui.metricas.AgregarMetricasScreen
import com.example.healthtrackmobile.ui.perfil.PerfilClinicoScreen
import com.example.healthtrackmobile.ui.directorio.DirectorioMedicoScreen
import com.example.healthtrackmobile.ui.citas.CitasScreen
import com.example.healthtrackmobile.ui.prevencion.PrevencionScreen
import com.example.healthtrackmobile.ui.medicamentos.MedicamentosScreen
import com.example.healthtrackmobile.ui.metas.MetasScreen
import com.example.healthtrackmobile.ui.reportes.ReportesScreen
import com.example.healthtrackmobile.ui.onboarding.OnboardingScreen
import com.example.healthtrackmobile.ui.onboarding.ConfiguracionInicialScreen
import com.example.healthtrackmobile.ui.tendencias.TendenciasScreen
import com.example.healthtrackmobile.util.SessionManager
import com.example.healthtrackmobile.util.OnboardingManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

@Composable
fun MainNavigation() {
  val context = LocalContext.current
  var currentUserId by remember { mutableStateOf(SessionManager.getUserId(context)) }
  var currentUserName by remember { mutableStateOf(SessionManager.getUserName(context)) }
  val initialKey = remember(currentUserId, currentUserName) {
    if (!currentUserId.isNullOrEmpty() && !currentUserName.isNullOrEmpty()) {
      Main(userId = currentUserId!!, userName = currentUserName!!)
    } else {
      Login
    }
  }
  val backStack = rememberNavBackStack(initialKey)
  val coroutineScope = rememberCoroutineScope()

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Login> {
          val context = LocalContext.current
          LoginScreen(
            onLoginSuccess = { usuario ->
              SessionManager.saveSession(context, usuario.id ?: "", usuario.nombre ?: "")
              currentUserId = usuario.id
              currentUserName = usuario.nombre
              coroutineScope.launch {
                com.example.healthtrackmobile.receiver.ReminderSyncManager.syncReminders(context, usuario.id ?: "")
                com.example.healthtrackmobile.service.NotificationListenerService.startListening(context, usuario.id ?: "")
                
                // Smart Login Logic: Bypass Onboarding if profile exists
                try {
                    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val profileDoc = db.collection("perfiles_pacientes").document(usuario.id ?: "").get().await()
                    
                    if (profileDoc.exists()) {
                        // Veteran user: Go to Dashboard
                        backStack.clear()
                        backStack.add(Main(userId = usuario.id ?: "", userName = usuario.nombre ?: ""))
                    } else {
                        // New user: Go to Onboarding tunnel
                        backStack.clear()
                        backStack.add(Onboarding)
                    }
                } catch (e: Exception) {
                    // Fallback to Onboarding in case of error during check
                    backStack.clear()
                    backStack.add(Onboarding)
                }
              }
            },
            onRegisterClick = {
              backStack.add(Register)
            },
            modifier = Modifier.safeDrawingPadding()
          )
        }
        entry<Register> {
          val context = LocalContext.current
          RegisterScreen(
            onRegisterSuccess = { usuario ->
              SessionManager.saveSession(context, usuario.id ?: "", usuario.nombre ?: "")
              currentUserId = usuario.id
              currentUserName = usuario.nombre
              coroutineScope.launch {
                com.example.healthtrackmobile.receiver.ReminderSyncManager.syncReminders(context, usuario.id ?: "")
                com.example.healthtrackmobile.service.NotificationListenerService.startListening(context, usuario.id ?: "")
                // New user: Go directly to Onboarding
                backStack.clear()
                backStack.add(Onboarding)
              }
            },
            onNavigateToLogin = {
              backStack.removeLastOrNull()
            },
            modifier = Modifier.safeDrawingPadding()
          )
        }
        entry<Main> { mainKey ->
          val context = LocalContext.current
          DashboardScreen(
            userId = mainKey.userId,
            userName = mainKey.userName,
            onLogout = {
              SessionManager.clearSession(context)
              currentUserId = null
              currentUserName = null
              backStack.clear()
              backStack.add(Login)
            },
            onAddMetricClick = {
              backStack.add(AgregarMetricas(userId = mainKey.userId))
            },
            onProfileClick = {
              backStack.add(PerfilClinico(userId = mainKey.userId))
            },
            onDirectoryClick = {
              backStack.add(DirectorioMedico)
            },
            onCitasClick = {
              backStack.add(CitasMedicas(userId = mainKey.userId, userName = mainKey.userName))
            },
            onTendenciasClick = {
              backStack.add(TendenciasDeSalud(userId = mainKey.userId))
            },
            onPrevencionClick = {
              backStack.add(PrevencionIA(userId = mainKey.userId))
            },
            onMedicamentosClick = {
              backStack.add(MisMedicamentos(userId = mainKey.userId))
            },
            onMetasClick = {
              backStack.add(MetasDeSalud(userId = mainKey.userId))
            },
            onReportesClick = {
              backStack.add(ReportesGenerales(userId = mainKey.userId))
            },
            modifier = Modifier.safeDrawingPadding()
          )
        }

        entry<AgregarMetricas> { key ->
          AgregarMetricasScreen(
            userId = key.userId,
            onNavigateBack = { backStack.removeLastOrNull() }
          )
        }
        entry<PerfilClinico> { key ->
          PerfilClinicoScreen(
            userId = key.userId,
            onNavigateBack = { backStack.removeLastOrNull() },
            modifier = Modifier.safeDrawingPadding()
          )
        }
        entry<DirectorioMedico> {
          DirectorioMedicoScreen(
            onNavigateBack = {
              backStack.removeLastOrNull()
            },
            modifier = Modifier.safeDrawingPadding()
          )
        }
        entry<CitasMedicas> { key ->
          CitasScreen(
            userId = key.userId,
            userName = key.userName,
            onNavigateBack = {
              backStack.removeLastOrNull()
            },
            modifier = Modifier.safeDrawingPadding()
          )
        }
        entry<PrevencionIA> { key ->
          PrevencionScreen(
            userId = key.userId,
            onNavigateBack = {
              backStack.removeLastOrNull()
            },
            modifier = Modifier.safeDrawingPadding()
          )
        }
        entry<MisMedicamentos> { key ->
          MedicamentosScreen(
            userId = key.userId,
            onNavigateBack = {
              backStack.removeLastOrNull()
            }
          )
        }
        entry<MetasDeSalud> { key ->
          MetasScreen(
            userId = key.userId,
            onNavigateBack = {
              backStack.removeLastOrNull()
            }
          )
        }
        entry<Onboarding> {
          val context = LocalContext.current
          OnboardingScreen(
            onFinished = {
                val userId = SessionManager.getUserId(context) ?: ""
                backStack.clear()
                backStack.add(ConfiguracionInicial(userId = userId))
            }
          )
        }
        entry<ConfiguracionInicial> { key ->
          val context = LocalContext.current
          ConfiguracionInicialScreen(
            userId = key.userId,
            onFinished = {
                val userName = SessionManager.getUserName(context) ?: ""
                currentUserId = key.userId
                currentUserName = userName
                backStack.clear()
                backStack.add(Main(userId = key.userId, userName = userName))
            }
          )
        }
        entry<ReportesGenerales> { key ->
          ReportesScreen(
            userId = key.userId,
            onNavigateBack = {
              backStack.removeLastOrNull()
            }
          )
        }
        entry<TendenciasDeSalud> { key ->
          val context = LocalContext.current
          TendenciasScreen(
            userId = key.userId,
            onNavigateBack = { backStack.removeLastOrNull() },
            onInicioClick = {
              val userName = SessionManager.getUserName(context) ?: "Usuario"
              backStack.clear()
              backStack.add(Main(userId = key.userId, userName = userName))
            },
            onCitasClick = {
              val userName = SessionManager.getUserName(context) ?: "Usuario"
              backStack.add(CitasMedicas(userId = key.userId, userName = userName))
            },
            onProfileClick = {
              backStack.add(PerfilClinico(userId = key.userId))
            }
          )
        }
      },
  )
}
