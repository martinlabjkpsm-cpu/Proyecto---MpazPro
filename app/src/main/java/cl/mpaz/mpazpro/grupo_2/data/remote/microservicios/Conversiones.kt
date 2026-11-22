package cl.mpaz.mpazpro.grupo_2.data.remote.microservicios

import cl.mpaz.mpazpro.grupo_2.data.local.ContenidoEntity
import cl.mpaz.mpazpro.grupo_2.data.local.IntentoEntity
import cl.mpaz.mpazpro.grupo_2.data.local.PreguntaEntity
import cl.mpaz.mpazpro.grupo_2.data.local.RespuestaEntity

// Conversiones entre las entidades de Room y el JSON de los microservicios.
// El id del servidor se guarda tal cual en Room, así los dos lados usan el mismo número.

fun ContenidoEntity.aDto() = ContenidoDto(
    id = id.takeIf { it > 0 }, asignatura = asignatura, unidad = unidad, eje = eje, codigoOa = codigoOa,
    objetivo = objetivo, explicacion = explicacion, ejemplo = ejemplo, recursoReferencia = recursoReferencia,
    estado = estado, observacionUtp = observacionUtp, asignado = asignado, maxIntentos = maxIntentos,
    informeValidado = informeValidado, comentarioDocente = comentarioDocente,
    fechaValidacion = fechaValidacion, intentosAlValidar = intentosAlValidar
)

fun ContenidoDto.aEntidad() = ContenidoEntity(
    id = id ?: 0, asignatura = asignatura, unidad = unidad, eje = eje, codigoOa = codigoOa,
    objetivo = objetivo, explicacion = explicacion, ejemplo = ejemplo, recursoReferencia = recursoReferencia,
    estado = estado, observacionUtp = observacionUtp, asignado = asignado, maxIntentos = maxIntentos,
    informeValidado = informeValidado, comentarioDocente = comentarioDocente,
    fechaValidacion = fechaValidacion, intentosAlValidar = intentosAlValidar
)

fun PreguntaEntity.aDto() = PreguntaDto(
    id = id.takeIf { it > 0 }, contenidoId = contenidoId, tipo = tipo, enunciado = enunciado,
    opcionA = opcionA, opcionB = opcionB, opcionC = opcionC,
    correcta = correcta, dificultad = dificultad, ayuda = ayuda
)

/** `contenidoId` de respaldo: el de la ruta, por si el JSON no lo trae. */
fun PreguntaDto.aEntidad(contenidoIdRuta: Int = 0) = PreguntaEntity(
    id = id ?: 0, contenidoId = contenidoId ?: contenidoIdRuta, tipo = tipo, enunciado = enunciado,
    opcionA = opcionA, opcionB = opcionB, opcionC = opcionC,
    correcta = correcta, dificultad = dificultad, ayuda = ayuda
)

fun IntentoDto.aEntidad() = IntentoEntity(
    id = id, estudianteId = estudianteId, contenidoId = contenidoId, tipo = tipo,
    correctas = correctas, total = total, fecha = fecha
)

fun RespuestaDto.aEntidad(intentoIdPadre: Int) = RespuestaEntity(
    id = id ?: 0, intentoId = intentoId ?: intentoIdPadre, preguntaId = preguntaId,
    esCorrecta = esCorrecta, opcionElegida = opcionElegida
)
