package cl.mpaz.mpazpro.domain.model

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
    val errorObservacion: String? = null
)

// Formulario  Responder una pregunta (estudiante)
data class ActividadEstado(
    val indice: Int = 0,
    val seleccion: Int? = null,
    val errorSeleccion: String? = null,
    val comprobada: Boolean = false,
    val respuestas: List<Int> = emptyList(),
    val terminada: Boolean = false,
    val correctas: Int = 0
)

// Formulario Validación del informe (docente)
data class ValidacionEstado(
    val comentario: String = "",
    val revisado: Boolean = false,
    val errorComentario: String? = null,
    val errorRevisado: String? = null,
    val validado: Boolean = false
)