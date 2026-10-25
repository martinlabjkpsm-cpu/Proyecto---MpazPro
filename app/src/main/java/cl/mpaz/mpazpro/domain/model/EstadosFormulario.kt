package cl.mpaz.mpazpro.domain.model

import cl.mpaz.mpazpro.data.local.PreguntaEntity

// Formulario Login
data class LoginEstado(
    val usuario: String = "",
    val pin: String = "",
    val recordar: Boolean = false,
    val errorUsuario: String? = null,
    val errorPin: String? = null,
    val errorGeneral: String? = null,
    val cargando: Boolean = false,
    val rolIngresado: Rol? = null
)

// Formulario Asignar actividad (docente)
data class AsignarEstado(
    val intentos: String = "",
    val asignado: Boolean = true,
    val errorIntentos: String? = null,
    val guardado: Boolean = false
)

// Formulario  Revisión UTP (aprobar o rechazar)
data class RevisionEstado(
    val contenidoId: Int? = null,
    val observacion: String = "",
    val errorObservacion: String? = null,
    val preguntas: List<PreguntaEntity> = emptyList(),   // la UTP revisa también las preguntas
    val errorAprobacion: String? = null                   // no se aprueba un contenido incompleto
)

// Formulario  Responder una pregunta (estudiante)
data class ActividadEstado(
    val indice: Int = 0,
    val seleccion: Int? = null,
    val errorSeleccion: String? = null,
    val comprobada: Boolean = false,
    val respuestas: List<Int> = emptyList(),
    val terminada: Boolean = false,
    val correctas: Int = 0,
    val revision: List<RevisionPregunta> = emptyList(),   // respuestas para revisar al terminar
    val mostrarCorrectas: Boolean = true,                  // si se revela la alternativa correcta
    val intentosRestantes: Int? = null                     // solo en la prueba final
)

// Formulario Validación del informe (docente)
data class ValidacionEstado(
    val comentario: String = "",
    val revisado: Boolean = false,
    val errorComentario: String? = null,
    val errorRevisado: String? = null,
    val validado: Boolean = false
)

// Formulario Contenido (docente propone o corrige un objetivo)
enum class CampoContenido { ASIGNATURA, UNIDAD, EJE, CODIGO, OBJETIVO, EXPLICACION, EJEMPLO, RECURSO }

data class ContenidoFormEstado(
    val id: Int = 0,
    val asignatura: String = "",
    val unidad: String = "",
    val eje: String = "",
    val codigoOa: String = "",
    val objetivo: String = "",
    val explicacion: String = "",
    val ejemplo: String = "",
    val recurso: String = "",
    val estado: Estado = Estado.PENDIENTE,
    val observacionUtp: String = "",
    val errores: Map<CampoContenido, String> = emptyMap(),
    val errorPreguntas: String? = null,
    val guardado: Boolean = false
)

// Formulario Pregunta (desafío o prueba final de un objetivo)
enum class CampoPregunta { ENUNCIADO, OPCION_A, OPCION_B, OPCION_C, CORRECTA, AYUDA }

data class PreguntaFormEstado(
    val tipo: TipoActividad = TipoActividad.DESAFIO,
    val enunciado: String = "",
    val opcionA: String = "",
    val opcionB: String = "",
    val opcionC: String = "",
    val correcta: Int? = null,
    val dificultad: Int = 1,
    val ayuda: String = "",
    val intentado: Boolean = false,          // los errores se muestran después del primer "Agregar"
    val errores: Map<CampoPregunta, String> = emptyMap()
)