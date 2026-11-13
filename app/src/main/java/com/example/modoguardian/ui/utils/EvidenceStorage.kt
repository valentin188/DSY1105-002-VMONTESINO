package com.example.modoguardian.ui.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

object EvidenceStorage {

    fun save(
        context: Context,
        original: Uri
    ): String {

        val directory = File(
            context.filesDir,
            "evidence"
        ).apply {
            mkdirs()
        }

        val destination = File(
            directory,
            "photo_${UUID.randomUUID()}.jpg"
        )

        context.contentResolver
            .openInputStream(original)
            ?.use { input ->

                destination.outputStream().use { output ->
                    input.copyTo(output)
                }

            } ?: error(
                "No se pudo leer la imagen seleccionada"
            )

        return Uri.fromFile(destination).toString()
    }
}
