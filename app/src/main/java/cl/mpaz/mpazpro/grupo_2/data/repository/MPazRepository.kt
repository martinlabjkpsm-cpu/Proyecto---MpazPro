package cl.mpaz.mpazpro.grupo_2.data.repository

import cl.mpaz.mpazpro.grupo_2.data.local.AppDao
import cl.mpaz.mpazpro.grupo_2.data.local.ContenidoEntity
import cl.mpaz.mpazpro.grupo_2.data.local.DatosIniciales
import cl.mpaz.mpazpro.grupo_2.data.local.IntentoEntity
import cl.mpaz.mpazpro.grupo_2.data.local.Preferencias
import cl.mpaz.mpazpro.grupo_2.data.local.PreguntaEntity
import cl.mpaz.mpazpro.grupo_2.data.local.RespuestaEntity
import cl.mpaz.mpazpro.grupo_2.data.local.UsuarioEntity
import cl.mpaz.mpazpro.grupo_2.data.remote.microservicios.ContenidosApi
import cl.mpaz.mpazpro.grupo_2.data.remote.microservicios.IntentoRequest
import cl.mpaz.mpazpro.grupo_2.data.remote.microservicios.RespuestaDto
import cl.mpaz.mpazpro.grupo_2.data.remote.microservicios.ResultadosApi
import cl.mpaz.mpazpro.grupo_2.data.remote.microservicios.aDto
import cl.mpaz.mpazpro.grupo_2.data.remote.microservicios.aEntidad
import cl.mpaz.mpazpro.grupo_2.domain.model.Estado
import cl.mpaz.mpazpro.grupo_2.domain.model.TipoActividad
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// La única puerta a los datos: habla con Room, DataStore y los microservicios Spring Boot.
//
// Cómo se combinan Room y los microservicios:
// - Al iniciar sesión se descargan contenidos, preguntas e intentos del servidor y se guardan en Room.
// - Cada cambio (crear, editar, eliminar, guardar intento) se envía primero al microservicio y
//   después se guarda en Room con el id que asignó el servidor.
// - Las pantallas siempre leen desde Room (Flow), así que se actualizan solas.
// - Si los microservicios no responden, la app sigue funcionando solo con Room (modo sin conexión).
//
// Si contenidosApi y resultadosApi son null (pruebas antiguas), el repositorio trabaja solo con Room.

