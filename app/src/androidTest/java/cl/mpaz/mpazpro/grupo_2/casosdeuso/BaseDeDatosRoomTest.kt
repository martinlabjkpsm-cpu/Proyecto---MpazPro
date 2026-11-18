package cl.mpaz.mpazpro.grupo_2.casosdeuso

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.mpaz.mpazpro.grupo_2.data.local.AppDao
import cl.mpaz.mpazpro.grupo_2.data.local.AppDatabase
import cl.mpaz.mpazpro.grupo_2.data.local.DatosIniciales
import cl.mpaz.mpazpro.grupo_2.data.local.IntentoEntity
import cl.mpaz.mpazpro.grupo_2.data.local.RespuestaEntity
import cl.mpaz.mpazpro.grupo_2.domain.model.Estado
import cl.mpaz.mpazpro.grupo_2.domain.model.Rol
import cl.mpaz.mpazpro.grupo_2.domain.model.TipoActividad
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Herramientas: JUnit (AndroidJUnit4) + kotlinx-coroutines-test (runTest).
// Prueba la base de datos REAL (Room / SQLite en memoria) con las consultas del AppDao.
// Prueba instrumentada: se ejecuta con ./gradlew connectedAndroidTest (emulador o teléfono).
// Respalda el CU16 (el progreso queda guardado) y el CRUD que usan los demás casos de uso.

@RunWith(AndroidJUnit4::class)
class BaseDeDatosRoomTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: AppDao

    @Before
    fun crear() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).allowMainThreadQueries().build()
        dao = db.dao()
        runTest { DatosIniciales.cargarSiEstaVacia(dao) }
    }

    @After
    fun cerrar() = db.close()

    @Test
    fun loginBuscaPorUsuarioYPin() = runTest {
        assertEquals(Rol.DOCENTE, dao.buscarUsuario("docente01", "1234")?.rol)
        assertNull(dao.buscarUsuario("docente01", "0000"))
        assertEquals(2, dao.estudiantes().size)
    }

    @Test
    fun contenidosOrdenadosPorAsignatura() = runTest {
        val lista = dao.observarContenidos().first()
        assertEquals(4, lista.size)
        assertEquals(lista.map { it.asignatura }.sorted(), lista.map { it.asignatura })
    }

    @Test
    fun preguntasOrdenadasPorDificultad() = runTest {
        val mate = dao.contenidosDe("Matemática").single()
        val desafios = dao.preguntas(mate.id, TipoActividad.DESAFIO.name)
        assertEquals(listOf(1, 2, 3), desafios.map { it.dificultad })
        assertEquals(6, dao.todasLasPreguntas(mate.id).size)
    }

    @Test
    fun actualizarYEliminarContenido() = runTest {
        val mate = dao.contenidosDe("Matemática").single()
        dao.actualizarContenido(mate.copy(estado = Estado.RECHAZADO, observacionUtp = "Revisar ejemplo"))
        assertEquals(Estado.RECHAZADO, dao.buscarContenido(mate.id)?.estado)

        dao.eliminarPreguntasDe(mate.id)
        dao.eliminarContenido(mate)
        assertNull(dao.buscarContenido(mate.id))
        assertTrue(dao.todasLasPreguntas(mate.id).isEmpty())
    }

    @Test
    fun intentosYRespuestasQuedanGuardados() = runTest {
        val mate = dao.contenidosDe("Matemática").single()
        val est = dao.buscarUsuario("est01", "1111")
        assertNotNull(est)
        val idIntento = dao.insertarIntento(
            IntentoEntity(estudianteId = est!!.id, contenidoId = mate.id, tipo = TipoActividad.PRUEBA, correctas = 2, total = 3)
        ).toInt()
        dao.insertarRespuestas(listOf(RespuestaEntity(intentoId = idIntento, preguntaId = 1, esCorrecta = true, opcionElegida = 1)))

        assertEquals(1, dao.contarIntentosPrueba(est.id, mate.id))
        assertEquals(1, dao.intentosPrueba(mate.id).size)
        assertEquals(1, dao.observarIntentosDe(est.id).first().size)
        assertEquals(1, dao.respuestasDe(listOf(idIntento)).single().opcionElegida)
    }
}
