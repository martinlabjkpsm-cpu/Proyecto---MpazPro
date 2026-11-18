package cl.mpaz.mpazpro.grupo_2.navegacion

import cl.mpaz.mpazpro.grupo_2.domain.model.Rol
import cl.mpaz.mpazpro.grupo_2.navigation.Rutas
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

// CU01 y restricción del caso (3.1 y 5.1): cada perfil entra a su pantalla y el informe
// solo existe dentro de las rutas del docente. Herramienta: JUnit 5.

class RutasPorPerfilTest {

    @Test
    @DisplayName("cada perfil entra a su propia pantalla de inicio")
    fun cadaPerfilVaASuPantalla() {
        assertEquals(Rutas.UTP, Rutas.inicio(Rol.UTP))
        assertEquals(Rutas.DOCENTE, Rutas.inicio(Rol.DOCENTE))
        assertEquals(Rutas.ESTUDIANTE, Rutas.inicio(Rol.ESTUDIANTE))
    }

    @Test
    @DisplayName("el informe y la edición de contenidos solo están bajo las rutas del docente")
    fun informeSoloParaDocente() {
        assertTrue(Rutas.informe(3).startsWith(Rutas.DOCENTE + "/"))
        assertTrue(Rutas.contenidoForm(3).startsWith(Rutas.DOCENTE + "/"))
        assertTrue(Rutas.asignar(3).startsWith(Rutas.DOCENTE + "/"))
        assertFalse(Rutas.informe(3).startsWith(Rutas.ESTUDIANTE))
    }

    @Test
    @DisplayName("las actividades del estudiante llevan el objetivo y el tipo en la ruta")
    fun rutaDeActividad() {
        assertEquals("estudiante/actividad/4/PRUEBA", Rutas.actividad(4, "PRUEBA"))
        assertEquals("docente/contenido/0", Rutas.contenidoForm())
    }
}
