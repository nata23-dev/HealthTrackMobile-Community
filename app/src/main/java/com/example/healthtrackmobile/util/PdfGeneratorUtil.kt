package com.example.healthtrackmobile.util

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.healthtrackmobile.model.Metrica
import com.example.healthtrackmobile.model.Recomendacion
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfGeneratorUtil {

    private val guindaColor = Color.parseColor("#621132")
    private val doradoColor = Color.parseColor("#D4C19C")

    fun generarReporteClinico(
        context: Context,
        nombrePaciente: String,
        folioHT: String,
        metricas: List<Metrica>,
        recomendaciones: List<Recomendacion>,
        alertas: List<String>
    ): Boolean {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(612, 792, 1).create() // Carta (8.5 x 11 in @ 72 dpi)
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()
        val textPaint = Paint().apply {
            isAntiAlias = true
            textSize = 12f
        }

        // 1. Encabezado
        paint.color = guindaColor
        canvas.drawRect(300f, 40f, 582f, 90f, paint)
        
        textPaint.color = Color.WHITE
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 10f
        canvas.drawText("GOBIERNO DE MÉXICO | HT Community", 310f, 60f, textPaint)
        textPaint.typeface = Typeface.DEFAULT
        canvas.drawText("Secretaría de Salud | Monitoreo Crónico", 310f, 75f, textPaint)

        // 2. Título
        textPaint.color = guindaColor
        textPaint.textSize = 18f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Ficha Clínica de Monitoreo Crónico", 150f, 130f, textPaint)

        // 3. Metadatos
        textPaint.color = Color.BLACK
        textPaint.textSize = 11f
        textPaint.typeface = Typeface.DEFAULT
        canvas.drawText("Paciente: $nombrePaciente", 40f, 170f, textPaint)
        
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        canvas.drawText("Fecha de Emisión: ${sdf.format(Date())}", 380f, 170f, textPaint)
        canvas.drawText("Folio HT: HT-${folioHT.uppercase()}", 40f, 185f, textPaint)

        // 4. Tabla de Mediciones
        var yPos = 220f
        paint.color = guindaColor
        canvas.drawRect(40f, yPos, 572f, yPos + 25f, paint)
        
        textPaint.color = Color.WHITE
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Fecha", 45f, yPos + 17f, textPaint)
        canvas.drawText("Presión (mmHg)", 140f, yPos + 17f, textPaint)
        canvas.drawText("Ritmo (LPM)", 260f, yPos + 17f, textPaint)
        canvas.drawText("Glucosa", 360f, yPos + 17f, textPaint)
        canvas.drawText("Peso", 480f, yPos + 17f, textPaint)

        yPos += 30f
        textPaint.color = Color.BLACK
        textPaint.typeface = Typeface.DEFAULT
        val tableSdf = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        
        metricas.take(10).forEach { m ->
            canvas.drawText(tableSdf.format(Date(m.timestamp)), 45f, yPos + 15f, textPaint)
            if (m.tipo == "PRESION") {
                canvas.drawText("${m.valor.toInt()}/${m.valorSecundario.toInt()}", 140f, yPos + 15f, textPaint)
            } else if (m.tipo == "FRECUENCIA" || m.tipo == "FRECUENCIA_CARDIACA") {
                canvas.drawText("${m.valor.toInt()}", 260f, yPos + 15f, textPaint)
            } else if (m.tipo == "GLUCOSA") {
                canvas.drawText("${m.valor.toInt()}", 360f, yPos + 15f, textPaint)
            } else if (m.tipo == "PESO") {
                canvas.drawText("${m.valor}", 480f, yPos + 15f, textPaint)
            }
            
            paint.color = doradoColor
            paint.strokeWidth = 0.5f
            canvas.drawLine(40f, yPos + 20f, 572f, yPos + 20f, paint)
            yPos += 25f
        }

        // 5. Gráficos de Tendencia (Sparklines)
        yPos += 20f
        textPaint.color = guindaColor
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Tendencia de Glucosa y Presión", 40f, yPos, textPaint)
        yPos += 10f
        
        val graphPaint = Paint().apply {
            color = guindaColor
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        
        // Mini chart Glucosa
        val glucoseData = metricas.filter { it.tipo == "GLUCOSA" }.take(5).reversed()
        if (glucoseData.isNotEmpty()) {
            val path = Path()
            glucoseData.forEachIndexed { i, m ->
                val x = 40f + (i * 40f)
                val y = yPos + 60f - (m.valor.toFloat() / 600f * 50f)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            canvas.drawPath(path, graphPaint)
        }
        
        // 6. Alertas e Indicaciones
        yPos += 80f
        textPaint.color = guindaColor
        canvas.drawText("Alertas Preventivas Activas (IA)", 40f, yPos, textPaint)
        yPos += 20f
        textPaint.color = Color.BLACK
        textPaint.typeface = Typeface.DEFAULT
        alertas.forEach { alerta ->
            canvas.drawText("• $alerta", 50f, yPos, textPaint)
            yPos += 15f
        }

        yPos += 20f
        textPaint.color = guindaColor
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Indicaciones Médicas", 40f, yPos, textPaint)
        yPos += 20f
        textPaint.color = Color.BLACK
        textPaint.typeface = Typeface.DEFAULT
        recomendaciones.take(3).forEach { rec ->
            canvas.drawText("• ${rec.mensaje}", 50f, yPos, textPaint)
            yPos += 15f
        }

        // 7. Pie de Página
        paint.color = guindaColor
        canvas.drawRect(0f, 750f, 612f, 792f, paint)
        textPaint.color = Color.WHITE
        textPaint.textSize = 9f
        canvas.drawText("© 2026 Secretaría de Salud - HT Community", 200f, 775f, textPaint)

        pdfDocument.finishPage(page)

        // Guardar archivo
        return savePdfToDownloads(context, pdfDocument, "Reporte_Salud_${nombrePaciente.replace(" ", "_")}.pdf")
    }

    private fun savePdfToDownloads(context: Context, document: PdfDocument, fileName: String): Boolean {
        return try {
            val contentResolver = context.contentResolver
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }

                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    val outputStream: OutputStream? = contentResolver.openOutputStream(it)
                    outputStream?.use { stream ->
                        document.writeTo(stream)
                    }
                    document.close()
                    true
                } ?: false
            } else {
                // Fallback para Android < 10
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = java.io.File(downloadsDir, fileName)
                val outputStream = java.io.FileOutputStream(file)
                document.writeTo(outputStream)
                outputStream.close()
                document.close()
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
