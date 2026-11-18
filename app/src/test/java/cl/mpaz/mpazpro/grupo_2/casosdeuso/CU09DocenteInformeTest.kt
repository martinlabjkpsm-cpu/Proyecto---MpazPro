package cl.mpaz.mpazpro.grupo_2.casosdeuso

import android.net.Uri
import cl.mpaz.mpazpro.grupo_2.soporte.Entorno
import cl.mpaz.mpazpro.grupo_2.soporte.casoDeUso
import cl.mpaz.mpazpro.grupo_2.ui.viewmodels.DocenteViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldMatch
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot

// CU09 · El docente revisa el informe reservado: rendimiento, fortalezas y aspectos a reforzar.
// CU10 · El docente valida el informe (comentario + confirmación), con fecha de validación.
// CU11 · El docente descarga el informe (CSV).
// Herramientas: Kotest + MockK (exportador de archivos y Uri) + kotlinx-coroutines-test.

class CU09DocenteInformeTest : StringSpec({

    // Prueba final de Matemática: correctas [1, 0, 2]. est01 saca 2 de 3 y est02 1 de 3.
    suspend fun Entorno.prepararIntentos(): Int {
        val mateId = contenidoDe("Matemática").id
        entrarComo("est01", "1111"); rendirPrueba(mateId, listOf(1, 0, 0))
        entrarComo("est02", "1111"); rendirPrueba(mateId, listOf(1, 1, 1))
        return mateId
    }

    suspend fun Entorno.docenteConInforme(contenidoId: Int): DocenteViewModel {
        entrarComo("docente01", "1234")
        return docenteVm().also { it.cargarInforme(contenidoId); avanzar() }
    }

    suspend fun Entorno.validar(vm: DocenteViewModel) {
        vm.onComentarioCambia("Reforzar conteo de 5 en 5 en clases.")
        vm.onRevisadoCambia(true)
        vm.validarInforme()
        avanzar()
    }

    // ---------- CU09 ----------

    "sin intentos el informe avisa que no hay datos" {
        casoDeUso {
            val vm = docenteConInforme(contenidoDe("Historia").id)
            vm.informe.value.sinDatos shouldBe true
        }
    }

    "el informe calcula el rendimiento general, por pregunta y por estudiante" {
        casoDeUso {
            val informe = docenteConInforme(prepararIntentos()).informe.value
            informe.sinDatos shouldBe false
            informe.porcentajeGeneral shouldBe 50
            informe.logros.map { it.porcentaje } shouldContainExactly listOf(100, 50, 0)
            informe.estudiantes.first { it.nombre == "Estudiante 01" }.mejorPorcentaje shouldBe 66
            informe.estudiantes.first { it.nombre == "Estudiante 02" }.mejorPorcentaje shouldBe 33
        }
    }

    "el informe separa fortalezas y aspectos a reforzar" {
        casoDeUso {
            val informe = docenteConInforme(prepararIntentos()).informe.value
            informe.fortalezas.map { it.enunciado } shouldContainExactly listOf("¿Qué sigue? 400, 500, 600, ___")
            informe.aReforzar.map { it.enunciado } shouldContainExactly listOf("¿Qué número falta? 255, 260, ___, 270")
        }
    }

    "el mejor resultado de cada estudiante considera todos sus intentos" {
        casoDeUso {
            val mateId = prepararIntentos()
            entrarComo("est02", "1111"); rendirPrueba(mateId, listOf(1, 0, 2))
            val est02 = docenteConInforme(mateId).informe.value.estudiantes.first { it.nombre == "Estudiante 02" }
            est02.intentos shouldBe 2
            est02.mejorPorcentaje shouldBe 100
        }
    }

    // ---------- CU10 ----------

    "validar exige un comentario y confirmar la revisión" {
        casoDeUso {
            val vm = docenteConInforme(prepararIntentos())
            vm.validarInforme(); avanzar()
            vm.validacion.value.errorComentario.shouldNotBeNull()
            vm.validacion.value.errorRevisado shouldBe "Debes confirmar que revisaste el informe"

            vm.onComentarioCambia("corto")
            vm.validacion.value.errorComentario shouldBe "Mínimo 10 caracteres"
            contenidoDe("Matemática").informeValidado shouldBe false
        }
    }

    "validar guarda el comentario, la fecha y los intentos revisados" {
        casoDeUso {
            val vm = docenteConInforme(prepararIntentos())
            validar(vm)
            vm.validacion.value.validado shouldBe true
            vm.validacion.value.fechaValidacion.shouldNotBeNull()
            val c = contenidoDe("Matemática")
            c.informeValidado shouldBe true
            c.comentarioDocente shouldBe "Reforzar conteo de 5 en 5 en clases."
            c.intentosAlValidar shouldBe 2
        }
    }

    "si llegan intentos nuevos después de validar hay que validar de nuevo" {
        casoDeUso {
            val mateId = prepararIntentos()
            validar(docenteConInforme(mateId))
            entrarComo("est01", "1111"); rendirPrueba(mateId, listOf(1, 0, 2))

            val otraVez = docenteConInforme(mateId)
            otraVez.validacion.value.intentosNuevos shouldBe 1
            otraVez.validacion.value.validado shouldBe false
        }
    }

    // ---------- CU11 ----------

    "el archivo se llama con el código OA y la fecha" {
        casoDeUso {
            val vm = docenteConInforme(prepararIntentos())
            vm.nombreArchivo() shouldMatch Regex("informe_MA03_OA_01_\\d{2}-\\d{2}-\\d{4}\\.csv")
        }
    }

    "descargar envía el CSV con la advertencia y el estado al exportador" {
        casoDeUso {
            val vm = docenteConInforme(prepararIntentos())
            val csv = slot<String>()
            coEvery { exportador.escribir(any(), capture(csv)) } returns true
            val destino = mockk<Uri>()

            vm.exportar(destino); avanzar()

            coVerify(exactly = 1) { exportador.escribir(destino, any()) }
            csv.captured shouldContain "No constituye diagnóstico ni decisión académica"
            csv.captured shouldContain "Estado de validación;Pendiente de validación"
            csv.captured shouldContain "Estudiante 01;1;66"
            vm.validacion.value.mensajeExportacion shouldBe "✓ Informe descargado"
        }
    }

    "un informe validado se descarga con su fecha de validación" {
        casoDeUso {
            val vm = docenteConInforme(prepararIntentos())
            validar(vm)
            val csv = slot<String>()
            coEvery { exportador.escribir(any(), capture(csv)) } returns true
            vm.exportar(mockk()); avanzar()
            csv.captured shouldContain "Estado de validación;Validado el"
        }
    }

    "si no se puede guardar el archivo se avisa al docente" {
        casoDeUso {
            val vm = docenteConInforme(prepararIntentos())
            coEvery { exportador.escribir(any(), any()) } returns false
            vm.validacion.value.mensajeExportacion.shouldBeNull()
            vm.exportar(mockk()); avanzar()
            vm.validacion.value.mensajeExportacion shouldBe "No se pudo guardar el archivo"
        }
    }
})
