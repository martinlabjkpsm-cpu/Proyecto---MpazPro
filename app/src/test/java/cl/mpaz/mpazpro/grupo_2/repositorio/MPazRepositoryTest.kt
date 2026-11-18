package cl.mpaz.mpazpro.grupo_2.repositorio

import cl.mpaz.mpazpro.grupo_2.data.local.AppDao
import cl.mpaz.mpazpro.grupo_2.data.local.ContenidoEntity
import cl.mpaz.mpazpro.grupo_2.data.local.IntentoEntity
import cl.mpaz.mpazpro.grupo_2.data.local.Preferencias
import cl.mpaz.mpazpro.grupo_2.data.local.PreguntaEntity
import cl.mpaz.mpazpro.grupo_2.data.local.RespuestaEntity
import cl.mpaz.mpazpro.grupo_2.data.local.UsuarioEntity
import cl.mpaz.mpazpro.grupo_2.data.repository.MPazRepository
import cl.mpaz.mpazpro.grupo_2.domain.model.Estado
import cl.mpaz.mpazpro.grupo_2.domain.model.Rol
import cl.mpaz.mpazpro.grupo_2.domain.model.TipoActividad
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest

// Pruebas del repositorio con el DAO de Room simulado con MockK (sin base de datos real).
// Herramientas: Kotest + MockK (coEvery / coVerify) + kotlinx-coroutines-test (runTest).

class MPazRepositoryTest : StringSpec({

    val estudiante = UsuarioEntity(id = 7, usuario = "est01", pin = "1111", nombre = "Estudiante 01", rol = Rol.ESTUDIANTE)

    fun pregunta(id: Int, correcta: Int) =
        PreguntaEntity(id, 1, TipoActividad.PRUEBA, "P$id", "a", "b", "c", correcta, 1, "ayuda")

    fun nuevoRepo(dao: AppDao) = MPazRepository(dao, mockk<Preferencias>(relaxed = true))

    "iniciarSesion devuelve el usuario y lo deja como usuario actual" {
        runTest {
            val dao = mockk<AppDao>()
            coEvery { dao.contarUsuarios() } returns 4                 // ya hay datos: no recarga
            coEvery { dao.buscarUsuario("est01", "1111") } returns estudiante
            val repo = nuevoRepo(dao)

            repo.iniciarSesion("est01", "1111") shouldBe estudiante
            repo.usuarioActual shouldBe estudiante
            coVerify(exactly = 0) { dao.insertarUsuarios(any()) }
        }
    }

    "iniciarSesion con datos incorrectos no deja usuario actual" {
        runTest {
            val dao = mockk<AppDao>()
            coEvery { dao.contarUsuarios() } returns 4
            coEvery { dao.buscarUsuario(any(), any()) } returns null
            val repo = nuevoRepo(dao)
            repo.iniciarSesion("est01", "0000").shouldBeNull()
            repo.usuarioActual.shouldBeNull()
        }
    }

    "guardarIntento cuenta las correctas y guarda cada respuesta con la alternativa elegida" {
        runTest {
            val dao = mockk<AppDao>()
            coEvery { dao.contarUsuarios() } returns 4
            coEvery { dao.buscarUsuario(any(), any()) } returns estudiante
            val intento = slot<IntentoEntity>()
            val respuestas = slot<List<RespuestaEntity>>()
            coEvery { dao.insertarIntento(capture(intento)) } returns 99L
            coEvery { dao.insertarRespuestas(capture(respuestas)) } just Runs
            val repo = nuevoRepo(dao)
            repo.iniciarSesion("est01", "1111")

            repo.guardarIntento(1, TipoActividad.PRUEBA, listOf(pregunta(10, 1), pregunta(11, 0), pregunta(12, 2)), listOf(1, 1, 2))

            intento.captured.estudianteId shouldBe 7
            intento.captured.correctas shouldBe 2
            intento.captured.total shouldBe 3
            respuestas.captured.map { it.intentoId }.distinct() shouldContainExactly listOf(99)
            respuestas.captured.map { it.esCorrecta } shouldContainExactly listOf(true, false, true)
            respuestas.captured.map { it.opcionElegida } shouldContainExactly listOf(1, 1, 2)
        }
    }

    "sin sesión iniciada no se guarda ningún intento" {
        runTest {
            val dao = mockk<AppDao>()
            nuevoRepo(dao).guardarIntento(1, TipoActividad.PRUEBA, listOf(pregunta(10, 1)), listOf(1))
            coVerify(exactly = 0) { dao.insertarIntento(any()) }
        }
    }

    "eliminar un contenido borra primero sus preguntas" {
        runTest {
            val dao = mockk<AppDao>()
            val c = ContenidoEntity(id = 3, asignatura = "Historia", codigoOa = "HI03 OA 06", objetivo = "x", explicacion = "y")
            coEvery { dao.eliminarPreguntasDe(3) } just Runs
            coEvery { dao.eliminarContenido(c) } just Runs

            nuevoRepo(dao).eliminar(c)

            coVerifyOrder {
                dao.eliminarPreguntasDe(3)
                dao.eliminarContenido(c)
            }
        }
    }

    "guardarContenido inserta si es nuevo y actualiza si ya existe" {
        runTest {
            val dao = mockk<AppDao>()
            val nuevo = ContenidoEntity(asignatura = "Lenguaje", codigoOa = "LE03 OA 04", objetivo = "x", explicacion = "y")
            coEvery { dao.insertarContenido(nuevo) } returns 5L
            coEvery { dao.actualizarContenido(any()) } just Runs
            val repo = nuevoRepo(dao)

            repo.guardarContenido(nuevo) shouldBe 5
            repo.guardarContenido(nuevo.copy(id = 5)) shouldBe 5
            coVerify { dao.actualizarContenido(nuevo.copy(id = 5)) }
        }
    }

    "volverARevision deja el contenido PENDIENTE y sin asignar" {
        runTest {
            val dao = mockk<AppDao>()
            val aprobado = ContenidoEntity(id = 2, asignatura = "Matemática", codigoOa = "MA03 OA 01", objetivo = "x",
                explicacion = "y", estado = Estado.APROBADO, asignado = true)
            val guardado = slot<ContenidoEntity>()
            coEvery { dao.buscarContenido(2) } returns aprobado
            coEvery { dao.actualizarContenido(capture(guardado)) } just Runs

            nuevoRepo(dao).volverARevision(2)

            guardado.captured.estado shouldBe Estado.PENDIENTE
            guardado.captured.asignado shouldBe false
        }
    }

    "recordarUsuario delega en las preferencias" {
        runTest {
            val preferencias = mockk<Preferencias>(relaxed = true)
            MPazRepository(mockk(), preferencias).recordarUsuario("docente01")
            coVerify { preferencias.guardarUsuario("docente01") }
        }
    }

    "intentosPruebaUsados cuenta los intentos del estudiante actual" {
        runTest {
            val dao = mockk<AppDao>()
            coEvery { dao.contarUsuarios() } returns 4
            coEvery { dao.buscarUsuario(any(), any()) } returns estudiante
            coEvery { dao.contarIntentosPrueba(7, 1) } returns 2
            val repo = nuevoRepo(dao)
            repo.intentosPruebaUsados(1) shouldBe 0            // sin sesión
            repo.iniciarSesion("est01", "1111")
            repo.intentosPruebaUsados(1) shouldBe 2
        }
    }
})
