package com.example.modoguardian.ui.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri

import com.example.modoguardian.model.EstadoEvento
import com.example.modoguardian.model.Event
import com.example.modoguardian.model.MotivoCierre
import com.example.modoguardian.model.Prioridad
import com.example.modoguardian.model.User

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TurnReportPdf {
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 42f
    private const val CONTENT_WIDTH =
        PAGE_WIDTH - MARGIN * 2
    private val navy = Color.rgb(11, 20, 38)
    private val blue = Color.rgb(40, 100, 232)
    private val gray = Color.rgb(103, 116, 137)
    private val red = Color.rgb(220, 53, 69)
    private val green = Color.rgb(25, 135, 84)
    private val yellow = Color.rgb(160, 108, 0)

    fun generate(
        context: Context,
        destination: Uri,
        user: User?,
        events: List<Event>
    ) {

        val document = PdfDocument()
        val paint = Paint(
            Paint.ANTI_ALIAS_FLAG
        )

        var pageNumber = 0
        var page: PdfDocument.Page? = null
        var canvas: Canvas? = null
        var y = 0f
        fun drawText(
            value: String,
            x: Float,
            positionY: Float,
            size: Float = 10f,
            color: Int = navy,
            bold: Boolean = false
        ) {

            paint.color = color
            paint.textSize = size

            paint.typeface = Typeface.create(
                Typeface.DEFAULT,
                if (bold) Typeface.BOLD
                else Typeface.NORMAL
            )

            canvas?.drawText(
                value,
                x,
                positionY,
                paint
            )
        }

        fun drawLine(positionY: Float) {
            paint.color = Color.LTGRAY
            paint.strokeWidth = 1f
            canvas?.drawLine(
                MARGIN,
                positionY,
                PAGE_WIDTH - MARGIN,
                positionY,
                paint
            )
        }

        fun newPage() {
            page?.let {
                document.finishPage(it)
            }

            pageNumber++
            val info = PdfDocument.PageInfo.Builder(
                PAGE_WIDTH,
                PAGE_HEIGHT,
                pageNumber
            ).create()

            page = document.startPage(info)
            canvas = page?.canvas
            canvas?.drawColor(Color.WHITE)
            paint.color = blue
            canvas?.drawRect(
                0f,
                0f,
                PAGE_WIDTH.toFloat(),
                12f,
                paint
            )

            drawText(
                value = "INSERCODE",
                x = MARGIN,
                positionY = 47f,
                size = 19f,
                color = navy,
                bold = true
            )

            drawText(
                value = "SEGURIDAD INTEGRAL",
                x = MARGIN,
                positionY = 64f,
                size = 9f,
                color = blue,
                bold = true
            )

            drawLine(78f)
            drawText(
                value = "Reporte de eventos",
                x = MARGIN,
                positionY = 106f,
                size = 15f,
                color = navy,
                bold = true
            )

            drawText(
                value = "Pagina $pageNumber",
                x = PAGE_WIDTH - MARGIN - 70f,
                positionY = PAGE_HEIGHT - 24f,
                size = 9f,
                color = gray
            )

            y = 132f
        }

        fun ensureSpace(required: Float) {
            if (
                y + required >
                PAGE_HEIGHT - 55f
            ) {
                newPage()
            }
        }
        
        fun wrapText(
            value: String,
            maxWidth: Float,
            size: Float = 10f
        ): List<String> {
            paint.textSize = size
            paint.typeface = Typeface.DEFAULT
            val result = mutableListOf<String>()
            var current = ""
            val words = value
                .replace("\n", " ")
                .split(Regex("\\s+"))
            for (word in words) {
                if (word.isBlank()) continue
                val candidate = if (
                    current.isBlank()
                ) {
                    word
                } else {
                    "$current $word"
                }

                if (
                    paint.measureText(candidate) <= maxWidth
                ) {
                    current = candidate

                } else {
                    if (current.isNotBlank()) {
                        result.add(current)
                    }

                    if (
                        paint.measureText(word) > maxWidth
                    ) {
                        var piece = ""
                        for (char in word) {
                            val next = piece + char
                            if (
                                piece.isNotEmpty() &&
                                paint.measureText(next) > maxWidth
                            ) {
                                result.add(piece)
                                piece = char.toString()
                            } else {
                                piece = next
                            }
                        }

                        current = piece

                    } else {
                        current = word
                    }
                }
            }

            if (current.isNotBlank()) {
                result.add(current)
            }
            
            return result
        }

        fun paragraph(
            value: String,
            color: Int = navy,
            size: Float = 10f,
            bold: Boolean = false
        ) {
            val lines = wrapText(
                value = value,
                maxWidth = CONTENT_WIDTH,
                size = size
            )

            for (part in lines) {
                ensureSpace(18f)
                drawText(
                    value = part,
                    x = MARGIN,
                    positionY = y,
                    size = size,
                    color = color,
                    bold = bold
                )

                y += 16f
            }
        }

        fun closeReason(
            event: Event
        ): String {
            if (
                event.estado !=
                EstadoEvento.RESUELTO
            ) {
                return "No corresponde (evento abierto)"
            }

            return when (event.motivoCierre) {
                MotivoCierre.FALSA_ALARMA ->
                    "Falsa alarma"
                MotivoCierre.INCIDENTE_SOLUCIONADO ->
                    "Incidente solucionado"

                null ->
                    "Cierre sin clasificar"
            }
        }
        
        val total = events.size
        val critical = events.count {
            it.prioridad == Prioridad.CRITICA
        }

        val pending = events.count {
            it.estado == EstadoEvento.PENDIENTE
        }

        val inProgress = events.count {
            it.estado == EstadoEvento.EN_PROCESO
        }

        val resolved = events.count {
            it.estado == EstadoEvento.RESUELTO
        }

        val falseAlarms = events.count {
            it.estado == EstadoEvento.RESUELTO &&
                it.motivoCierre ==
                    MotivoCierre.FALSA_ALARMA
        }

        val solved = events.count {
            it.estado == EstadoEvento.RESUELTO &&
                it.motivoCierre ==
                    MotivoCierre.INCIDENTE_SOLUCIONADO
        }

        val unclassified = events.count {
            it.estado == EstadoEvento.RESUELTO &&
                it.motivoCierre == null
        }

        val dateFormat = SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale("es", "CL")
        )

        val generationDate = dateFormat.format(
            Date()
        )

        try {
            newPage()
            paragraph(
                "REPORTE DE TURNO",
                blue,
                11f,
                true
            )

            y += 8f
            paragraph(
                "Generado: $generationDate"
            )

            paragraph(
                "Responsable: " +
                    (user?.nombre ?: "No informado")
            )

            paragraph(
                "Correo: " +
                    (user?.email ?: "No informado")
            )

            paragraph(
                "Rol: " +
                    (user?.rol?.name ?: "No informado")
            )

            y += 14f
            ensureSpace(30f)
            drawLine(y)
            y += 24f
            paragraph(
                "RESUMEN GENERAL",
                blue,
                12f,
                true
            )

            y += 8f
            paragraph(
                "Total de eventos: $total"
            )

            paragraph(
                "Eventos criticos: $critical"
            )

            paragraph(
                "Eventos pendientes: $pending"
            )

            paragraph(
                "Eventos en proceso: $inProgress"
            )

            paragraph(
                "Eventos resueltos: $resolved"
            )

            y += 12f
            ensureSpace(35f)
            drawLine(y)
            y += 24f
            paragraph(
                "CLASIFICACION DE CIERRES",
                blue,
                12f,
                true
            )

            y += 8f
            paragraph(
                "Falsas alarmas: $falseAlarms",
                yellow,
                10f,
                true
            )

            paragraph(
                "Incidentes solucionados: $solved",
                green,
                10f,
                true
            )

            paragraph(
                "Cierres sin clasificar: $unclassified",
                gray
            )

            y += 8f
            paragraph(
                "Los cierres sin clasificar corresponden " +
                    "a eventos resueltos que no tienen " +
                    "un motivo registrado.",
                gray,
                9f
            )

            y += 14f
            ensureSpace(35f)
            drawLine(y)
            y += 24f
            paragraph(
                "DETALLE DE EVENTOS",
                blue,
                12f,
                true
            )

            y += 12f
            if (events.isEmpty()) {
                paragraph(
                    "No existen eventos para " +
                        "los filtros seleccionados."
                )
            }

            events.forEachIndexed { index, event ->
                val title = "${index + 1}. ${event.titulo}"
                val titleLines = wrapText(
                    title,
                    CONTENT_WIDTH,
                    11f
                )

                val descriptionLines = wrapText(
                    "Descripcion: ${event.descripcion}",
                    CONTENT_WIDTH,
                    10f
                )

                val locationLines = wrapText(
                    "Ubicacion: ${event.ubicacion}",
                    CONTENT_WIDTH,
                    10f
                )

                val required =
                    (
                        titleLines.size +
                        descriptionLines.size +
                        locationLines.size + 6
                    ) * 16f + 24f

                ensureSpace(
                    minOf(required, 110f)
                )

                paragraph(
                    title,
                    navy,
                    11f,
                    true
                )

                y += 4f
                paragraph(
                    "Codigo: ${event.id}"
                )

                paragraph(
                    "Fecha: ${event.fechaHora}"
                )

                paragraph(
                    "Ubicacion: ${event.ubicacion}"
                )

                paragraph(
                    "Prioridad: ${event.prioridad.name}",
                    if (
                        event.prioridad ==
                        Prioridad.CRITICA
                    ) {
                        red
                    } else {
                        navy
                    }
                )

                paragraph(
                    "Estado: " +
                        event.estado.name.replace(
                            "_",
                            " "
                        ),
                    if (
                        event.estado ==
                        EstadoEvento.RESUELTO
                    ) {
                        green
                    } else {
                        navy
                    }
                )

                val reason = closeReason(
                    event
                )

                val reasonColor = when (
                    event.motivoCierre
                ) {
                    MotivoCierre.FALSA_ALARMA ->
                        yellow
                    MotivoCierre.INCIDENTE_SOLUCIONADO ->
                        green
                    null ->
                        gray
                }

                paragraph(
                    "Motivo de cierre: $reason",
                    reasonColor,
                    10f,
                    event.estado ==
                        EstadoEvento.RESUELTO
                )

                paragraph(
                    "Descripcion: ${event.descripcion}"
                )

                y += 12f
                ensureSpace(10f)
                drawLine(y)
                y += 20f
            }

            ensureSpace(70f)
            y += 14f
            paragraph(
                "Documento generado por INSERCODE.",
                gray,
                9f
            )

            paragraph(
                "Este reporte contiene " +
                    "los eventos seleccionados " +
                    "en el historial de la aplicacion.",
                gray,
                9f
            )

            paragraph(
                "Sistema de demostracion academica. " +
                    "No constituye un reporte " +
                    "de vigilancia certificado.",
                gray,
                9f
            )

            page?.let {
                document.finishPage(it)
                page = null
            }

            context.contentResolver
                .openOutputStream(
                    destination,
                    "w"
                )
                ?.use { output ->

                    document.writeTo(
                        output
                    )

                } ?: error(
                    "No se pudo abrir el destino del PDF"
                )

        } finally {
            page?.let {
                document.finishPage(it)
            }

            document.close()
        }
    }
}
