package cl.mpaz.mpazpro.nativo

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Recurso nativo de almacenamiento (Storage Access Framework): el docente elige dónde guardar
// el informe con el selector de archivos del sistema. No requiere permisos de almacenamiento.

class ExportadorCsv(private val context: Context) {

    suspend fun escribir(uri: Uri, contenido: String): Boolean = withContext(Dispatchers.IO) {
        runCatching {
            val salida = context.contentResolver.openOutputStream(uri) ?: error("No se pudo abrir el archivo")
            // BOM UTF-8 para que Excel muestre bien las tildes
            salida.use { it.write(("﻿" + contenido).toByteArray(Charsets.UTF_8)) }
        }.isSuccess
    }
}