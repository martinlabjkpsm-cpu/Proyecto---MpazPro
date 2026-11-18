package cl.mpaz.mpazpro.grupo_2.casosdeuso

import cl.mpaz.mpazpro.grupo_2.domain.model.TipoActividad
import cl.mpaz.mpazpro.grupo_2.soporte.Entorno
import cl.mpaz.mpazpro.grupo_2.soporte.casoDeUso
import cl.mpaz.mpazpro.grupo_2.ui.viewmodels.EstudianteViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

// CU14 · El estudiante responde la prueba final dentro de los intentos permitidos.
// CU15 · El estudiante revisa sus respuestas correctas e incorrectas.
// Herramientas: Kotest + kotlinx-coroutines-test.

class CU14EstudiantePruebaFinalTest : StringSpec({

    // Matemática: máximo 3 intentos; correctas de la prueba final [1, 0, 2]
    fun Entorno.responderPrueba(vm: EstudianteViewModel, opciones: List<Int>) {
        vm.iniciarActividad(contenidoDe("Matemática").id, TipoActividad.PRUEBA)
        avanzar()
        opciones.forEach { vm.seleccionar(it); vm.comprobar() }
        avanzar()
    }

    // ---------- CU14 ----------

    "primero debe completar los desafíos para rendir la prueba final" {
        casoDeUso {
            entrarComo("est01", "1111")
            val vm = estudianteVm()
            suscribir(vm.intentos)
            val mate = contenidoDe("Matemática")
            vm.puedeRendirPrueba(mate, vm.intentos.value) shouldBe false

            hacerDesafio(mate.id, listOf(1, 0, 1)); avanzar()
            vm.desafioHecho(mate.id, vm.intentos.value) shouldBe true
            vm.puedeRendirPrueba(mate, vm.intentos.value) shouldBe true
        }
    }

    "no puede rendir más veces que el máximo de intentos" {
        casoDeUso {
            entrarComo("est01", "1111")
            val vm = estudianteVm()
            suscribir(vm.intentos)
            val mate = contenidoDe("Matemática")
            hacerDesafio(mate.id, listOf(1, 0, 1))
            repeat(3) { rendirPrueba(mate.id, listOf(0, 0, 0)) }
            avanzar()
            vm.intentosUsados(mate.id, vm.intentos.value) shouldBe 3
            vm.puedeRendirPrueba(mate, vm.intentos.value) shouldBe false
        }
    }

    "en la prueba no hay retroalimentación: pasa directo a la siguiente pregunta" {
        casoDeUso {
            entrarComo("est01", "1111")
            val vm = estudianteVm()
            vm.iniciarActividad(contenidoDe("Matemática").id, TipoActividad.PRUEBA); avanzar()
            vm.seleccionar(1)
            vm.comprobar()
            vm.actividad.value.comprobada shouldBe false
            vm.actividad.value.indice shouldBe 1
        }
    }

    "al terminar se guarda el intento y se informan los intentos restantes" {
        casoDeUso {
            entrarComo("est01", "1111")
            val vm = estudianteVm()
            responderPrueba(vm, listOf(1, 0, 2))
            vm.actividad.value.terminada shouldBe true
            vm.actividad.value.correctas shouldBe 3
            vm.actividad.value.intentosRestantes shouldBe 2
            dao.todosLosIntentos().single().tipo shouldBe TipoActividad.PRUEBA
        }
    }

    "salir de la prueba sin terminar gasta el intento" {
        casoDeUso {
            entrarComo("est01", "1111")
            val vm = estudianteVm()
            vm.iniciarActividad(contenidoDe("Matemática").id, TipoActividad.PRUEBA); avanzar()
            vm.seleccionar(1); vm.comprobar()
            vm.abandonarPrueba(); avanzar()

            vm.actividad.value.terminada shouldBe true
            val intento = dao.todosLosIntentos().single()
            intento.correctas shouldBe 1
            intento.total shouldBe 3
            dao.todasLasRespuestas().map { it.opcionElegida } shouldContainExactly listOf(1, -1, -1)
        }
    }

    "salir de un desafío no gasta intentos" {
        casoDeUso {
            entrarComo("est01", "1111")
            val vm = estudianteVm()
            vm.iniciarActividad(contenidoDe("Matemática").id, TipoActividad.DESAFIO); avanzar()
            vm.abandonarPrueba(); avanzar()
            dao.todosLosIntentos().shouldBeEmpty()
        }
    }

    // ---------- CU15 ----------

    "revisa cada respuesta: qué marcó, cuál era la correcta y la ayuda" {
        casoDeUso {
            entrarComo("est01", "1111")
            val vm = estudianteVm()
            responderPrueba(vm, listOf(1, 1, 2))
            val revision = vm.actividad.value.revision
            revision.map { it.esCorrecta } shouldContainExactly listOf(true, false, true)
            revision[1].elegida shouldBe 1
            revision[1].correcta shouldBe 0
            revision[1].ayuda shouldBe "502 + 10 = 512."
        }
    }

    "si reprueba y le quedan intentos no se revela la respuesta correcta" {
        casoDeUso {
            entrarComo("est01", "1111")
            val vm = estudianteVm()
            responderPrueba(vm, listOf(0, 1, 0))
            vm.actividad.value.mostrarCorrectas shouldBe false
            vm.actividad.value.intentosRestantes shouldBe 2
        }
    }

    "si aprueba con 70 % o más se revela la respuesta correcta" {
        casoDeUso {
            entrarComo("est01", "1111")
            val vm = estudianteVm()
            responderPrueba(vm, listOf(1, 0, 0))          // 66 %: no aprueba
            vm.actividad.value.mostrarCorrectas shouldBe false
            responderPrueba(vm, listOf(1, 0, 2))          // 100 %
            vm.actividad.value.mostrarCorrectas shouldBe true
        }
    }

    "al agotar los intentos se revela la respuesta correcta" {
        casoDeUso {
            entrarComo("est01", "1111")
            val vm = estudianteVm()
            val id = contenidoDe("Matemática").id
            rendirPrueba(id, listOf(0, 0, 0))
            rendirPrueba(id, listOf(0, 0, 0))
            responderPrueba(vm, listOf(0, 0, 0))          // tercer y último intento
            vm.actividad.value.intentosRestantes shouldBe 0
            vm.actividad.value.mostrarCorrectas shouldBe true
        }
    }
})
