package com.example.healthtrackmobile.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.healthtrackmobile.model.RecordatorioMedicamento
import java.text.SimpleDateFormat
import java.util.*

object ReminderScheduler {
    private const val TAG = "ReminderScheduler"

    fun scheduleReminder(context: Context, reminder: RecordatorioMedicamento) {
        if (reminder.estado != "activo") {
            cancelReminder(context, reminder.id)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val triggerTime = calculateNextTriggerTime(reminder) ?: return

        // Verificar si la fecha de disparo excede la fecha de fin
        if (isPastEndDate(triggerTime, reminder.fechaFin)) {
            Log.d(TAG, "El recordatorio ${reminder.id} ha excedido la fecha fin (${reminder.fechaFin}). No se programa.")
            cancelReminder(context, reminder.id)
            return
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("reminder_id", reminder.id)
            putExtra("med_name", reminder.medicamento)
            putExtra("dosis", reminder.dosis)
            putExtra("frecuencia", reminder.frecuencia)
            putExtra("fecha_fin", reminder.fechaFin)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            Log.d(TAG, "Programada alarma para ${reminder.medicamento} a las: ${Date(triggerTime)}")
        } catch (e: SecurityException) {
            Log.e(TAG, "Error de permisos para programar alarma exacta", e)
        }
    }

    fun cancelReminder(context: Context, reminderId: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId.hashCode(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(TAG, "Cancelado recordatorio $reminderId")
        }
    }

    private fun parseFrecuenciaToHours(frecuencia: String?): Int {
        val f = frecuencia?.lowercase() ?: return 24
        return when {
            f.contains("4 horas") -> 4
            f.contains("6 horas") -> 6
            f.contains("8 horas") -> 8
            f.contains("12 horas") -> 12
            f.contains("24 horas") || f.contains("una vez al día") || f.contains("diaria") -> 24
            f.contains("dos veces al día") -> 12
            f.contains("tres veces al día") -> 8
            else -> 24
        }
    }

    private fun calculateNextTriggerTime(reminder: RecordatorioMedicamento): Long? {
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())

        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        try {
            // Intentar leer la fecha de inicio. Si no hay, usar hoy.
            val startDate = reminder.fechaInicio?.let { sdfDate.parse(it) } ?: Date()
            calendar.time = startDate

            // Establecer la hora
            val timeStr = reminder.horaRecordatorio ?: "08:00"
            val time = sdfTime.parse(timeStr) ?: Date()
            val timeCal = Calendar.getInstance().apply { this.time = time }

            calendar.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
            calendar.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)

            val intervalHours = parseFrecuenciaToHours(reminder.frecuencia)
            val intervalMs = intervalHours * 60 * 60 * 1000L

            // Si el tiempo calculado ya pasó, sumar el intervalo hasta que sea a futuro
            while (calendar.timeInMillis <= now) {
                calendar.timeInMillis += intervalMs
            }

            return calendar.timeInMillis
        } catch (e: Exception) {
            Log.e(TAG, "Error al calcular el trigger time de recordatorio", e)
            return null
        }
    }

    private fun isPastEndDate(triggerTime: Long, fechaFin: String?): Boolean {
        if (fechaFin.isNullOrBlank()) return false
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            val endDate = sdfDate.parse(fechaFin) ?: return false
            // Ponemos el fin de fechaFin al final del día (23:59:59)
            val endCalendar = Calendar.getInstance().apply {
                this.time = endDate
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            triggerTime > endCalendar.timeInMillis
        } catch (e: Exception) {
            false
        }
    }
}
