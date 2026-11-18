package cl.mpaz.mpazpro.grupo_2.logica

import cl.mpaz.mpazpro.grupo_2.data.local.IntentoEntity
import cl.mpaz.mpazpro.grupo_2.domain.model.Logros
import cl.mpaz.mpazpro.grupo_2.domain.model.RevisionPregunta
import cl.mpaz.mpazpro.grupo_2.domain.model.TipoActividad
import io.kotest.core.spec.style.StringSpec
import io.kotest.data.forAll
import io.kotest.data.row
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

// Cálculo de logros del estudiante (porcentaje, estrellas, medallas) y regla para revelar
// la respuesta correcta. Herramienta: Kotest (incluye pruebas con tabla de datos).

class LogrosTest : StringSpec({

    fun intento(contenido: Int, tipo: TipoActividad, correctas: Int, total: Int) =
        IntentoEntity(estudianteId = 1, contenidoId = contenido, tipo = tipo, correctas = correctas, total = total)

    "estrellas y mensaje según el porcentaje" {
        forAll(
            row(100, 3, "¡Sobresaliente!"),
            row(90, 3, "¡Sobresaliente!"),
            row(70, 2, "¡Muy bien!"),
            row(50, 1, "¡Vas bien!"),
            row(30, 0, "Sigue practicando")
        ) { porcentaje, estrellas, mensaje ->
            Logros.estrellas(porcentaje) shouldBe estrellas
            Logros.mensaje(estrellas) shouldBe mensaje
        }
    }

    "el porcentaje sin preguntas es cero (no divide por cero)" {
        Logros.porcentaje(0, 0) shouldBe 0
        Logros.porcentaje(2, 3) shouldBe 66
    }

    "una medalla por objetivo con prueba final aprobada" {
        val intentos = listOf(
            intento(1, TipoActividad.PRUEBA, 3, 3),
            intento(1, TipoActividad.PRUEBA, 3, 3),   // repetir el mismo objetivo no suma
            intento(2, TipoActividad.PRUEBA, 1, 3),   // reprobada
            intento(3, TipoActividad.DESAFIO, 3, 3)   // los desafíos no dan medalla
        )
        Logros.medallas(intentos) shouldBe 1
    }

    "el mejor porcentaje toma el mayor y es nulo si no lo ha intentado" {
        val intentos = listOf(intento(1, TipoActividad.PRUEBA, 1, 2), intento(1, TipoActividad.PRUEBA, 2, 2))
        Logros.mejorPorcentaje(1, TipoActividad.PRUEBA, intentos) shouldBe 100
        Logros.mejorPorcentaje(9, TipoActividad.PRUEBA, intentos).shouldBeNull()
    }

    "la respuesta correcta se revela solo si aprueba o ya no tiene intentos" {
        Logros.mostrarRespuestaCorrecta(50, 2) shouldBe false
        Logros.mostrarRespuestaCorrecta(80, 2) shouldBe true
        Logros.mostrarRespuestaCorrecta(30, 0) shouldBe true
    }

    "una fila de revisión sabe si la respuesta fue correcta" {
        RevisionPregunta("¿2+2?", listOf("3", "4", "5"), elegida = 1, correcta = 1, ayuda = "").esCorrecta shouldBe true
        RevisionPregunta("¿2+2?", listOf("3", "4", "5"), elegida = 0, correcta = 1, ayuda = "").esCorrecta shouldBe false
    }
})
