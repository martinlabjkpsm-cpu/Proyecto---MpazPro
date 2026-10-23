package cl.mpaz.mpazpro

import cl.mpaz.mpazpro.utils.Validaciones
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

//pruebas unitarias

class ValidacionesTest {

    @Test
    fun pinCorrectoNoTieneError() {
        assertNull(Validaciones.pin("1234"))
    }

    @Test
    fun pinConLetrasOCortoTieneError() {
        assertEquals("El PIN solo tiene números", Validaciones.pin("12a4"))
        assertEquals("El PIN tiene 4 números", Validaciones.pin("123"))
    }

    @Test
    fun intentosFueraDeRango() {
        assertNull(Validaciones.intentos("3"))
        assertNotNull(Validaciones.intentos("0"))
        assertNotNull(Validaciones.intentos("9"))
    }

    @Test
    fun rechazarSinObservacionTieneError() {
        assertNotNull(Validaciones.observacion("", esRechazo = true))
        assertNull(Validaciones.observacion("", esRechazo = false))
    }

    @Test
    fun seleccionObligatoria() {
        assertNotNull(Validaciones.seleccion(null))
        assertNull(Validaciones.seleccion(1))
    }
}