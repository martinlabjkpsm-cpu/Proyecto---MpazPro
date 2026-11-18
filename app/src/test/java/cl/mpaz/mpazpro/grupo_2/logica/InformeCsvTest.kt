package cl.mpaz.mpazpro.grupo_2.logica

import cl.mpaz.mpazpro.grupo_2.domain.model.Informe
import cl.mpaz.mpazpro.grupo_2.domain.model.LogroPregunta
import cl.mpaz.mpazpro.grupo_2.domain.model.ResultadoEstudiante
import cl.mpaz.mpazpro.grupo_2.domain.model.aCsv
import cl.mpaz.mpazpro.grupo_2.utils.Fechas
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

// Informe docente: fortalezas (70 % o más), aspectos a reforzar (menos de 50 %) y CSV de descarga.
// Herramienta: Kotest.

class InformeCsvTest : StringSpec({

    val informe = Informe(
        porcentajeGeneral = 60,
        logros = listOf(LogroPregunta("A", 90), LogroPregunta("B", 60), LogroPregunta("C", 20)),
        estudiantes = listOf(ResultadoEstudiante("Estudiante 01", 2, 80)),
        sinDatos = false
    )

    "fortalezas son las preguntas con 70 % o más y a reforzar las de menos de 50 %" {
        informe.fortalezas.map { it.enunciado } shouldContainExactly listOf("A")
        informe.aReforzar.map { it.enunciado } shouldContainExactly listOf("C")
    }

    "un informe nuevo parte sin datos" {
        Informe().sinDatos shouldBe true
    }

    "el CSV dice que no es un diagnóstico e incluye resultados por estudiante" {
        val csv = informe.aCsv("MA03 OA 01", "Reforzar", "Pendiente de validación")
        csv shouldContain "No constituye diagnóstico ni decisión académica"
        csv shouldContain "Rendimiento general (%);60"
        csv shouldContain "Estudiante 01;2;80"
    }

    "el CSV usa punto y coma y limpia los textos para no romper columnas" {
        val csv = Informe(60, listOf(LogroPregunta("¿Qué sigue; 10?", 60)), emptyList(), sinDatos = false)
            .aCsv("MA03 OA 01", "Bien;mejorar\notra línea", "Pendiente de validación")
        csv shouldContain "¿Qué sigue, 10?;60"
        csv shouldContain "Comentario docente;Bien,mejorar otra línea"
        csv shouldNotContain "Bien;mejorar"
    }

    "una fecha vacía se muestra con un guion" {
        Fechas.formato(null) shouldBe "—"
    }
})
