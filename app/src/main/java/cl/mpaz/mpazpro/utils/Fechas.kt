package cl.mpaz.mpazpro.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Formato de fechas para mostrar al docente (hora de Chile)

object Fechas {
    fun formato(ms: Long?): String =
        if (ms == null) "—" else SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.forLanguageTag("es-CL")).format(Date(ms))
}