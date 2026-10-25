package cl.mpaz.mpazpro.domain.model

// Resumen del informe que ve solo el docente.
// Describe resultados.

data class LogroPregunta(val enunciado: String, val porcentaje: Int)

data class ResultadoEstudiante(val nombre: String, val intentos: Int, val mejorPorcentaje: Int)

data class Informe(
    val porcentajeGeneral: Int = 0,
    val logros: List<LogroPregunta> = emptyList(),
    val estudiantes: List<ResultadoEstudiante> = emptyList(),
    val sinDatos: Boolean = true
) {

    val fortalezas: List<LogroPregunta> get() = logros.filter { it.porcentaje >= 70 }
    val aReforzar: List<LogroPregunta> get() = logros.filter { it.porcentaje < 50 }
}

// Informe en formato CSV para descargar.
// Separador ";" porque Excel en español usa la coma como decimal.
fun Informe.aCsv(codigoOa: String, comentario: String, estadoValidacion: String): String = buildString {
    fun limpio(t: String) = t.replace(";", ",").replace("\n", " ")
    appendLine("Informe preliminar de uso docente;${limpio(codigoOa)}")
    appendLine("No constituye diagnóstico ni decisión académica;")
    appendLine("Estado de validación;$estadoValidacion")
    appendLine("Comentario docente;${limpio(comentario)}")
    appendLine("Rendimiento general (%);$porcentajeGeneral")
    appendLine()
    appendLine("Pregunta;Logro (%)")
    logros.forEach { appendLine("${limpio(it.enunciado)};${it.porcentaje}") }
    appendLine()
    appendLine("Fortalezas (70 % o más);")
    fortalezas.forEach { appendLine("${limpio(it.enunciado)};${it.porcentaje}") }
    appendLine()
    appendLine("Aspectos a reforzar (menos de 50 %);")
    aReforzar.forEach { appendLine("${limpio(it.enunciado)};${it.porcentaje}") }
    appendLine()
    appendLine("Estudiante (ficticio);Intentos;Mejor resultado (%)")
    estudiantes.forEach { appendLine("${limpio(it.nombre)};${it.intentos};${it.mejorPorcentaje}") }
}