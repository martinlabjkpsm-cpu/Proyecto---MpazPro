package cl.mpaz.mpazpro.data.repository

import cl.mpaz.mpazpro.data.local.AppDao
import cl.mpaz.mpazpro.data.local.ContenidoEntity
import cl.mpaz.mpazpro.data.local.DatosIniciales
import cl.mpaz.mpazpro.data.local.IntentoEntity
import cl.mpaz.mpazpro.data.local.PreferenciasDataStore
import cl.mpaz.mpazpro.data.local.PreguntaEntity
import cl.mpaz.mpazpro.data.local.RespuestaEntity
import cl.mpaz.mpazpro.data.local.UsuarioEntity
import cl.mpaz.mpazpro.domain.model.Estado
import cl.mpaz.mpazpro.domain.model.TipoActividad
import kotlinx.coroutines.flow.Flow

// El que habla con room y datastore.


class MPazRepository(
    private val dao: AppDao,
    private val preferencias: PreferenciasDataStore
) {

    var usuarioActual: UsuarioEntity? = null
        private set


    suspend fun iniciarSesion(usuario: String, pin: String): UsuarioEntity? {
        DatosIniciales.cargarSiEstaVacia(dao)       // garantiza que existan los usuarios de prueba
        val encontrado = dao.buscarUsuario(usuario, pin)
        usuarioActual = encontrado
        return encontrado
    }

    fun cerrarSesion() { usuarioActual = null }

    val usuarioRecordado: Flow<String> = preferencias.usuarioRecordado
    suspend fun recordarUsuario(usuario: String) = preferencias.guardarUsuario(usuario)


    fun contenidos(): Flow<List<ContenidoEntity>> = dao.observarContenidos()
    suspend fun contenido(id: Int): ContenidoEntity? = dao.buscarContenido(id)
    suspend fun actualizar(contenido: ContenidoEntity) = dao.actualizarContenido(contenido)

    suspend fun eliminar(contenido: ContenidoEntity) {
        dao.eliminarPreguntasDe(contenido.id)   // primero sus preguntas
        dao.eliminarContenido(contenido)
    }


    suspend fun preguntas(contenidoId: Int, tipo: TipoActividad): List<PreguntaEntity> =
        dao.preguntas(contenidoId, tipo.name)

    fun intentosDe(estudianteId: Int): Flow<List<IntentoEntity>> = dao.observarIntentosDe(estudianteId)


    suspend fun guardarIntento(contenidoId: Int, tipo: TipoActividad, preguntas: List<PreguntaEntity>, elegidas: List<Int>) {
        val estudiante = usuarioActual ?: return
        val correctas = preguntas.indices.count { i -> elegidas[i] == preguntas[i].correcta }
        val intentoId = dao.insertarIntento(
            IntentoEntity(estudianteId = estudiante.id, contenidoId = contenidoId, tipo = tipo,
                correctas = correctas, total = preguntas.size)
        ).toInt()
        dao.insertarRespuestas(
            preguntas.mapIndexed { i, p ->
                RespuestaEntity(intentoId = intentoId, preguntaId = p.id,
                    esCorrecta = elegidas[i] == p.correcta, opcionElegida = elegidas[i])   // NUEVO: guarda la alternativa
            }
        )
    }


    suspend fun estudiantes(): List<UsuarioEntity> = dao.estudiantes()
    suspend fun intentosPrueba(contenidoId: Int): List<IntentoEntity> = dao.intentosPrueba(contenidoId)
    suspend fun respuestasDe(intentoIds: List<Int>): List<RespuestaEntity> = dao.respuestasDe(intentoIds)


    // ---------- NUEVO (v2) ----------

    // Cuántas veces el estudiante actual rindió la prueba de un objetivo
    suspend fun intentosPruebaUsados(contenidoId: Int): Int {
        val e = usuarioActual ?: return 0
        return dao.contarIntentosPrueba(e.id, contenidoId)
    }

    // Crear (id = 0) o editar un contenido. Devuelve el id.
    suspend fun guardarContenido(c: ContenidoEntity): Int =
        if (c.id == 0) dao.insertarContenido(c).toInt() else { dao.actualizarContenido(c); c.id }

    suspend fun contenidosDe(asignatura: String): List<ContenidoEntity> = dao.contenidosDe(asignatura)
    suspend fun todasLasPreguntas(contenidoId: Int): List<PreguntaEntity> = dao.todasLasPreguntas(contenidoId)
    suspend fun agregarPregunta(p: PreguntaEntity): Long = dao.insertarPregunta(p)
    suspend fun eliminarPregunta(p: PreguntaEntity) = dao.eliminarPregunta(p)

    // Regla de negocio: cualquier cambio a un contenido exige nueva autorización UTP
    suspend fun volverARevision(contenidoId: Int) {
        val c = dao.buscarContenido(contenidoId) ?: return
        dao.actualizarContenido(c.copy(estado = Estado.PENDIENTE, asignado = false))
    }
}