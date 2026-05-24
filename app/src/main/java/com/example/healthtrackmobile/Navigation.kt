package com.example.healthtrackmobile

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.healthtrackmobile.ui.dashboard.DashboardScreen
import com.example.healthtrackmobile.ui.login.LoginScreen
import com.example.healthtrackmobile.ui.metricas.AgregarMetricaScreen
import com.example.healthtrackmobile.ui.perfil.PerfilClinicoScreen
import com.example.healthtrackmobile.ui.directorio.DirectorioMedicoScreen
import com.example.healthtrackmobile.util.SessionManager
import kotlinx.coroutines.launch

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Login)
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
              coroutineScope.launch {
                com.example.healthtrackmobile.receiver.ReminderSyncManager.syncReminders(context, usuario.id ?: "")
              }
              backStack.add(Main(userId = usuario.id ?: "", userName = usuario.nombre ?: ""))
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
              backStack.add(Login)
            },
            onAddMetricClick = {
              backStack.add(AgregarMetrica(userId = mainKey.userId))
            },
            onProfileClick = {
              backStack.add(PerfilClinico(userId = mainKey.userId))
            },
            onDirectoryClick = {
              backStack.add(DirectorioMedico)
            },
            modifier = Modifier.safeDrawingPadding()
          )
        }
        entry<AgregarMetrica> { key ->
          AgregarMetricaScreen(
            userId = key.userId,
            onNavigateBack = {
              backStack.removeLastOrNull()
            },
            modifier = Modifier.safeDrawingPadding()
          )
        }
        entry<PerfilClinico> { key ->
          PerfilClinicoScreen(
            userId = key.userId,
            onNavigateBack = {
              backStack.removeLastOrNull()
            },
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
      },
  )
}
