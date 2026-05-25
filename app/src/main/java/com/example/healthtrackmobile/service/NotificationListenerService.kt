package com.example.healthtrackmobile.service

import android.content.Context
import com.example.healthtrackmobile.util.NotificationHelper
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

object NotificationListenerService {
    private var mensajesListener: ListenerRegistration? = null
    private var alertasListener: ListenerRegistration? = null

    fun startListening(context: Context, userId: String) {
        stopListening() // Evitar duplicados
        val db = FirebaseFirestore.getInstance()

        // Escuchar nuevos mensajes del médico
        mensajesListener = db.collection("recomendaciones")
            .whereEqualTo("paciente_id", userId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                
                snapshots?.documentChanges?.forEach { dc ->
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val mensaje = dc.document.getString("mensaje") ?: "Tiene una nueva recomendación médica"
                        val medico = dc.document.getString("medico_nombre") ?: "Su médico"
                        
                        // Solo notificar si el timestamp es reciente (opcional, pero recomendado)
                        val timestamp = dc.document.getLong("fecha_envio") ?: 0L
                        if (System.currentTimeMillis() - timestamp < 60000) {
                             NotificationHelper.mostrarNotificacion(
                                context,
                                "Nuevo mensaje de $medico",
                                mensaje
                            )
                        }
                    }
                }
            }

        // Escuchar alertas de IA (si existe esa colección, si no usamos recomendaciones de tipo IA)
        alertasListener = db.collection("alertas_ia")
            .whereEqualTo("paciente_id", userId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                
                snapshots?.documentChanges?.forEach { dc ->
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val mensaje = dc.document.getString("mensaje") ?: "Nueva alerta preventiva detectada"
                        
                        val timestamp = dc.document.getLong("timestamp") ?: 0L
                        if (System.currentTimeMillis() - timestamp < 60000) {
                            NotificationHelper.mostrarNotificacion(
                                context,
                                "Alerta Preventiva IA",
                                mensaje
                            )
                        }
                    }
                }
            }
    }

    fun stopListening() {
        mensajesListener?.remove()
        alertasListener?.remove()
        mensajesListener = null
        alertasListener = null
    }
}
