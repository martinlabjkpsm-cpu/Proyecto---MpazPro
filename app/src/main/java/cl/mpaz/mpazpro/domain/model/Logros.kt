package cl.mpaz.mpazpro.domain.model

import cl.mpaz.mpazpro.data.local.IntentoEntity

// Cálculos de logros del estudiante (estrellas y medallas) a partir de sus intentos guardados en Room.

object Logros {

    fun porcentaje(correctas: Int, total: Int): Int = if (total == 0) 0 else correctas * 100 / total

    fun estrellas(porcentaje: Int): Int = when {
        porcentaje >= 90 -> 3
        porcentaje >= 70 -> 2
        porcentaje >= 50 -> 1
        else -> 0
    }

    fun mensaje(estrellas: Int): String = when (estrellas) {
        3 -> "¡Sobresaliente!"
        2 -> "¡Muy bien!"
        1 -> "¡Vas bien!"
        else -> "Sigue practicando"
    }

    // Mejor resultado del estudiante en un objetivo (null si no lo ha intentado)
    fun mejorPorcentaje(contenidoId: Int, tipo: TipoActividad, intentos: List<IntentoEntity>): Int? =
        intentos.filter { it.contenidoId == contenidoId && it.tipo == tipo }
            .maxOfOrNull { porcentaje(it.correctas, it.total) }

    // Una medalla por cada objetivo cuya prueba final se aprobó con 70 % o más
    fun medallas(intentos: List<IntentoEntity>): Int =
        intentos.filter { it.tipo == TipoActividad.PRUEBA && porcentaje(it.correctas, it.total) >= 70 }
            .map { it.contenidoId }
            .distinct()
            .size

    // La respuesta correcta se revela si aprobó (70 % o más) o si ya usó todos sus intentos,
    // así no se "regala" la respuesta para el siguiente intento.
    fun mostrarRespuestaCorrecta(porcentaje: Int, intentosRestantes: Int): Boolean =
        porcentaje >= 70 || intentosRestantes <= 0
}