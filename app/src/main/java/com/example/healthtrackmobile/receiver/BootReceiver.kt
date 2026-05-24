package com.example.healthtrackmobile.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.healthtrackmobile.util.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        Log.d("BootReceiver", "Teléfono encendido. Sincronizando alarmas de medicamentos...")

        val userId = SessionManager.getUserId(context)
        if (userId.isNullOrBlank()) {
            Log.d("BootReceiver", "No hay sesión de usuario activa. No se programan alarmas.")
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ReminderSyncManager.syncReminders(context, userId)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
