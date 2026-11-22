package cl.mpaz.mpazpro.grupo_2.microservicios

import cl.mpaz.mpazpro.grupo_2.data.local.DatosIniciales
import cl.mpaz.mpazpro.grupo_2.data.local.Preferencias
import cl.mpaz.mpazpro.grupo_2.data.remote.microservicios.ContenidoDto
import cl.mpaz.mpazpro.grupo_2.data.remote.microservicios.ContenidosApi
import cl.mpaz.mpazpro.grupo_2.data.remote.microservicios.IntentoDto
import cl.mpaz.mpazpro.grupo_2.data.remote.microservicios.ResultadosApi
import cl.mpaz.mpazpro.grupo_2.data.repository.MPazRepository
import cl.mpaz.mpazpro.grupo_2.domain.model.Estado
import cl.mpaz.mpazpro.grupo_2.domain.model.TipoActividad
import cl.mpaz.mpazpro.grupo_2.nativo.ServicioFoto
import cl.mpaz.mpazpro.grupo_2.nativo.ServicioVoz
import cl.mpaz.mpazpro.grupo_2.soporte.FakeDao
import cl.mpaz.mpazpro.grupo_2.ui.viewmodels.DocenteViewModel
import cl.mpaz.mpazpro.grupo_2.ui.viewmodels.EstudianteViewModel
import cl.mpaz.mpazpro.grupo_2.ui.viewmodels.UtpViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

// Datos "en tiempo real": cada pantalla principal (docente, UTP, estudiante) y el informe
// vuelven a pedir los datos al servidor al abrirse, sin cerrar sesión.
// Herramientas: Kotest + MockK + kotlinx-coroutines-test (Dispatchers.setMain + advanceUntilIdle).

@OptIn(ExperimentalCoroutinesApi::class)
class RefrescoPantallasTest : StringSpec({

    fun contenidoDto(id: Int, estado: Estado) = ContenidoDto(
        id = id, asignatura = "Matemática", unidad = "Unidad 1", codigoOa = "MA03 OA $id",
        objetivo = "Objetivo", explicacion = "Explicación", estado = estado, asignado = true
    )

    class Entorno {
        val dao = FakeDao()
        val contenidosApi = mockk<ContenidosApi>()
        val resultadosApi = mockk<ResultadosApi>()
        val repo = MPazRepository(dao, mockk<Preferencias>(relaxed = true), contenidosApi, resultadosApi)
    }

    // Main usa el mismo reloj virtual que runTest (igual que soporte/Entorno.kt)
    fun conMain(cuerpo: suspend TestScope.(Entorno) -> Unit) {
        Dispatchers.setMain(StandardTestDispatcher())
        try {
            runTest {
                val e = Entorno()
                DatosIniciales.cargarSiEstaVacia(e.dao)
                coEvery { e.contenidosApi.preguntas(any()) } returns emptyList()
                coEvery { e.resultadosApi.listar() } returns emptyList()
                cuerpo(e)
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    "al abrir la pantalla del docente llega el contenido que creó otro dispositivo" {
        conMain { e ->
            coEvery { e.contenidosApi.listar() } returns listOf(contenidoDto(20, Estado.PENDIENTE))

            DocenteViewModel(e.repo, mockk(relaxed = true)).refrescar()
            advanceUntilIdle()

            e.dao.todosLosContenidos().map { it.id } shouldContainExactly listOf(20)
        }
    }

    "al abrir la pantalla de la UTP se piden los contenidos al servidor" {
        conMain { e ->
            coEvery { e.contenidosApi.listar() } returns listOf(contenidoDto(21, Estado.APROBADO))

            UtpViewModel(e.repo).refrescar()
            advanceUntilIdle()

            coVerify(exactly = 1) { e.contenidosApi.listar() }
            e.dao.buscarContenido(21)?.estado shouldBe Estado.APROBADO
        }
    }

    "al abrir la pantalla del estudiante se actualizan contenidos e intentos" {
        conMain { e ->
            coEvery { e.contenidosApi.listar() } returns listOf(contenidoDto(22, Estado.APROBADO))
            coEvery { e.resultadosApi.listar() } returns listOf(IntentoDto(7, 3, 22, TipoActividad.DESAFIO, 1, 1, 1L))
            val foto = mockk<ServicioFoto> { coEvery { cargar(any()) } returns null }
            val voz = mockk<ServicioVoz> { every { hablando } returns MutableStateFlow(false) }

            EstudianteViewModel(e.repo, mockk(relaxed = true), mockk(relaxed = true), voz, foto).refrescar()
            advanceUntilIdle()

            e.dao.todosLosIntentos().single().id shouldBe 7
        }
    }

    "el informe del docente sincroniza antes de calcular" {
        conMain { e ->
            coEvery { e.contenidosApi.listar() } returns listOf(contenidoDto(23, Estado.APROBADO))
            coEvery { e.resultadosApi.listar() } returns listOf(IntentoDto(8, 3, 23, TipoActividad.PRUEBA, 2, 3, 1L))

            val vm = DocenteViewModel(e.repo, mockk(relaxed = true))
            vm.cargarInforme(23)
            advanceUntilIdle()

            coVerify(exactly = 1) { e.resultadosApi.listar() }
            vm.informe.value.sinDatos shouldBe false
        }
    }
})
