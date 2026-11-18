package cl.mpaz.mpazpro.grupo_2.casosdeuso

import android.graphics.Bitmap
import cl.mpaz.mpazpro.grupo_2.domain.model.TipoActividad
import cl.mpaz.mpazpro.grupo_2.soporte.Entorno
import cl.mpaz.mpazpro.grupo_2.soporte.casoDeUso
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder

// CU17 · La app confirma la finalización de la actividad (aviso), da retroalimentación con vibración
//        y ofrece audio de apoyo (lectura en voz alta). Caso 3.3.
// Herramientas: Kotest + MockK (verify de los recursos nativos simulados) + kotlinx-coroutines-test.
// Los recursos reales de Android se prueban en androidTest (RecursosNativosTest).

class CU17ConfirmacionYAudioTest : StringSpec({

    fun Entorno.terminarDesafios(opciones: List<Int>) {
        val vm = estudianteVm()
        vm.iniciarActividad(contenidoDe("Matemática").id, TipoActividad.DESAFIO); avanzar()
        opciones.forEach { vm.seleccionar(it); vm.comprobar(); vm.siguiente() }
        avanzar()
        vm.actividad.value.terminada shouldBe true
    }

    "al terminar una actividad se confirma con un aviso" {
        casoDeUso {
            entrarComo("est01", "1111")
            terminarDesafios(listOf(1, 0, 1))
            verify(exactly = 1) {
                avisos.mostrar("¡Actividad terminada!", "3 de 3 correctas. Tu avance quedó guardado.")
            }
        }
    }

    "en los desafíos vibra distinto si acierta o se equivoca" {
        casoDeUso {
            entrarComo("est01", "1111")
            terminarDesafios(listOf(1, 1, 1))                // correctas [1, 0, 1]
            verifyOrder {
                vibracion.correcto()
                vibracion.incorrecto()
                vibracion.correcto()
            }
        }
    }

    "en la prueba final no vibra para no dar pistas" {
        casoDeUso {
            entrarComo("est01", "1111")
            val vm = estudianteVm()
            vm.iniciarActividad(contenidoDe("Matemática").id, TipoActividad.PRUEBA); avanzar()
            listOf(1, 0, 2).forEach { vm.seleccionar(it); vm.comprobar() }
            avanzar()
            verify(exactly = 0) { vibracion.correcto() }
            verify(exactly = 0) { vibracion.incorrecto() }
        }
    }

    "el estudiante puede activar y detener la lectura en voz alta" {
        casoDeUso {
            entrarComo("est01", "1111")
            val vm = estudianteVm()
            vm.leerEnVozAlta("Contar de 10 en 10")
            verify { voz.leer("Contar de 10 en 10") }
            vm.hablando.value shouldBe true

            vm.leerEnVozAlta("Contar de 10 en 10")           // segundo toque: detiene
            verify(exactly = 1) { voz.leer(any()) }
            vm.hablando.value shouldBe false
        }
    }

    "al cerrar sesión se detiene el audio" {
        casoDeUso {
            entrarComo("est01", "1111")
            val vm = estudianteVm()
            vm.leerEnVozAlta("Hola")
            vm.cerrarSesion()
            verify { voz.detener() }
            vm.hablando.value shouldBe false
        }
    }

    "la foto de perfil se carga al entrar y se guarda por estudiante" {
        casoDeUso {
            val est = entrarComo("est01", "1111")
            val vm = estudianteVm()
            coVerify { foto.cargar(est.id) }

            val nueva = mockk<Bitmap>()
            vm.guardarFoto(nueva); avanzar()
            coVerify { foto.guardar(est.id, nueva) }
            vm.foto.value shouldBe nueva
        }
    }
})
