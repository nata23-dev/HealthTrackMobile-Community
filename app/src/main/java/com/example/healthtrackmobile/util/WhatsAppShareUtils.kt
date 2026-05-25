package com.example.healthtrackmobile.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder

object WhatsAppShareUtils {
    fun compartirPorWhatsApp(context: Context, mensaje: String, telefono: String? = null) {
        val encodedMessage = URLEncoder.encode(mensaje, "UTF-8")
        val uriString = if (telefono.isNullOrBlank()) {
            "whatsapp://send?text=$encodedMessage"
        } else {
            val cleanPhone = telefono.replace("+", "").replace(" ", "")
            "https://api.whatsapp.com/send?phone=$cleanPhone&text=$encodedMessage"
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(uriString)
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Fallback: Menú genérico de compartir
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, mensaje)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Compartir reporte via"))
        }
    }
}
