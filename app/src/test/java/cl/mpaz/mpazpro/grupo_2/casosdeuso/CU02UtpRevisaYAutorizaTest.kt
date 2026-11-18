package cl.mpaz.mpazpro.grupo_2.casosdeuso

import cl.mpaz.mpazpro.grupo_2.data.local.ContenidoEntity
import cl.mpaz.mpazpro.grupo_2.data.local.PreguntaEntity
import cl.mpaz.mpazpro.grupo_2.domain.model.Estado
import cl.mpaz.mpazpro.grupo_2.domain.model.TipoActividad
import cl.mpaz.mpazpro.grupo_2.soporte.Entorno
import cl.mpaz.mpazpro.grupo_2.soporte.casoDeUso
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

// CU02 · La jefatura UTP revisa los contenidos y sus preguntas.
// CU03 · La UTP aprueba (autoriza) un contenido completo.
// CU04 · La UTP rechaza un contenido con una observación.
// CU05 · La UTP elimina un contenido.
// Herramientas: Kotest + kotlinx-coroutines-test (UtpViewModel real con base en memoria).

class CU02UtpRevisaYAutorizaTest : StringSpec({

    // Un contenido nuevo PENDIENTE propuesto por el docente
    suspend fun Entorno.crearPendiente(): Int = repo.guardarContenido(
        ContenidoEntity(
            asignatura = "Matemática", unidad = "Unidad 1: Números hasta 1 000", eje = "Números",
            codigoOa = "MA03 OA 02", objetivo = "Leer y escribir números hasta 1 000.",
            explicacion = "Los números se leen de izquierda a derecha por centenas.", estado = Estado.PENDIENTE
        )
    )

    suspend fun Entorno.agregarPregunta(contenidoId: Int, tipo: TipoActividad) = repo.agregarPregunta(
        PreguntaEntity(0, contenidoId, tipo, "¿Cómo se lee 305?", "Trescientos cinco", "Treinta y cinco",
            "Tres cero cinco", 0, 1, "3 centenas y 5 unidades.")
    )

    fun Entorno.contenido(id: Int) = dao.todosLosContenidos().first { it.id == id }

    "la UTP ve todos los contenidos, también los pendientes" {
        casoDeUso {
            entrarComo("utp01", "1234")
            val id = crearPendiente()
            val vm = utpVm()
            suscribir(vm.contenidos)
            vm.contenidos.value shouldHaveSize 5
            vm.contenidos.value.first { it.id == id }.estado shouldBe Estado.PENDIENTE
            vm.nombre shouldBe "Jefatura UTP"
        }
    }

    "al abrir la revisión se cargan las preguntas del contenido" {
        casoDeUso {
            entrarComo("utp01", "1234")
            val vm = utpVm()
            vm.abrirRevision(contenidoDe("Matemática"))
            avanzar()
            vm.revision.value.preguntas shouldHaveSize 6      // 3 desafíos + 3 de prueba final
            vm.cerrarRevision()
            vm.revision.value.contenidoId.shouldBeNull()
        }
    }

    "aprobar un contenido con desafíos y prueba final lo deja APROBADO" {
        casoDeUso {
            entrarComo("utp01", "1234")
            val id = crearPendiente()
            agregarPregunta(id, TipoActividad.DESAFIO)
            agregarPregunta(id, TipoActividad.PRUEBA)
            val vm = utpVm()
            suscribir(vm.contenidos)

            vm.abrirRevision(contenido(id))
            avanzar()
            vm.onObservacionCambia("Bien alineado al OA")
            vm.aprobar()
            avanzar()

            contenido(id).estado shouldBe Estado.APROBADO
            contenido(id).observacionUtp shouldBe "Bien alineado al OA"
            vm.revision.value.contenidoId.shouldBeNull()
        }
    }

    "no se puede aprobar un contenido sin desafíos o sin prueba final" {
        casoDeUso {
            entrarComo("utp01", "1234")
            val id = crearPendiente()
            val vm = utpVm()
            suscribir(vm.contenidos)

            vm.abrirRevision(contenido(id)); avanzar()
            vm.aprobar(); avanzar()
            vm.revision.value.errorAprobacion shouldBe "No se puede aprobar: Agrega al menos un desafío"

            agregarPregunta(id, TipoActividad.DESAFIO)
            vm.abrirRevision(contenido(id)); avanzar()
            vm.aprobar(); avanzar()
            vm.revision.value.errorAprobacion shouldBe "No se puede aprobar: Agrega al menos una pregunta de prueba final"
            contenido(id).estado shouldBe Estado.PENDIENTE
        }
    }

    "para rechazar hay que explicar el motivo" {
        casoDeUso {
            entrarComo("utp01", "1234")
            val id = crearPendiente()
            val vm = utpVm()
            suscribir(vm.contenidos)
            vm.abrirRevision(contenido(id)); avanzar()
            vm.onObservacionCambia("Corto")
            vm.rechazar(); avanzar()
            vm.revision.value.errorObservacion.shouldNotBeNull()
            contenido(id).estado shouldBe Estado.PENDIENTE
        }
    }

    "rechazar guarda la observación y quita el contenido del curso" {
        casoDeUso {
            entrarComo("utp01", "1234")
            val vm = utpVm()
            suscribir(vm.contenidos)
            contenidoDe("Matemática").asignado shouldBe true

            vm.abrirRevision(contenidoDe("Matemática")); avanzar()
            vm.onObservacionCambia("  El ejemplo no corresponde al objetivo  ")
            vm.rechazar(); avanzar()

            val despues = contenidoDe("Matemática")
            despues.estado shouldBe Estado.RECHAZADO
            despues.observacionUtp shouldBe "El ejemplo no corresponde al objetivo"
            despues.asignado shouldBe false
        }
    }

    "la observación no puede pasar de 200 caracteres" {
        casoDeUso {
            entrarComo("utp01", "1234")
            val vm = utpVm()
            vm.abrirRevision(contenidoDe("Lenguaje"))
            vm.onObservacionCambia("a".repeat(201))
            vm.revision.value.errorObservacion shouldBe "Máximo 200 caracteres"
        }
    }

    "eliminar borra el contenido y todas sus preguntas" {
        casoDeUso {
            entrarComo("utp01", "1234")
            val vm = utpVm()
            val mate = contenidoDe("Matemática")
            vm.eliminar(mate)
            avanzar()
            dao.todosLosContenidos().map { it.id } shouldNotContain mate.id
            dao.todasLasPreguntasGuardadas().filter { it.contenidoId == mate.id } shouldHaveSize 0
        }
    }
})
