package cl.mpaz.mpazpro

import cl.mpaz.mpazpro.data.local.IntentoEntity
import cl.mpaz.mpazpro.domain.model.Logros
import cl.mpaz.mpazpro.domain.model.TipoActividad
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

//pruebas unitarias de estrellas y medallas

class LogrosTest {

    private fun intento(contenido: Int, tipo: TipoActividad, correctas: Int, total: Int) =
        IntentoEntity(estudianteId = 1, contenidoId = contenido, tipo = tipo, correctas = correctas, total = total)

    @Test
    fun estrellasSegunPorcentaje() {
        assertEquals(3, Logros.estrellas(100))
        assertEquals(2, Logros.estrellas(70))
        assertEquals(1, Logros.estrellas(50))
        assertEquals(0, Logros.estrellas(30))
    }

    @Test
    fun porcentajeSinPreguntasEsCero() {
        assertEquals(0, Logros.porcentaje(0, 0))
        assertEquals(66, Logros.porcentaje(2, 3))
    }

    @Test
    fun medallaSoloPorPruebaAprobadaUnaVezPorObjetivo() {
        val intentos = listOf(
            intento(1, TipoActividad.PRUEBA, 3, 3),
            intento(1, TipoActividad.PRUEBA, 3, 3),   // repetir el mismo objetivo no suma otra medalla
            intento(2, TipoActividad.PRUEBA, 1, 3),   // reprobada
            intento(3, TipoActividad.DESAFIO, 3, 3)   // los desafíos no dan medalla
        )
        assertEquals(1, Logros.medallas(intentos))
    }

    @Test
    fun mejorPorcentajeTomaElMayor() {
        val intentos = listOf(intento(1, TipoActividad.PRUEBA, 1, 2), intento(1, TipoActividad.PRUEBA, 2, 2))
        assertEquals(100, Logros.mejorPorcentaje(1, TipoActividad.PRUEBA, intentos))
        assertNull(Logros.mejorPorcentaje(9, TipoActividad.PRUEBA, intentos))
    }
}
