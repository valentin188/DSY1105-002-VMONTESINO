package com.example.modoguardian.ui.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.example.modoguardian.model.Event
import com.example.modoguardian.model.User
import com.example.modoguardian.model.EstadoEvento
import com.example.modoguardian.model.Prioridad
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

    fun generate(
        context: Context,
        destination: Uri,
        user: User?,
        events: List<Event>
    ) {
        val document = PdfDocument()

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        var pageNumber = 0
        var page: PdfDocument.Page? = null
        var canvas: Canvas? = null
        var y = 0f

        fun text(
            value: String,
            x: Float,
            positionY: Float,
            size: Float = 10f,
            color: Int = navy,
            bold: Boolean = false
        ) {
            paint.color = color
            paint.textSize = size
            paint.typeface = if (bold) {
                Typeface.create(
                    Typeface.DEFAULT,
                    Typeface.BOLD
                )
            } else {
                Typeface.DEFAULT
            }

            canvas?.drawText(
                value,
                x,
                positionY,
                paint
            )
        }

        fun line(positionY: Float) {
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

            text(
                "INSERCODE",
                MARGIN,
                47f,
                19f,
                navy,
                true
            )

            text(
                "SEGURIDAD INTEGRAL",
                MARGIN,
                64f,
                9f,
                blue,
                true
            )

            line(78f)

            text(
                "Reporte de eventos",
                MARGIN,
                106f,
                15f,
                navy,
                true
            )

            text(
                "Pagina $pageNumber",
                PAGE_WIDTH - MARGIN - 65f,
                PAGE_HEIGHT - 24f,
                9f,
                gray
            )

            y = 130f
        }

        fun ensureSpace(required: Float) {
            if (y + required > PAGE_HEIGHT - 55f) {
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

            for (word in value
                .replace("\n", " ")
                .split(Regex("\\s+"))) {

                if (word.isBlank()) continue

                val candidate = if (current.isBlank()) {
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

                    if (paint.measureText(word) > maxWidth) {
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
                value,
                CONTENT_WIDTH,
                size
            )

            for (part in lines) {
                ensureSpace(18f)

                text(
                    part,
                    MARGIN,
                    y,
                    size,
                    color,
                    bold
                )

                y += 15f
            }
        }

        val dateFormat = SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale("es", "CL")
        )

        val fechaGeneracion = dateFormat.format(Date())

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
                "Generado: $fechaGeneracion"
            )

            paragraph(
                "Responsable: ${user?.nombre ?: "No informado"}"
            )

            paragraph(
                "Correo: ${user?.email ?: "No informado"}"
            )

            paragraph(
                "Rol: ${user?.rol?.name ?: "No informado"}"
            )

            y += 14f
            line(y)
            y += 24f

            paragraph(
                "RESUMEN GENERAL",
                blue,
                12f,
                true
            )

            y += 8f

            val total = events.size

            val criticos = events.count {
                it.prioridad == Prioridad.CRITICA
            }

            val pendientes = events.count {
                it.estado == EstadoEvento.PENDIENTE
            }

            val resueltos = events.count {
                it.estado == EstadoEvento.RESUELTO
            }

            paragraph("Total de eventos: $total")
            paragraph("Eventos criticos: $criticos")
            paragraph("Eventos pendientes: $pendientes")
            paragraph("Eventos resueltos: $resueltos")

            y += 14f
            line(y)
            y += 25f

            paragraph(
                "DETALLE DE EVENTOS",
                blue,
                12f,
                true
            )

            y += 12f

            if (events.isEmpty()) {
                paragraph(
                    "No existen eventos para los filtros seleccionados."
                )
            }

            events.forEachIndexed { index, event ->

                val titleLines = wrapText(
                    "${index + 1}. ${event.titulo}",
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
                    (titleLines.size +
                        descriptionLines.size +
                        locationLines.size + 5) * 16f + 20f

                if (required < PAGE_HEIGHT - 180f) {
                    ensureSpace(required)
                } else {
                    ensureSpace(90f)
                }

                paragraph(
                    "${index + 1}. ${event.titulo}",
                    navy,
                    11f,
                    true
                )

                y += 4f

                paragraph("Codigo: ${event.id}")

                paragraph(
                    "Fecha: ${event.fechaHora}"
                )

                paragraph(
                    "Ubicacion: ${event.ubicacion}"
                )

                paragraph(
                    "Prioridad: ${event.prioridad.name}",
                    if (
                        event.prioridad == Prioridad.CRITICA
                    ) red else navy
                )

                paragraph(
                    "Estado: ${event.estado.name.replace("_", " ")}",
                    if (
                        event.estado == EstadoEvento.RESUELTO
                    ) green else navy
                )

                paragraph(
                    "Descripcion: ${event.descripcion}"
                )

                y += 12f
                ensureSpace(8f)
                line(y)
                y += 20f
            }

            ensureSpace(55f)

            y += 14f

            paragraph(
                "Documento generado por INSERCODE.",
                gray,
                9f
            )

            paragraph(
                "Reporte local de los eventos seleccionados.",
                gray,
                9f
            )

            page?.let {
                document.finishPage(it)
                page = null
            }

            context.contentResolver
                .openOutputStream(destination, "w")
                ?.use { output ->
                    document.writeTo(output)
                } ?: error(
                    "No se pudo abrir el destino del PDF"
                )

        } finally {
            document.close()
        }
    }
}
