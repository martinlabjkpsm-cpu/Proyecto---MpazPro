package cl.mpaz.mpazpro.utils

import cl.mpaz.mpazpro.data.local.PreguntaEntity
import cl.mpaz.mpazpro.domain.model.CampoContenido
import cl.mpaz.mpazpro.domain.model.CampoPregunta
import cl.mpaz.mpazpro.domain.model.ContenidoFormEstado
import cl.mpaz.mpazpro.domain.model.PreguntaFormEstado
import cl.mpaz.mpazpro.domain.model.TipoActividad

//validaciones separadas de la pantalla

object Validaciones {


    fun usuario(valor: String): String? = when {
        valor.isBlank() -> "Escribe tu usuario"
        valor.length < 4 -> "El usuario tiene al menos 4 caracteres"
        !valor.all { it.isLowerCase() || it.isDigit() } -> "solo minúsculas y números"
        else -> null
    }


    fun pin(valor: String): String? = when {
        valor.isBlank() -> "Escribe tu PIN"
        !valor.all { it.isDigit() } -> "El PIN solo tiene números"
        valor.length != 4 -> "El PIN tiene 4 números"
        else -> null
    }


    fun intentos(valor: String): String? {
        val numero = valor.toIntOrNull()
        return when {
            valor.isBlank() -> "Indica el máximo de intentos"
            numero == null -> "Ingresa solo números"
            numero !in 1..5 -> "Debe ser entre 1 y 5 intentos"
            else -> null
        }
    }


    fun observacion(valor: String, esRechazo: Boolean): String? = when {
        valor.length > 200 -> "Máximo 200 caracteres"
        esRechazo && valor.trim().length < 10 -> "Para rechazar, explica el motivo (mínimo 10 caracteres)"
        else -> null
    }

    fun seleccion(valor: Int?): String? =
        if (valor == null) "Elige una alternativa antes de continuar" else null


    fun comentario(valor: String): String? = when {
        valor.isBlank() -> "Escribe un comentario"
        valor.trim().length < 10 -> "Mínimo 10 caracteres"
        else -> null
    }


    fun aceptacion(marcado: Boolean): String? =
        if (!marcado) "Debes confirmar que revisaste el informe" else null


    // ---------- Formulario de contenido ----------

    // Asignaturas del caso y el prefijo de su código OA
    val ASIGNATURAS = mapOf(
        "Lenguaje" to "LE", "Matemática" to "MA", "Ciencias Naturales" to "CN", "Historia" to "HI"
    )

    fun asignatura(valor: String): String? =
        if (valor !in ASIGNATURAS) "Elige una asignatura" else null

    fun codigoOa(valor: String, asignatura: String): String? {
        val prefijo = ASIGNATURAS[asignatura]
        return when {
            valor.isBlank() -> "Escribe el código del OA"
            !Regex("^[A-Z]{2}03 OA \\d{2}$").matches(valor.trim()) -> "Formato esperado: MA03 OA 01"
            prefijo != null && !valor.startsWith(prefijo) -> "Para $asignatura el código empieza con $prefijo"
            else -> null
        }
    }

    fun textoLargo(valor: String, campo: String, min: Int, max: Int): String? = when {
        valor.isBlank() -> "Escribe $campo"
        valor.trim().length < min -> "Mínimo $min caracteres"
        valor.length > max -> "Máximo $max caracteres"
        else -> null
    }

    // Regla del caso: como máximo UNA unidad por asignatura
    fun unidadUnica(unidad: String, unidadesExistentes: List<String>): String? {
        val distinta = unidadesExistentes.filter { it.isNotBlank() }
            .firstOrNull { !it.equals(unidad.trim(), ignoreCase = true) }
        return if (distinta != null) "Solo se permite una unidad por asignatura (ya existe \"$distinta\")" else null
    }

    fun contenido(e: ContenidoFormEstado): Map<CampoContenido, String> = buildMap {
        asignatura(e.asignatura)?.let { put(CampoContenido.ASIGNATURA, it) }
        textoLargo(e.unidad, "la unidad", 3, 60)?.let { put(CampoContenido.UNIDAD, it) }
        textoLargo(e.eje, "el eje curricular", 3, 60)?.let { put(CampoContenido.EJE, it) }
        codigoOa(e.codigoOa, e.asignatura)?.let { put(CampoContenido.CODIGO, it) }
        textoLargo(e.objetivo, "el objetivo", 15, 300)?.let { put(CampoContenido.OBJETIVO, it) }
        textoLargo(e.explicacion, "la explicación", 20, 600)?.let { put(CampoContenido.EXPLICACION, it) }
        if (e.ejemplo.length > 300) put(CampoContenido.EJEMPLO, "Máximo 300 caracteres")
        textoLargo(e.recurso, "el recurso de referencia", 5, 150)?.let { put(CampoContenido.RECURSO, it) }
    }


    // ---------- Formulario de pregunta ----------

    fun pregunta(p: PreguntaFormEstado): Map<CampoPregunta, String> = buildMap {
        textoLargo(p.enunciado, "la pregunta", 5, 150)?.let { put(CampoPregunta.ENUNCIADO, it) }
        textoLargo(p.opcionA, "la alternativa A", 1, 60)?.let { put(CampoPregunta.OPCION_A, it) }
        textoLargo(p.opcionB, "la alternativa B", 1, 60)?.let { put(CampoPregunta.OPCION_B, it) }
        textoLargo(p.opcionC, "la alternativa C", 1, 60)?.let { put(CampoPregunta.OPCION_C, it) }
        val opciones = listOf(p.opcionA, p.opcionB, p.opcionC).map { it.trim().lowercase() }.filter { it.isNotEmpty() }
        if (opciones.size != opciones.distinct().size) put(CampoPregunta.OPCION_C, "Las alternativas no pueden repetirse")
        if (p.correcta == null) put(CampoPregunta.CORRECTA, "Marca cuál es la alternativa correcta")
        textoLargo(p.ayuda, "la retroalimentación", 5, 150)?.let { put(CampoPregunta.AYUDA, it) }
    }

    // Un contenido solo puede aprobarse o asignarse si tiene desafíos y prueba final
    fun preguntasMinimas(preguntas: List<PreguntaEntity>): String? = when {
        preguntas.none { it.tipo == TipoActividad.DESAFIO } -> "Agrega al menos un desafío"
        preguntas.none { it.tipo == TipoActividad.PRUEBA } -> "Agrega al menos una pregunta de prueba final"
        else -> null
    }
}