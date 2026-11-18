package cl.mpaz.mpazpro.grupo_2.casosdeuso

import cl.mpaz.mpazpro.grupo_2.data.local.DatosIniciales
import cl.mpaz.mpazpro.grupo_2.domain.model.Estado
import cl.mpaz.mpazpro.grupo_2.domain.model.Logros
import cl.mpaz.mpazpro.grupo_2.domain.model.Rol
import cl.mpaz.mpazpro.grupo_2.domain.model.TipoActividad
import cl.mpaz.mpazpro.grupo_2.soporte.casoDeUso
import cl.mpaz.mpazpro.grupo_2.utils.Validaciones
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

// CU16 · Se conserva el progreso del estudiante y se muestran sus logros (estrellas y medallas).
// CU18 · La app trabaja con datos ficticios: una unidad por cada asignatura y tres perfiles (caso 5.1).
// Herramientas: Kotest + kotlinx-coroutines-test.

class CU16ProgresoYLogrosTest : StringSpec({

    // ---------- CU16 ----------

    "el progreso queda guardado para la próxima sesión" {
        casoDeUso {
            entrarComo("est01", "1111")
            val mateId = contenidoDe("Matemática").id
            hacerDesafio(mateId, listOf(1, 0, 1))
            repo.cerrarSesion()

            entrarComo("est01", "1111")                      // vuelve otro día
            val vm = estudianteVm()
            suscribir(vm.intentos)
            vm.intentos.value shouldHaveSize 1
            vm.desafioHecho(mateId, vm.intentos.value) shouldBe true
        }
    }

    "cada estudiante ve solo su propio avance" {
        casoDeUso {
            entrarComo("est01", "1111")
            hacerDesafio(contenidoDe("Matemática").id, listOf(1, 0, 1))
            entrarComo("est02", "1111")
            val vm = estudianteVm()
            suscribir(vm.intentos)
            vm.intentos.value.shouldBeEmpty()
        }
    }

    "gana una medalla al aprobar la prueba final y repetirla no suma otra" {
        casoDeUso {
            entrarComo("est01", "1111")
            val mateId = contenidoDe("Matemática").id
            val vm = estudianteVm()
            suscribir(vm.intentos)

            rendirPrueba(mateId, listOf(0, 0, 0)); avanzar()
            vm.medallas(vm.intentos.value) shouldBe 0
            rendirPrueba(mateId, listOf(1, 0, 2))
            rendirPrueba(mateId, listOf(1, 0, 2)); avanzar()
            vm.medallas(vm.intentos.value) shouldBe 1
            Logros.mejorPorcentaje(mateId, TipoActividad.PRUEBA, vm.intentos.value) shouldBe 100
            Logros.mejorPorcentaje(mateId, TipoActividad.DESAFIO, vm.intentos.value).shouldBeNull()
        }
    }

    // ---------- CU18 ----------

    "hay una sola unidad por cada una de las cuatro asignaturas" {
        casoDeUso {
            val contenidos = dao.todosLosContenidos()
            contenidos.map { it.asignatura }.toSet() shouldBe Validaciones.ASIGNATURAS.keys
            contenidos.groupBy { it.asignatura }.values.forEach { lista ->
                lista.map { it.unidad }.distinct() shouldHaveSize 1
            }
        }
    }

    "los contenidos aprobados tienen desafíos, prueba final y código OA oficial" {
        casoDeUso {
            dao.todosLosContenidos().filter { it.estado == Estado.APROBADO }.forEach { c ->
                Validaciones.preguntasMinimas(repo.todasLasPreguntas(c.id)).shouldBeNull()
                Validaciones.codigoOa(c.codigoOa, c.asignatura).shouldBeNull()
            }
        }
    }

    "existen usuarios ficticios de los tres perfiles" {
        casoDeUso {
            val roles = listOf("utp01" to "1234", "docente01" to "1234", "est01" to "1111", "est02" to "1111")
                .map { (u, p) -> entrarComo(u, p).rol }
            roles shouldContainExactly listOf(Rol.UTP, Rol.DOCENTE, Rol.ESTUDIANTE, Rol.ESTUDIANTE)
        }
    }

    "los datos iniciales no se duplican al abrir la app otra vez" {
        casoDeUso {
            val antes = dao.todosLosContenidos().size
            DatosIniciales.cargarSiEstaVacia(dao)
            entrarComo("est01", "1111")                       // el login también intenta cargarlos
            dao.todosLosContenidos() shouldHaveSize antes
        }
    }
})
