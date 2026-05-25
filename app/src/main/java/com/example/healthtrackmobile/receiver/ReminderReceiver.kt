package com.example.healthtrackmobile.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.healthtrackmobile.MainActivity
import com.example.healthtrackmobile.model.RecordatorioMedicamento

class ReminderReceiver : BroadcastReceiver() {
    private val CHANNEL_ID = "medication_reminders"
    private val CHANNEL_NAME = "Recordatorios de Medicamentos"

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        val reminderId = intent.getStringExtra("reminder_id") ?: return
        val medName = intent.getStringExtra("med_name") ?: "Medicamento"
        val dosis = intent.getStringExtra("dosis") ?: ""
        val frecuencia = intent.getStringExtra("frecuencia") ?: ""
        val fechaFin = intent.getStringExtra("fecha_fin")

        Log.d("ReminderReceiver", "¡Alarma disparada para $medName ($dosis)!")

        // 1. Mostrar notificación al usuario
        mostrarNotificacion(context, medName, dosis)

        // 2. Programar la siguiente ocurrencia de la alarma
        val reminder = RecordatorioMedicamento(
            id = reminderId,
            medicamento = medName,
            dosis = dosis,
            frecuencia = frecuencia,
            fechaFin = fechaFin,
            estado = "activo"
        )
        ReminderScheduler.scheduleReminder(context, reminder)
    }

    private fun mostrarNotificacion(context: Context, medName: String, dosis: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal de notificación para Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones para recordatorios de toma de medicamentos"
                enableLights(true)
                lightColor = 0xFF621132.toInt()
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Acción al pulsar la notificación: abrir MainActivity
        val openIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            medName.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Hora de tu medicamento: $medName")
            .setContentText("Dosis: $dosis")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(0xFF621132.toInt()) // Guinda institucional
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(medName.hashCode(), notification)
    }
}
