package com.example.healthtrackmobile.receiver

import android.content.Context
import android.util.Log
import com.example.healthtrackmobile.model.RecordatorioMedicamento
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object ReminderSyncManager {
    suspend fun syncReminders(context: Context, userId: String) {
        try {
            val db = FirebaseFirestore.getInstance()
            val snapshot = db.collection("recordatorios_medicamentos")
                .whereEqualTo("paciente_id", userId)
                .whereEqualTo("estado", "activo")
                .get()
                .await()

            val reminders = snapshot.toObjects(RecordatorioMedicamento::class.java)
            
            // Mapear IDs
            snapshot.documents.forEachIndexed { index, doc ->
                if (index < reminders.size) {
                    reminders[index].id = doc.id
                }
            }

            Log.d("ReminderSyncManager", "Sincronizando ${reminders.size} recordatorios activos...")

            // Cancelar y volver a programar para evitar duplicidades
            for (reminder in reminders) {
                ReminderScheduler.scheduleReminder(context, reminder)
            }
        } catch (e: Exception) {
            Log.e("ReminderSyncManager", "Error al sincronizar recordatorios desde Firestore", e)
        }
    }
}
