package cl.mpaz.mpazpro.grupo_2.casosdeuso

import cl.mpaz.mpazpro.grupo_2.domain.model.CampoContenido
import cl.mpaz.mpazpro.grupo_2.domain.model.CampoPregunta
import cl.mpaz.mpazpro.grupo_2.domain.model.Estado
import cl.mpaz.mpazpro.grupo_2.domain.model.TipoActividad
import cl.mpaz.mpazpro.grupo_2.soporte.casoDeUso
import cl.mpaz.mpazpro.grupo_2.ui.viewmodels.ContenidoFormViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldContainKeys
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

// CU06 · El docente propone o corrige un contenido curricular (asignatura, unidad, eje, código OA,
//        objetivo, explicación, ejemplo y recurso de referencia).
// CU07 · El docente agrega y elimina preguntas (desafíos y prueba final).
// Reglas del caso: una unidad por asignatura y todo cambio vuelve a revisión UTP.
// Herramientas: Kotest + kotlinx-coroutines-test.

class CU06DocenteGestionaContenidoTest : StringSpec({

    fun ContenidoFormViewModel.llenarValido() {
        cargar(0)
        onCambio(CampoContenido.ASIGNATURA, "Matemática")
        onCambio(CampoContenido.UNIDAD, "Unidad 1: Números hasta 1 000")
        onCambio(CampoContenido.EJE, "Números y operaciones")
        onCambio(CampoContenido.CODIGO, "ma03 oa 02")
        onCambio(CampoContenido.OBJETIVO, "Leer números hasta el 1 000 y escribirlos con palabras.")
        onCambio(CampoContenido.EXPLICACION, "Cada número tiene centenas, decenas y unidades que se leen en orden.")
        onCambio(CampoContenido.EJEMPLO, "305 se lee trescientos cinco.")
        onCambio(CampoContenido.RECURSO, "Currículum Nacional (Mineduc)")
    }

    fun ContenidoFormViewModel.llenarPregunta(tipo: TipoActividad, enunciado: String) = onPregunta {
        it.copy(tipo = tipo, enunciado = enunciado, opcionA = "Trescientos cinco", opcionB = "Treinta y cinco",
            opcionC = "Tres mil cinco", correcta = 0, dificultad = 2, ayuda = "3 centenas y 5 unidades.")
    }

    // ---------- CU06 ----------

    "un contenido válido se guarda PENDIENTE de revisión UTP y sin asignar" {
        casoDeUso {
            entrarComo("docente01", "1234")
            val vm = formularioVm()
            vm.llenarValido()
            vm.estado.value.codigoOa shouldBe "MA03 OA 02"      // se pasa a mayúsculas
            vm.estado.value.errores.shouldBeEmpty()

            vm.guardar()
            avanzar()

            vm.estado.value.guardado shouldBe true
            val guardado = dao.todosLosContenidos().first { it.id == vm.estado.value.id }
            guardado.estado shouldBe Estado.PENDIENTE
            guardado.asignado shouldBe false
            guardado.recursoReferencia shouldBe "Currículum Nacional (Mineduc)"
            vm.estado.value.errorPreguntas shouldBe "Agrega al menos un desafío"
        }
    }

    "un formulario incompleto muestra errores y no se guarda" {
        casoDeUso {
            entrarComo("docente01", "1234")
            val antes = dao.todosLosContenidos().size
            val vm = formularioVm()
            vm.cargar(0)
            vm.guardar()
            avanzar()
            vm.estado.value.errores.shouldContainKeys(
                CampoContenido.ASIGNATURA, CampoContenido.OBJETIVO, CampoContenido.EXPLICACION, CampoContenido.RECURSO
            )
            vm.estado.value.guardado shouldBe false
            dao.todosLosContenidos() shouldHaveSize antes
        }
    }

    "solo se permite una unidad por asignatura" {
        casoDeUso {
            entrarComo("docente01", "1234")
            val vm = formularioVm()
            vm.llenarValido()
            vm.onCambio(CampoContenido.UNIDAD, "Unidad 2: Fracciones")
            vm.guardar()
            avanzar()
            vm.estado.value.errores shouldContainKey CampoContenido.UNIDAD
            vm.estado.value.guardado shouldBe false
        }
    }

    "al cambiar la asignatura se vuelve a revisar el código OA" {
        casoDeUso {
            val vm = formularioVm()
            vm.llenarValido()
            vm.onCambio(CampoContenido.ASIGNATURA, "Lenguaje")
            vm.estado.value.errores[CampoContenido.CODIGO] shouldBe "Para Lenguaje el código empieza con LE"
            vm.onCambio(CampoContenido.CODIGO, "LE03 OA 05")
            vm.estado.value.errores shouldNotContainKey CampoContenido.CODIGO
        }
    }

    "editar un contenido aprobado lo devuelve a revisión y deja de verse en el curso" {
        casoDeUso {
            entrarComo("docente01", "1234")
            val mate = contenidoDe("Matemática")
            val vm = formularioVm()
            vm.cargar(mate.id)
            avanzar()
            vm.estado.value.codigoOa shouldBe "MA03 OA 01"
            vm.preguntas.value shouldHaveSize 6

            vm.onCambio(CampoContenido.EJEMPLO, "De 10 en 10 desde 120: 130, 140, 150.")
            vm.guardar()
            avanzar()

            val despues = contenidoDe("Matemática")
            despues.estado shouldBe Estado.PENDIENTE
            despues.asignado shouldBe false
            despues.ejemplo shouldBe "De 10 en 10 desde 120: 130, 140, 150."
            despues.maxIntentos shouldBe mate.maxIntentos
        }
    }

    // ---------- CU07 ----------

    "una pregunta incompleta muestra sus errores y no se agrega" {
        casoDeUso {
            entrarComo("docente01", "1234")
            val vm = formularioVm()
            vm.llenarValido()
            vm.guardar(); avanzar()
            vm.agregarPregunta(); avanzar()
            vm.pregunta.value.intentado shouldBe true
            vm.pregunta.value.errores.shouldContainKeys(CampoPregunta.ENUNCIADO, CampoPregunta.CORRECTA, CampoPregunta.AYUDA)
            vm.preguntas.value.shouldBeEmpty()
        }
    }

    "agregar un desafío y una pregunta de prueba completa el contenido" {
        casoDeUso {
            entrarComo("docente01", "1234")
            val vm = formularioVm()
            vm.llenarValido()
            vm.guardar(); avanzar()
            val id = vm.estado.value.id

            vm.llenarPregunta(TipoActividad.DESAFIO, "¿Cómo se lee 305?")
            vm.agregarPregunta(); avanzar()
            vm.estado.value.errorPreguntas shouldBe "Agrega al menos una pregunta de prueba final"
            vm.pregunta.value.enunciado shouldBe ""            // el formulario se limpia

            vm.llenarPregunta(TipoActividad.PRUEBA, "¿Cuál es el número 305?")
            vm.agregarPregunta(); avanzar()
            vm.estado.value.errorPreguntas.shouldBeNull()

            val guardadas = dao.todasLasPreguntasGuardadas().filter { it.contenidoId == id }
            guardadas.map { it.tipo } shouldContainExactlyInAnyOrder listOf(TipoActividad.DESAFIO, TipoActividad.PRUEBA)
        }
    }

    "agregar una pregunta a un contenido aprobado lo devuelve a revisión" {
        casoDeUso {
            entrarComo("docente01", "1234")
            val vm = formularioVm()
            vm.cargar(contenidoDe("Matemática").id); avanzar()
            vm.llenarPregunta(TipoActividad.DESAFIO, "¿Cómo se lee 305?")
            vm.agregarPregunta(); avanzar()
            contenidoDe("Matemática").estado shouldBe Estado.PENDIENTE
            vm.preguntas.value shouldHaveSize 7
        }
    }

    "eliminar una pregunta vuelve a revisión y avisa si falta la prueba final" {
        casoDeUso {
            entrarComo("docente01", "1234")
            val vm = formularioVm()
            vm.cargar(contenidoDe("Ciencias Naturales").id); avanzar()     // 1 desafío y 1 prueba
            val prueba = vm.preguntas.value.first { it.tipo == TipoActividad.PRUEBA }
            vm.eliminarPregunta(prueba); avanzar()

            vm.estado.value.errorPreguntas shouldBe "Agrega al menos una pregunta de prueba final"
            contenidoDe("Ciencias Naturales").estado shouldBe Estado.PENDIENTE
        }
    }

    "no se agregan preguntas antes de guardar el contenido" {
        casoDeUso {
            val antes = dao.todasLasPreguntasGuardadas().size
            val vm = formularioVm()
            vm.cargar(0)
            vm.llenarPregunta(TipoActividad.DESAFIO, "¿Cómo se lee 305?")
            vm.agregarPregunta(); avanzar()
            dao.todasLasPreguntasGuardadas() shouldHaveSize antes
        }
    }
})
