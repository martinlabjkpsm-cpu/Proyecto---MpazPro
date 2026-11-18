package cl.mpaz.mpazpro.grupo_2.casosdeuso

import cl.mpaz.mpazpro.grupo_2.domain.model.Estado
import cl.mpaz.mpazpro.grupo_2.domain.model.TipoActividad
import cl.mpaz.mpazpro.grupo_2.soporte.casoDeUso
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.verify

// CU12 · El estudiante consulta el material asignado (solo contenidos aprobados por UTP y asignados).
// CU13 · El estudiante desarrolla los desafíos progresivos con retroalimentación inmediata.
// Herramientas: Kotest + MockK (vibración) + kotlinx-coroutines-test.

class CU12EstudianteMaterialYDesafiosTest : StringSpec({

    // ---------- CU12 ----------

    "el estudiante solo ve contenidos aprobados por UTP y asignados por el docente" {
        casoDeUso {
            entrarComo("est01", "1111")
            val vm = estudianteVm()
            suscribir(vm.contenidos)
            vm.contenidos.value.map { it.asignatura } shouldContainExactly listOf("Matemática")
            vm.nombre shouldBe "Estudiante 01"

            repo.actualizar(contenidoDe("Lenguaje").copy(asignado = true)); avanzar()
            vm.contenidos.value.map { it.asignatura } shouldContainExactlyInAnyOrder listOf("Matemática", "Lenguaje")

            repo.actualizar(contenidoDe("Historia").copy(asignado = true, estado = Estado.PENDIENTE)); avanzar()
            vm.contenidos.value.any { it.asignatura == "Historia" } shouldBe false
        }
    }

    "al abrir un desafío se carga la explicación del objetivo como pista" {
        casoDeUso {
            entrarComo("est01", "1111")
            val vm = estudianteVm()
            vm.iniciarActividad(contenidoDe("Matemática").id, TipoActividad.DESAFIO)
            avanzar()
            vm.pista.value shouldBe contenidoDe("Matemática").explicacion
            vm.cargando.value shouldBe false
        }
    }

    "mientras se cargan las preguntas la pantalla sabe que está cargando" {
        casoDeUso {
            entrarComo("est01", "1111")
            val vm = estudianteVm()
            vm.iniciarActividad(contenidoDe("Matemática").id, TipoActividad.DESAFIO)
            vm.preguntas.value shouldBe emptyList()          // todavía no corre la corrutina
            avanzar()
            vm.preguntas.value.size shouldBe 3
        }
    }

    // ---------- CU13 ----------

    "los desafíos van de menor a mayor dificultad" {
        casoDeUso {
            entrarComo("est01", "1111")
            val vm = estudianteVm()
            vm.iniciarActividad(contenidoDe("Matemática").id, TipoActividad.DESAFIO); avanzar()
            vm.preguntas.value.map { it.dificultad } shouldContainExactly listOf(1, 2, 3)
        }
    }

    "no puede continuar sin elegir una alternativa" {
        casoDeUso {
            entrarComo("est01", "1111")
            val vm = estudianteVm()
            vm.iniciarActividad(contenidoDe("Matemática").id, TipoActividad.DESAFIO); avanzar()
            vm.comprobar()
            vm.actividad.value.errorSeleccion shouldBe "Elige una alternativa antes de continuar"
            vm.actividad.value.indice shouldBe 0
        }
    }

    "en un desafío ve si acertó antes de avanzar y no puede cambiar su respuesta" {
        casoDeUso {
            entrarComo("est01", "1111")
            val vm = estudianteVm()
            vm.iniciarActividad(contenidoDe("Matemática").id, TipoActividad.DESAFIO); avanzar()
            vm.seleccionar(1)
            vm.comprobar()
            vm.actividad.value.comprobada shouldBe true
            vm.actividad.value.indice shouldBe 0
            verify(exactly = 1) { vibracion.correcto() }

            vm.seleccionar(2)
            vm.actividad.value.seleccion shouldBe 1

            vm.siguiente()
            vm.actividad.value.indice shouldBe 1
            vm.actividad.value.seleccion.shouldBeNull()
        }
    }

    "al terminar los desafíos se guarda el avance con cada respuesta" {
        casoDeUso {
            entrarComo("est01", "1111")
            val vm = estudianteVm()
            vm.iniciarActividad(contenidoDe("Matemática").id, TipoActividad.DESAFIO); avanzar()
            listOf(1, 0, 0).forEach { vm.seleccionar(it); vm.comprobar(); vm.siguiente() }   // correctas [1, 0, 1]
            avanzar()

            val a = vm.actividad.value
            a.terminada shouldBe true
            a.correctas shouldBe 2
            a.revision.map { it.esCorrecta } shouldContainExactly listOf(true, true, false)
            a.mostrarCorrectas shouldBe true
            a.intentosRestantes.shouldBeNull()

            val intento = dao.todosLosIntentos().single()
            intento.tipo shouldBe TipoActividad.DESAFIO
            intento.correctas shouldBe 2
            dao.todasLasRespuestas().map { it.opcionElegida } shouldContainExactly listOf(1, 0, 0)
        }
    }

    "volver a la misma actividad en curso no la reinicia" {
        casoDeUso {
            entrarComo("est01", "1111")
            val vm = estudianteVm()
            val id = contenidoDe("Matemática").id
            vm.iniciarActividad(id, TipoActividad.DESAFIO); avanzar()
            vm.seleccionar(1); vm.comprobar(); vm.siguiente()
            vm.iniciarActividad(id, TipoActividad.DESAFIO); avanzar()     // p. ej. al girar la pantalla
            vm.actividad.value.indice shouldBe 1
        }
    }
})
