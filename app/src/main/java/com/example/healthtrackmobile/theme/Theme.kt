package com.example.healthtrackmobile.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(primary = Guinda4T, secondary = Dorado4T, tertiary = VerdeSalud4T)

private val LightColorScheme =
  lightColorScheme(
    primary = GuindaOficial,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = DoradoOficial,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    tertiary = VerdeSalud4T,
    background = FondoClaro,
    surface = SuperficieBlanca,
    onBackground = TextoPrincipal,
    onSurface = TextoPrincipal,
  )

@Composable
fun HealthTrackMobileTheme(
  darkTheme: Boolean = false, // Forzar tema claro institucional 4T en todo momento
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = LightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

