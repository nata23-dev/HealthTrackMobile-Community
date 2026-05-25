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
        
        metricas.take(5).forEach { m ->
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
        textPaint.textSize = 11f
        canvas.drawText("Gráficos de Tendencia de Salud (Últimos 5 registros)", 40f, yPos, textPaint)
        
        val boxTop = yPos + 10f
        val boxHeight = 90f
        val boxWidth = 246f
        
        // --- 5.1 Gráfico de Glucosa ---
        val box1Left = 40f
        val box1Right = box1Left + boxWidth
        
        // Fondo de la tarjeta de Glucosa
        paint.color = Color.parseColor("#F5F5F5")
        canvas.drawRoundRect(box1Left, boxTop, box1Right, boxTop + boxHeight, 8f, 8f, paint)
        
        // Título del gráfico 1
        textPaint.color = guindaColor
        textPaint.textSize = 9f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Glucosa (mg/dL)", box1Left + 10f, boxTop + 15f, textPaint)
        
        val glucoseData = metricas.filter { it.tipo == "GLUCOSA" }.take(5).reversed()
        if (glucoseData.isNotEmpty()) {
            val minG = glucoseData.map { it.valor }.minOrNull() ?: 70.0
            val maxG = glucoseData.map { it.valor }.maxOrNull() ?: 150.0
            val rangeG = if (maxG - minG == 0.0) 50.0 else maxG - minG
            
            val graphLeft = box1Left + 15f
            val graphRight = box1Right - 15f
            val graphTop = boxTop + 25f
            val graphBottom = boxTop + boxHeight - 15f
            
            val path = Path()
            val pointPaint = Paint().apply {
                color = guindaColor
                style = Paint.Style.FILL
            }
            val linePaint = Paint().apply {
                color = guindaColor
                style = Paint.Style.STROKE
                strokeWidth = 2f
                isAntiAlias = true
            }
            
            glucoseData.forEachIndexed { i, m ->
                val x = graphLeft + (i * (graphRight - graphLeft) / (glucoseData.size - 1).coerceAtLeast(1))
                val y = graphBottom - ((m.valor - minG) / rangeG * (graphBottom - graphTop)).toFloat()
                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
                canvas.drawCircle(x, y, 3f, pointPaint)
                
                // Mostrar valor del último punto
                if (i == glucoseData.size - 1) {
                    textPaint.color = Color.BLACK
                    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    textPaint.textSize = 8f
                    canvas.drawText("${m.valor.toInt()}", x - 10f, y - 8f, textPaint)
                }
            }
            canvas.drawPath(path, linePaint)
        } else {
            textPaint.color = Color.GRAY
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            textPaint.textSize = 9f
            canvas.drawText("Sin registros de glucosa", box1Left + 15f, boxTop + 50f, textPaint)
        }
        
        // --- 5.2 Gráfico de Presión Arterial ---
        val box2Left = 326f
        val box2Right = box2Left + boxWidth
        
        // Fondo de la tarjeta de Presión
        paint.color = Color.parseColor("#F5F5F5")
        canvas.drawRoundRect(box2Left, boxTop, box2Right, boxTop + boxHeight, 8f, 8f, paint)
        
        // Título del gráfico 2
        textPaint.color = guindaColor
        textPaint.textSize = 9f
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Presión Arterial (mmHg)", box2Left + 10f, boxTop + 15f, textPaint)
        
        val presionData = metricas.filter { it.tipo == "PRESION" }.take(5).reversed()
        if (presionData.isNotEmpty()) {
            val minP = minOf(
                presionData.map { it.valor }.minOrNull() ?: 80.0,
                presionData.map { it.valorSecundario }.minOrNull() ?: 60.0
            )
            val maxP = maxOf(
                presionData.map { it.valor }.maxOrNull() ?: 140.0,
                presionData.map { it.valorSecundario }.maxOrNull() ?: 90.0
            )
            val rangeP = if (maxP - minP == 0.0) 60.0 else maxP - minP
            
            val graphLeft = box2Left + 15f
            val graphRight = box2Right - 15f
            val graphTop = boxTop + 25f
            val graphBottom = boxTop + boxHeight - 15f
            
            val pathSystolic = Path()
            val pathDiastolic = Path()
            
            val pointSysPaint = Paint().apply {
                color = guindaColor
                style = Paint.Style.FILL
            }
            val lineSysPaint = Paint().apply {
                color = guindaColor
                style = Paint.Style.STROKE
                strokeWidth = 2f
                isAntiAlias = true
            }
            
            val pointDiaPaint = Paint().apply {
                color = doradoColor
                style = Paint.Style.FILL
            }
            val lineDiaPaint = Paint().apply {
                color = doradoColor
                style = Paint.Style.STROKE
                strokeWidth = 2f
                isAntiAlias = true
            }
            
            presionData.forEachIndexed { i, m ->
                val x = graphLeft + (i * (graphRight - graphLeft) / (presionData.size - 1).coerceAtLeast(1))
                val ySys = graphBottom - ((m.valor - minP) / rangeP * (graphBottom - graphTop)).toFloat()
                val yDia = graphBottom - ((m.valorSecundario - minP) / rangeP * (graphBottom - graphTop)).toFloat()
                
                if (i == 0) {
                    pathSystolic.moveTo(x, ySys)
                    pathDiastolic.moveTo(x, yDia)
                } else {
                    pathSystolic.lineTo(x, ySys)
                    pathDiastolic.lineTo(x, yDia)
                }
                
                canvas.drawCircle(x, ySys, 3f, pointSysPaint)
                canvas.drawCircle(x, yDia, 3f, pointDiaPaint)
                
                // Mostrar último valor
                if (i == presionData.size - 1) {
                    textPaint.color = Color.BLACK
                    textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    textPaint.textSize = 8f
                    canvas.drawText("${m.valor.toInt()}/${m.valorSecundario.toInt()}", x - 15f, ySys - 8f, textPaint)
                }
            }
            
            canvas.drawPath(pathSystolic, lineSysPaint)
            canvas.drawPath(pathDiastolic, lineDiaPaint)
        } else {
            textPaint.color = Color.GRAY
            textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            textPaint.textSize = 9f
            canvas.drawText("Sin registros de presión", box2Left + 15f, boxTop + 50f, textPaint)
        }
        
        yPos += boxHeight + 15f
        
        // 6. Alertas e Indicaciones
        yPos += 15f
        textPaint.color = guindaColor
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 11f
        canvas.drawText("Alertas Preventivas Activas (IA)", 40f, yPos, textPaint)
        yPos += 18f
        textPaint.color = Color.BLACK
        textPaint.typeface = Typeface.DEFAULT
        textPaint.textSize = 10f
        alertas.forEach { alerta ->
            canvas.drawText("• $alerta", 50f, yPos, textPaint)
            yPos += 14f
        }

        yPos += 10f
        textPaint.color = guindaColor
        textPaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textPaint.textSize = 11f
        canvas.drawText("Indicaciones Médicas", 40f, yPos, textPaint)
        yPos += 18f
        textPaint.color = Color.BLACK
        textPaint.typeface = Typeface.DEFAULT
        textPaint.textSize = 10f
        recomendaciones.take(3).forEach { rec ->
            canvas.drawText("• ${rec.mensaje}", 50f, yPos, textPaint)
            yPos += 14f
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
