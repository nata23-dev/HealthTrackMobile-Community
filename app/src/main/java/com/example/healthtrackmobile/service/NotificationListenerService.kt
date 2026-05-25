package com.example.healthtrackmobile.service

import android.content.Context
import com.example.healthtrackmobile.util.NotificationHelper
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

object NotificationListenerService {
    private var mensajesListener: ListenerRegistration? = null
    private var alertasListener: ListenerRegistration? = null
    private var notificacionesListener: ListenerRegistration? = null

    fun startListening(context: Context, userId: String) {
        stopListening() // Evitar duplicados
        val db = FirebaseFirestore.getInstance()

        // Escuchar nuevos mensajes del médico
        mensajesListener = db.collection("recomendaciones")
            .whereEqualTo("pacienteId", userId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                
                snapshots?.documentChanges?.forEach { dc ->
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val mensaje = dc.document.getString("mensaje") ?: "Tiene una nueva recomendación médica"
                        val medico = dc.document.getString("medicoNombre") ?: "Su médico"
                        
                        // Solo notificar si el timestamp es reciente
                        val timestamp = dc.document.getLong("fechaEnvio") ?: 0L
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

        // Escuchar alertas de IA
        alertasListener = db.collection("alertas_ia")
            .whereEqualTo("pacienteId", userId)
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

        // Escuchar notificaciones generales de la base de datos
        notificacionesListener = db.collection("notificaciones")
            .whereEqualTo("usuarioId", userId)
            .addSnapshotListener { snapshots, e ->
                if (e != null) return@addSnapshotListener
                
                snapshots?.documentChanges?.forEach { dc ->
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val titulo = dc.document.getString("titulo") ?: "Nueva Notificación"
                        val mensaje = dc.document.getString("mensaje") ?: "Tiene un nuevo aviso"
                        
                        val date = dc.document.getDate("fechaCreacion")
                        val timestamp = date?.time ?: 0L
                        
                        if (System.currentTimeMillis() - timestamp < 60000) {
                            NotificationHelper.mostrarNotificacion(
                                context,
                                titulo,
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
        notificacionesListener?.remove()
        mensajesListener = null
        alertasListener = null
        notificacionesListener = null
    }
}
