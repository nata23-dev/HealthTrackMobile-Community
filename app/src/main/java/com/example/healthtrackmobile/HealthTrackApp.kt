package com.example.healthtrackmobile

import android.app.Application
import com.example.healthtrackmobile.util.NotificationHelper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

class HealthTrackApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Crear canal de notificaciones
        NotificationHelper.createNotificationChannel(this)

        // Configurar persistencia local de Firestore
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(PersistentCacheSettings.newBuilder()
                .setSizeBytes(100 * 1024 * 1024) // 100 MB
                .build())
            .build()
        db.firestoreSettings = settings
    }
}
