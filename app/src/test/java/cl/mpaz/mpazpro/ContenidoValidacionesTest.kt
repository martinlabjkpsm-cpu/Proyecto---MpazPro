package cl.mpaz.mpazpro

import cl.mpaz.mpazpro.data.local.PreguntaEntity
import cl.mpaz.mpazpro.domain.model.CampoContenido
import cl.mpaz.mpazpro.domain.model.CampoPregunta
import cl.mpaz.mpazpro.domain.model.ContenidoFormEstado
import cl.mpaz.mpazpro.domain.model.Informe
import cl.mpaz.mpazpro.domain.model.LogroPregunta
import cl.mpaz.mpazpro.domain.model.Logros
import cl.mpaz.mpazpro.domain.model.PreguntaFormEstado
import cl.mpaz.mpazpro.domain.model.TipoActividad
import cl.mpaz.mpazpro.domain.model.aCsv
import cl.mpaz.mpazpro.utils.Validaciones
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// pruebas unitarias de los formularios nuevos, la revisión de respuestas y el informe CSV

class ContenidoValidacionesTest {

    private val valido = ContenidoFormEstado(
        asignatura = "Matemática", unidad = "Unidad 1", eje = "Números", codigoOa = "MA03 OA 01",
        objetivo = "Contar de 10 en 10 hasta 1 000", explicacion = "Cada salto suma diez unidades al número.",
        recurso = "Currículum Nacional"
    )

    @Test
    fun contenidoValidoNoTieneErrores() {
        assertTrue(Validaciones.contenido(valido).isEmpty())
    }

    @Test
    fun codigoDebeCoincidirConAsignatura() {
        assertNotNull(Validaciones.codigoOa("LE03 OA 01", "Matemática"))
        assertNotNull(Validaciones.codigoOa("MA3 OA 1", "Matemática"))
        assertNull(Validaciones.codigoOa("MA03 OA 01", "Matemática"))
    }

    @Test
    fun soloUnaUnidadPorAsignatura() {
        assertNotNull(Validaciones.unidadUnica("Unidad 2", listOf("Unidad 1")))
        assertNull(Validaciones.unidadUnica("unidad 1", listOf("Unidad 1", "")))
    }

    @Test
    fun objetivoCortoTieneError() {
        assertEquals(setOf(CampoContenido.OBJETIVO), Validaciones.contenido(valido.copy(objetivo = "corto")).keys)
    }

    @Test
    fun preguntaSinCorrectaYAlternativasRepetidas() {
        val p = PreguntaFormEstado(
            enunciado = "¿Qué sigue?", opcionA = "10", opcionB = "10", opcionC = "20", ayuda = "Suma diez"
        )
        val errores = Validaciones.pregunta(p)
        assertTrue(CampoPregunta.CORRECTA in errores)
        assertTrue(CampoPregunta.OPCION_C in errores)
    }

    @Test
    fun contenidoNecesitaDesafioYPrueba() {
        fun q(t: TipoActividad) = PreguntaEntity(0, 1, t, "x", "a", "b", "c", 0, 1, "y")
        assertNotNull(Validaciones.preguntasMinimas(listOf(q(TipoActividad.DESAFIO))))
        assertNull(Validaciones.preguntasMinimas(listOf(q(TipoActividad.DESAFIO), q(TipoActividad.PRUEBA))))
    }

    @Test
    fun respuestaCorrectaSeRevelaSoloSiApruebaOSinIntentos() {
        assertFalse(Logros.mostrarRespuestaCorrecta(50, 2))
        assertTrue(Logros.mostrarRespuestaCorrecta(80, 2))
        assertTrue(Logros.mostrarRespuestaCorrecta(30, 0))
    }

    @Test
    fun csvUsaPuntoYComaYNoRompeColumnas() {
        val informe = Informe(60, listOf(LogroPregunta("¿Qué sigue; 10?", 60)), emptyList(), sinDatos = false)
        val csv = informe.aCsv("MA03 OA 01", "Bien;mejorar", "Pendiente de validación")
        assertTrue(csv.contains("¿Qué sigue, 10?;60"))
        assertTrue(csv.contains("Comentario docente;Bien,mejorar"))
    }
}