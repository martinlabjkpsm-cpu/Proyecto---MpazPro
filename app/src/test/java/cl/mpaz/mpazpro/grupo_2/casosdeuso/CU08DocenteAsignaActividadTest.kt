package cl.mpaz.mpazpro.grupo_2.casosdeuso

import cl.mpaz.mpazpro.grupo_2.data.local.ContenidoEntity
import cl.mpaz.mpazpro.grupo_2.domain.model.Estado
import cl.mpaz.mpazpro.grupo_2.soporte.casoDeUso
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

// CU08 · El docente selecciona un objetivo, lo asigna al curso y define el máximo de intentos (1 a 5).
// Herramientas: Kotest + kotlinx-coroutines-test.

class CU08DocenteAsignaActividadTest : StringSpec({

    "el docente ve los objetivos disponibles, uno por asignatura" {
        casoDeUso {
            entrarComo("docente01", "1234")
            val vm = docenteVm()
            suscribir(vm.contenidos)
            vm.contenidos.value shouldHaveSize 4
            vm.nombre shouldBe "Docente 3.º A"
        }
    }

    "al abrir la asignación se muestran los intentos y la visibilidad actuales" {
        casoDeUso {
            entrarComo("docente01", "1234")
            val vm = docenteVm()
            vm.cargarAsignacion(contenidoDe("Matemática").id)
            avanzar()
            vm.asignar.value.intentos shouldBe "3"
            vm.asignar.value.asignado shouldBe true
        }
    }

    "los intentos deben estar entre 1 y 5" {
        casoDeUso {
            entrarComo("docente01", "1234")
            val vm = docenteVm()
            vm.cargarAsignacion(contenidoDe("Matemática").id); avanzar()

            vm.onIntentosCambia("0")
            vm.asignar.value.errorIntentos shouldBe "Debe ser entre 1 y 5 intentos"
            vm.guardarAsignacion(); avanzar()
            vm.asignar.value.guardado shouldBe false

            vm.onIntentosCambia("")
            vm.asignar.value.errorIntentos shouldBe "Indica el máximo de intentos"
            contenidoDe("Matemática").maxIntentos shouldBe 3
        }
    }

    "el campo de intentos acepta un solo dígito" {
        casoDeUso {
            val vm = docenteVm()
            vm.onIntentosCambia("45")
            vm.asignar.value.intentos shouldBe "4"
        }
    }

    "guardar la asignación cambia los intentos y la visibilidad del objetivo" {
        casoDeUso {
            entrarComo("docente01", "1234")
            val vm = docenteVm()
            val id = contenidoDe("Matemática").id
            vm.cargarAsignacion(id); avanzar()
            vm.onIntentosCambia("5")
            vm.onAsignadoCambia(false)
            vm.guardarAsignacion(); avanzar()

            vm.asignar.value.guardado shouldBe true
            vm.asignar.value.errorIntentos.shouldBeNull()
            contenidoDe("Matemática").maxIntentos shouldBe 5
            contenidoDe("Matemática").asignado shouldBe false

            vm.cargarAsignacion(id); avanzar()
            vm.onAsignadoCambia(true)
            vm.guardarAsignacion(); avanzar()
            contenidoDe("Matemática").asignado shouldBe true
        }
    }

    "no se puede asignar un objetivo que no tiene desafíos ni prueba final" {
        casoDeUso {
            entrarComo("docente01", "1234")
            val id = repo.guardarContenido(
                ContenidoEntity(asignatura = "Historia", codigoOa = "HI03 OA 07", objetivo = "Objetivo sin preguntas",
                    explicacion = "Sin preguntas aún", estado = Estado.APROBADO)
            )
            val vm = docenteVm()
            vm.cargarAsignacion(id); avanzar()
            vm.onIntentosCambia("2")
            vm.onAsignadoCambia(true)
            vm.guardarAsignacion(); avanzar()

            vm.asignar.value.errorIntentos shouldBe "Agrega al menos un desafío"
            vm.asignar.value.guardado shouldBe false
            dao.todosLosContenidos().first { it.id == id }.asignado shouldBe false
        }
    }
})
