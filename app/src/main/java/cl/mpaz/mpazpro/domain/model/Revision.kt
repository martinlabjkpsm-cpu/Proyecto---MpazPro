package cl.mpaz.mpazpro.domain.model

// Una fila de la revisión que ve el estudiante al terminar una actividad:
// qué marcó, cuál era la correcta y la retroalimentación.

data class RevisionPregunta(
    val enunciado: String,
    val opciones: List<String>,
    val elegida: Int,
    val correcta: Int,
    val ayuda: String
) {
    val esCorrecta: Boolean get() = elegida == correcta
}