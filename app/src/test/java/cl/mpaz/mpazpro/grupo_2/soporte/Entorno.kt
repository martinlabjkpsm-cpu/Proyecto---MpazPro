package cl.mpaz.mpazpro.grupo_2.soporte

import cl.mpaz.mpazpro.grupo_2.data.local.ContenidoEntity
import cl.mpaz.mpazpro.grupo_2.data.local.DatosIniciales
import cl.mpaz.mpazpro.grupo_2.data.local.Preferencias
import cl.mpaz.mpazpro.grupo_2.data.local.PreguntaEntity
import cl.mpaz.mpazpro.grupo_2.data.local.UsuarioEntity
import cl.mpaz.mpazpro.grupo_2.data.repository.MPazRepository
import cl.mpaz.mpazpro.grupo_2.domain.model.TipoActividad
import cl.mpaz.mpazpro.grupo_2.nativo.ServicioAvisos
import cl.mpaz.mpazpro.grupo_2.nativo.ServicioExportacion
import cl.mpaz.mpazpro.grupo_2.nativo.ServicioFoto
import cl.mpaz.mpazpro.grupo_2.nativo.ServicioVibracion
import cl.mpaz.mpazpro.grupo_2.nativo.ServicioVoz
import cl.mpaz.mpazpro.grupo_2.ui.viewmodels.ContenidoFormViewModel
import cl.mpaz.mpazpro.grupo_2.ui.viewmodels.DocenteViewModel
import cl.mpaz.mpazpro.grupo_2.ui.viewmodels.EstudianteViewModel
import cl.mpaz.mpazpro.grupo_2.ui.viewmodels.LoginViewModel
import cl.mpaz.mpazpro.grupo_2.ui.viewmodels.UtpViewModel
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

// Entorno de cada prueba de caso de uso:
// - ViewModel y repositorio REALES de la app.
// - Base de datos: FakeDao (en memoria, con los DatosIniciales reales).
// - Recursos nativos y preferencias: mocks de MockK (no se necesita teléfono).
// - Corrutinas: kotlinx-coroutines-test. Dispatchers.Main usa el reloj virtual del test,
//   por eso después de cada acción se llama a avanzar() (advanceUntilIdle) y nunca a Thread.sleep.

@OptIn(ExperimentalCoroutinesApi::class)
class Entorno(val scope: TestScope) {

    val dao = FakeDao()

    // Preferencias (usuario recordado) simuladas con MockK
    private val recordado = MutableStateFlow("")
    val preferencias: Preferencias = mockk {
        every { usuarioRecordado } returns recordado
        coEvery { guardarUsuario(any()) } answers { recordado.value = firstArg() }
    }

    // Recursos nativos simulados con MockK
    val vibracion: ServicioVibracion = mockk(relaxed = true)
    val avisos: ServicioAvisos = mockk(relaxed = true)
    private val hablando = MutableStateFlow(false)
    val voz: ServicioVoz = mockk {
        every { hablando } returns this@Entorno.hablando
        every { leer(any()) } answers { this@Entorno.hablando.value = true }
        every { detener() } answers { this@Entorno.hablando.value = false }
    }
    val foto: ServicioFoto = mockk {
        coEvery { cargar(any()) } returns null
        coEvery { guardar(any(), any()) } just Runs
    }
    val exportador: ServicioExportacion = mockk {
        coEvery { escribir(any(), any()) } returns true
    }

    val repo = MPazRepository(dao, preferencias)

    // ---------- ayudas ----------

    fun avanzar() = scope.advanceUntilIdle()

    suspend fun entrarComo(usuario: String, pin: String): UsuarioEntity =
        checkNotNull(repo.iniciarSesion(usuario, pin)) { "No se pudo iniciar sesión con $usuario" }

    fun contenidoDe(asignatura: String): ContenidoEntity =
        dao.todosLosContenidos().first { it.asignatura == asignatura }

    suspend fun preguntasDe(contenidoId: Int, tipo: TipoActividad): List<PreguntaEntity> =
        repo.preguntas(contenidoId, tipo)

    // Mantiene "escuchando" un StateFlow (WhileSubscribed), como lo haría la pantalla
    fun suscribir(flow: Flow<*>) {
        scope.backgroundScope.launch(UnconfinedTestDispatcher(scope.testScheduler)) { flow.collect { } }
        avanzar()
    }

    suspend fun rendirPrueba(contenidoId: Int, elegidas: List<Int>) =
        repo.guardarIntento(contenidoId, TipoActividad.PRUEBA, repo.preguntas(contenidoId, TipoActividad.PRUEBA), elegidas)

    suspend fun hacerDesafio(contenidoId: Int, elegidas: List<Int>) =
        repo.guardarIntento(contenidoId, TipoActividad.DESAFIO, repo.preguntas(contenidoId, TipoActividad.DESAFIO), elegidas)

    // ViewModel reales (el init de cada uno corre al llamar avanzar)
    fun loginVm() = LoginViewModel(repo).also { avanzar() }
    fun utpVm() = UtpViewModel(repo).also { avanzar() }
    fun docenteVm() = DocenteViewModel(repo, exportador).also { avanzar() }
    fun formularioVm() = ContenidoFormViewModel(repo)
    fun estudianteVm() = EstudianteViewModel(repo, vibracion, avisos, voz, foto).also { avanzar() }
}

// Ejecuta un caso de uso dentro de runTest con un entorno nuevo (tests aislados y deterministas).
// Dispatchers.Main se reemplaza ANTES de runTest para que ambos compartan el mismo reloj virtual,
// y se restaura después, cuando ya terminaron todas las corrutinas del test.
@OptIn(ExperimentalCoroutinesApi::class)
fun casoDeUso(cuerpo: suspend Entorno.() -> Unit) {
    Dispatchers.setMain(StandardTestDispatcher())
    try {
        runTest {
            val entorno = Entorno(this)
            DatosIniciales.cargarSiEstaVacia(entorno.dao)
            entorno.cuerpo()
        }
    } finally {
        Dispatchers.resetMain()
    }
}
