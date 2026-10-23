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