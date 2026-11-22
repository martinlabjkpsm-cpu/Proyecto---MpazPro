package cl.mpaz.mpazpro.grupo_2.data.remote.microservicios

import cl.mpaz.mpazpro.grupo_2.domain.model.Estado
import cl.mpaz.mpazpro.grupo_2.domain.model.TipoActividad

// JSON de los microservicios Spring Boot (repositorio mpazpro-microservicios).
// Los nombres de los campos son los mismos de las entidades de Room, así Gson los convierte sin anotaciones.
// Un id null al crear significa "el servidor lo asigna" (Gson no envía los campos null).

/** ms-contenidos: un objetivo de aprendizaje (OA). */
data class ContenidoDto(
    val id: Int? = null,
    val asignatura: String,
    val unidad: String = "",
    val eje: String = "",
    val codigoOa: String,
    val objetivo: String,
    val explicacion: String,
    val ejemplo: String = "",
    val recursoReferencia: String = "",
    val estado: Estado = Estado.PENDIENTE,
    val observacionUtp: String = "",
    val asignado: Boolean = false,
    val maxIntentos: Int = 3,
    val informeValidado: Boolean = false,
    val comentarioDocente: String = "",
    val fechaValidacion: Long? = null,
    val intentosAlValidar: Int = 0
)

/** ms-contenidos: pregunta de desafío o de prueba final. */
data class PreguntaDto(
    val id: Int? = null,
    val contenidoId: Int? = null,
    val tipo: TipoActividad,
    val enunciado: String,
    val opcionA: String,
    val opcionB: String,
    val opcionC: String,
    val correcta: Int,
    val dificultad: Int,
    val ayuda: String
)

/** ms-resultados: la alternativa elegida en una pregunta. */
data class RespuestaDto(
    val id: Int? = null,
    val intentoId: Int? = null,
    val preguntaId: Int,
    val esCorrecta: Boolean,
    val opcionElegida: Int
)

/** ms-resultados: lo que envía la app al terminar un desafío o la prueba. El servidor cuenta las correctas. */
data class IntentoRequest(
    val estudianteId: Int,
    val contenidoId: Int,
    val tipo: TipoActividad,
    val fecha: Long,
    val respuestas: List<RespuestaDto>
)

/** ms-resultados: un intento guardado, con sus respuestas. */
data class IntentoDto(
    val id: Int,
    val estudianteId: Int,
    val contenidoId: Int,
    val tipo: TipoActividad,
    val correctas: Int,
    val total: Int,
    val fecha: Long,
    val respuestas: List<RespuestaDto> = emptyList()
)
