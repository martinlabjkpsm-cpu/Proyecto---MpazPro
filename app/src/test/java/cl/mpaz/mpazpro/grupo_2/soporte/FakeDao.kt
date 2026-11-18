package cl.mpaz.mpazpro.grupo_2.soporte

import cl.mpaz.mpazpro.grupo_2.data.local.AppDao
import cl.mpaz.mpazpro.grupo_2.data.local.ContenidoEntity
import cl.mpaz.mpazpro.grupo_2.data.local.IntentoEntity
import cl.mpaz.mpazpro.grupo_2.data.local.PreguntaEntity
import cl.mpaz.mpazpro.grupo_2.data.local.RespuestaEntity
import cl.mpaz.mpazpro.grupo_2.data.local.UsuarioEntity
import cl.mpaz.mpazpro.grupo_2.domain.model.Rol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

// Base de datos falsa en memoria: imita lo que hace Room con las mismas consultas del AppDao,
// pero sin SQLite. Así los casos de uso se prueban rápido y sin emulador.

class FakeDao : AppDao {

    private val usuarios = mutableListOf<UsuarioEntity>()
    private val contenidos = MutableStateFlow<List<ContenidoEntity>>(emptyList())
    private val preguntas = mutableListOf<PreguntaEntity>()
    private val intentos = MutableStateFlow<List<IntentoEntity>>(emptyList())
    private val respuestas = mutableListOf<RespuestaEntity>()

    private var sigUsuario = 1
    private var sigContenido = 1
    private var sigPregunta = 1
    private var sigIntento = 1
    private var sigRespuesta = 1

    // ---- ayudas para los tests ----
    fun todosLosContenidos(): List<ContenidoEntity> = contenidos.value
    fun todasLasPreguntasGuardadas(): List<PreguntaEntity> = preguntas.toList()
    fun todosLosIntentos(): List<IntentoEntity> = intentos.value
    fun todasLasRespuestas(): List<RespuestaEntity> = respuestas.toList()

    // ---- usuarios ----
    override suspend fun insertarUsuarios(usuarios: List<UsuarioEntity>) {
        usuarios.forEach { this.usuarios += it.copy(id = sigUsuario++) }
    }

    override suspend fun buscarUsuario(usuario: String, pin: String): UsuarioEntity? =
        usuarios.firstOrNull { it.usuario == usuario && it.pin == pin }

    override suspend fun estudiantes(): List<UsuarioEntity> = usuarios.filter { it.rol == Rol.ESTUDIANTE }

    override suspend fun contarUsuarios(): Int = usuarios.size

    // ---- contenidos ----
    override suspend fun insertarContenido(contenido: ContenidoEntity): Long {
        val nuevo = contenido.copy(id = sigContenido++)
        contenidos.value = contenidos.value + nuevo
        return nuevo.id.toLong()
    }

    override fun observarContenidos(): Flow<List<ContenidoEntity>> =
        contenidos.map { lista -> lista.sortedBy { it.asignatura } }

    override suspend fun buscarContenido(id: Int): ContenidoEntity? = contenidos.value.firstOrNull { it.id == id }

    override suspend fun actualizarContenido(contenido: ContenidoEntity) {
        contenidos.value = contenidos.value.map { if (it.id == contenido.id) contenido else it }
    }

    override suspend fun eliminarContenido(contenido: ContenidoEntity) {
        contenidos.value = contenidos.value.filter { it.id != contenido.id }
    }

    override suspend fun contenidosDe(asignatura: String): List<ContenidoEntity> =
        contenidos.value.filter { it.asignatura == asignatura }

    // ---- preguntas ----
    override suspend fun insertarPreguntas(preguntas: List<PreguntaEntity>) {
        preguntas.forEach { insertarPregunta(it) }
    }

    override suspend fun insertarPregunta(pregunta: PreguntaEntity): Long {
        val nueva = pregunta.copy(id = sigPregunta++)
        preguntas += nueva
        return nueva.id.toLong()
    }

    override suspend fun preguntas(contenidoId: Int, tipo: String): List<PreguntaEntity> =
        preguntas.filter { it.contenidoId == contenidoId && it.tipo.name == tipo }.sortedBy { it.dificultad }

    override suspend fun eliminarPreguntasDe(contenidoId: Int) {
        preguntas.removeAll { it.contenidoId == contenidoId }
    }

    override suspend fun eliminarPregunta(pregunta: PreguntaEntity) {
        preguntas.removeAll { it.id == pregunta.id }
    }

    override suspend fun todasLasPreguntas(contenidoId: Int): List<PreguntaEntity> =
        preguntas.filter { it.contenidoId == contenidoId }
            .sortedWith(compareBy<PreguntaEntity>({ it.tipo.name }, { it.dificultad }))

    // ---- intentos y respuestas ----
    override suspend fun insertarIntento(intento: IntentoEntity): Long {
        val nuevo = intento.copy(id = sigIntento++)
        intentos.value = intentos.value + nuevo
        return nuevo.id.toLong()
    }

    override suspend fun insertarRespuestas(respuestas: List<RespuestaEntity>) {
        respuestas.forEach { this.respuestas += it.copy(id = sigRespuesta++) }
    }

    override fun observarIntentosDe(estudianteId: Int): Flow<List<IntentoEntity>> =
        intentos.map { lista -> lista.filter { it.estudianteId == estudianteId } }

    override suspend fun intentosPrueba(contenidoId: Int): List<IntentoEntity> =
        intentos.value.filter { it.contenidoId == contenidoId && it.tipo.name == "PRUEBA" }

    override suspend fun respuestasDe(intentoIds: List<Int>): List<RespuestaEntity> =
        respuestas.filter { it.intentoId in intentoIds }

    override suspend fun contarIntentosPrueba(estudianteId: Int, contenidoId: Int): Int =
        intentos.value.count { it.estudianteId == estudianteId && it.contenidoId == contenidoId && it.tipo.name == "PRUEBA" }
}
