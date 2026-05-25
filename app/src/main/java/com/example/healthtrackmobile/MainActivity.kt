package com.example.healthtrackmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.healthtrackmobile.receiver.SyncRemindersWorker
import com.example.healthtrackmobile.service.NotificationListenerService
import com.example.healthtrackmobile.theme.HealthTrackMobileTheme
import com.example.healthtrackmobile.util.SessionManager

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val userId = SessionManager.getUserId(this)
    if (userId != null) {
      SyncRemindersWorker.schedule(this, userId)
      NotificationListenerService.startListening(this, userId)
    }

    enableEdgeToEdge()
    setContent {
      HealthTrackMobileTheme { Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { MainNavigation() } }
    }
  }
}
