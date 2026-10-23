package cl.mpaz.mpazpro.data.repository

import cl.mpaz.mpazpro.data.local.AppDao
import cl.mpaz.mpazpro.data.local.ContenidoEntity
import cl.mpaz.mpazpro.data.local.IntentoEntity
import cl.mpaz.mpazpro.data.local.PreferenciasDataStore
import cl.mpaz.mpazpro.data.local.PreguntaEntity
import cl.mpaz.mpazpro.data.local.RespuestaEntity
import cl.mpaz.mpazpro.data.local.UsuarioEntity
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
            preguntas.mapIndexed { i, p -> RespuestaEntity(intentoId = intentoId, preguntaId = p.id, esCorrecta = elegidas[i] == p.correcta) }
        )
    }


    suspend fun estudiantes(): List<UsuarioEntity> = dao.estudiantes()
    suspend fun intentosPrueba(contenidoId: Int): List<IntentoEntity> = dao.intentosPrueba(contenidoId)
    suspend fun respuestasDe(intentoIds: List<Int>): List<RespuestaEntity> = dao.respuestasDe(intentoIds)
}