class MPazRepository(
    private val dao: AppDao,
    private val preferencias: Preferencias,
    private val contenidosApi: ContenidosApi? = null,
    private val resultadosApi: ResultadosApi? = null
) {

    var usuarioActual: UsuarioEntity? = null
        private set

    // true si la última llamada a los microservicios funcionó; la pantalla puede mostrarlo.
    private val _enLinea = MutableStateFlow(false)
    val enLinea: StateFlow<Boolean> = _enLinea.asStateFlow()


    suspend fun iniciarSesion(usuario: String, pin: String): UsuarioEntity? {
        DatosIniciales.cargarSiEstaVacia(dao)       // garantiza que existan los usuarios de prueba
        val encontrado = dao.buscarUsuario(usuario, pin)
        usuarioActual = encontrado
        if (encontrado != null) sincronizar()       // trae lo último del servidor
        return encontrado
    }

    fun cerrarSesion() { usuarioActual = null }

    val usuarioRecordado: Flow<String> = preferencias.usuarioRecordado
    suspend fun recordarUsuario(usuario: String) = preferencias.guardarUsuario(usuario)


    // ---------- Sincronización con los microservicios ----------

    /**
     * Reemplaza los contenidos, preguntas e intentos de Room por los del servidor.
     * Primero descarga todo y recién después escribe en Room: si algo falla, Room queda como estaba.
     * Se llama al iniciar sesión y cada vez que se abre una pantalla principal (datos "en tiempo real").
     * Devuelve true si se sincronizó.
     */
    suspend fun sincronizar(): Boolean {
        val api = contenidosApi ?: return false
        val datos = intentar {
            val contenidos = api.listar()
            val preguntas = contenidos.mapNotNull { it.id }.flatMap { id ->
                api.preguntas(id).map { it.aEntidad(id) }
            }
            val intentos = resultadosApi?.listar()
            Triple(contenidos.map { it.aEntidad() }, preguntas, intentos)
        } ?: return false

        val (contenidos, preguntas, intentos) = datos
        // Guardar primero y después quitar lo que el servidor ya no tiene: la lista nunca queda vacía
        dao.guardarContenidos(contenidos)
        dao.guardarPreguntas(preguntas)
        dao.borrarPreguntasExcepto(preguntas.map { it.id })
        dao.borrarContenidosExcepto(contenidos.map { it.id })
        if (intentos != null) {
            val respuestas = intentos.flatMap { i -> i.respuestas.map { it.aEntidad(i.id) } }
            dao.guardarIntentos(intentos.map { it.aEntidad() })
            dao.guardarRespuestas(respuestas)
            dao.borrarRespuestasExcepto(respuestas.map { it.id })
            dao.borrarIntentosExcepto(intentos.map { it.id })
        }
        return true
    }

    /** Ejecuta una llamada al servidor. Si falla (sin red, 4xx, 5xx) devuelve null y la app sigue con Room. */
    private suspend fun <T> intentar(llamada: suspend () -> T): T? =
        try {
            llamada().also { _enLinea.value = true }
        } catch (e: CancellationException) {
            throw e                                  // no se traga la cancelación de la corrutina
        } catch (e: Exception) {
            _enLinea.value = false
            null
        }


    // ---------- Contenidos ----------

    fun contenidos(): Flow<List<ContenidoEntity>> = dao.observarContenidos()
    suspend fun contenido(id: Int): ContenidoEntity? = dao.buscarContenido(id)

    /** Editar (UTP aprueba o rechaza, docente asigna o valida el informe): PUT y luego Room. */
    suspend fun actualizar(contenido: ContenidoEntity) {
        contenidosApi?.let { api -> intentar { api.actualizar(contenido.id, contenido.aDto()) } }
        dao.actualizarContenido(contenido)
    }

    suspend fun eliminar(contenido: ContenidoEntity) {
        contenidosApi?.let { api -> intentar { api.eliminar(contenido.id) } }   // el servidor borra también sus preguntas
        dao.eliminarPreguntasDe(contenido.id)   // primero sus preguntas
        dao.eliminarContenido(contenido)
    }


    suspend fun preguntas(contenidoId: Int, tipo: TipoActividad): List<PreguntaEntity> =
        dao.preguntas(contenidoId, tipo.name)

    fun intentosDe(estudianteId: Int): Flow<List<IntentoEntity>> = dao.observarIntentosDe(estudianteId)


    suspend fun guardarIntento(contenidoId: Int, tipo: TipoActividad, preguntas: List<PreguntaEntity>, elegidas: List<Int>) {
        val estudiante = usuarioActual ?: return
        val correctas = preguntas.indices.count { i -> elegidas[i] == preguntas[i].correcta }
        val fecha = System.currentTimeMillis()

        // 1) Microservicio: si responde, se guarda en Room con los ids del servidor
        val remoto = resultadosApi?.let { api ->
            intentar {
                api.registrar(
                    IntentoRequest(
                        estudianteId = estudiante.id, contenidoId = contenidoId, tipo = tipo, fecha = fecha,
                        respuestas = preguntas.mapIndexed { i, p ->
                            RespuestaDto(preguntaId = p.id, esCorrecta = elegidas[i] == p.correcta, opcionElegida = elegidas[i])
                        }
                    )
                )
            }
        }
        if (remoto != null) {
            dao.guardarIntentos(listOf(remoto.aEntidad()))
            dao.guardarRespuestas(remoto.respuestas.map { it.aEntidad(remoto.id) })
            return
        }

        // 2) Sin servidor: solo Room, como antes
        val intentoId = dao.insertarIntento(
            IntentoEntity(estudianteId = estudiante.id, contenidoId = contenidoId, tipo = tipo,
                correctas = correctas, total = preguntas.size, fecha = fecha)
        ).toInt()
        dao.insertarRespuestas(
            preguntas.mapIndexed { i, p ->
                RespuestaEntity(intentoId = intentoId, preguntaId = p.id,
                    esCorrecta = elegidas[i] == p.correcta, opcionElegida = elegidas[i])   // guarda la alternativa
            }
        )
    }


    suspend fun estudiantes(): List<UsuarioEntity> = dao.estudiantes()
    suspend fun intentosPrueba(contenidoId: Int): List<IntentoEntity> = dao.intentosPrueba(contenidoId)
    suspend fun respuestasDe(intentoIds: List<Int>): List<RespuestaEntity> = dao.respuestasDe(intentoIds)


    // Cuántas veces el estudiante actual rindió la prueba de un objetivo
    suspend fun intentosPruebaUsados(contenidoId: Int): Int {
        val e = usuarioActual ?: return 0
        return dao.contarIntentosPrueba(e.id, contenidoId)
    }

    /** Crear (id = 0) o editar un contenido: POST o PUT al microservicio y luego Room. Devuelve el id. */
    suspend fun guardarContenido(c: ContenidoEntity): Int {
        val remoto = contenidosApi?.let { api ->
            intentar { if (c.id == 0) api.crear(c.aDto()) else api.actualizar(c.id, c.aDto()) }
        }
        if (remoto?.id != null) {
            val guardado = remoto.aEntidad()
            dao.guardarContenido(guardado)
            return guardado.id
        }
        // Sin servidor: solo Room
        return if (c.id == 0) dao.insertarContenido(c).toInt() else { dao.actualizarContenido(c); c.id }
    }

    suspend fun contenidosDe(asignatura: String): List<ContenidoEntity> = dao.contenidosDe(asignatura)
    suspend fun todasLasPreguntas(contenidoId: Int): List<PreguntaEntity> = dao.todasLasPreguntas(contenidoId)

    suspend fun agregarPregunta(p: PreguntaEntity): Long {
        val remota = contenidosApi?.let { api -> intentar { api.agregarPregunta(p.contenidoId, p.aDto()) } }
        if (remota?.id != null) return dao.guardarPregunta(remota.aEntidad(p.contenidoId))
        return dao.insertarPregunta(p)
    }

    suspend fun eliminarPregunta(p: PreguntaEntity) {
        contenidosApi?.let { api -> intentar { api.eliminarPregunta(p.id) } }
        dao.eliminarPregunta(p)
    }

    // Regla de negocio: cualquier cambio a un contenido exige nueva autorización UTP
    suspend fun volverARevision(contenidoId: Int) {
        val c = dao.buscarContenido(contenidoId) ?: return
        actualizar(c.copy(estado = Estado.PENDIENTE, asignado = false))
    }
}
