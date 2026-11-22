package cl.mpaz.mpazpro.grupo_2.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
//CRUD de la aplicacion, estas funciones no comgelan la pantalla gracias a la corrutina.

@Dao
interface AppDao {


    @Insert
    suspend fun insertarUsuarios(usuarios: List<UsuarioEntity>)

    @Query("SELECT * FROM usuarios WHERE usuario = :usuario AND pin = :pin LIMIT 1")
    suspend fun buscarUsuario(usuario: String, pin: String): UsuarioEntity?          // R

    @Query("SELECT * FROM usuarios WHERE rol = 'ESTUDIANTE'")
    suspend fun estudiantes(): List<UsuarioEntity>                                    // R

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contarUsuarios(): Int                                                 // R


    @Insert
    suspend fun insertarContenido(contenido: ContenidoEntity): Long                   // C

    @Query("SELECT * FROM contenidos ORDER BY asignatura")
    fun observarContenidos(): Flow<List<ContenidoEntity>>                             // R (avisa)

    @Query("SELECT * FROM contenidos WHERE id = :id")
    suspend fun buscarContenido(id: Int): ContenidoEntity?                            // R

    @Update
    suspend fun actualizarContenido(contenido: ContenidoEntity)                       // U

    @Delete
    suspend fun eliminarContenido(contenido: ContenidoEntity)                         // D


    @Insert
    suspend fun insertarPreguntas(preguntas: List<PreguntaEntity>)                    // C


    @Query("SELECT * FROM preguntas WHERE contenidoId = :contenidoId AND tipo = :tipo ORDER BY dificultad")
    suspend fun preguntas(contenidoId: Int, tipo: String): List<PreguntaEntity>       // R

    @Query("DELETE FROM preguntas WHERE contenidoId = :contenidoId")
    suspend fun eliminarPreguntasDe(contenidoId: Int)                                 // D

    @Insert
    suspend fun insertarPregunta(pregunta: PreguntaEntity): Long                      // C

    @Delete
    suspend fun eliminarPregunta(pregunta: PreguntaEntity)                            // D

    @Query("SELECT * FROM preguntas WHERE contenidoId = :contenidoId ORDER BY tipo, dificultad")
    suspend fun todasLasPreguntas(contenidoId: Int): List<PreguntaEntity>             // R

    @Query("SELECT * FROM contenidos WHERE asignatura = :asignatura")
    suspend fun contenidosDe(asignatura: String): List<ContenidoEntity>               // R


    @Insert
    suspend fun insertarIntento(intento: IntentoEntity): Long                         // C

    @Insert
    suspend fun insertarRespuestas(respuestas: List<RespuestaEntity>)                 // C

    @Query("SELECT * FROM intentos WHERE estudianteId = :estudianteId")
    fun observarIntentosDe(estudianteId: Int): Flow<List<IntentoEntity>>              // R (avisa)

    @Query("SELECT * FROM intentos WHERE contenidoId = :contenidoId AND tipo = 'PRUEBA'")
    suspend fun intentosPrueba(contenidoId: Int): List<IntentoEntity>                 // R

    @Query("SELECT * FROM respuestas WHERE intentoId IN (:intentoIds)")
    suspend fun respuestasDe(intentoIds: List<Int>): List<RespuestaEntity>            // R

    @Query("SELECT COUNT(*) FROM intentos WHERE estudianteId = :estudianteId AND contenidoId = :contenidoId AND tipo = 'PRUEBA'")
    suspend fun contarIntentosPrueba(estudianteId: Int, contenidoId: Int): Int        // R

    // ---------- Sincronización con los microservicios ----------
    // REPLACE: si ya existe una fila con ese id (el id que asignó el servidor), la reemplaza.
    // borrarXExcepto: quita lo que el servidor ya no tiene, sin vaciar la tabla (la lista no parpadea).

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarContenido(contenido: ContenidoEntity): Long                    // C/U

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarContenidos(contenidos: List<ContenidoEntity>)                  // C/U

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarPregunta(pregunta: PreguntaEntity): Long                       // C/U

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarPreguntas(preguntas: List<PreguntaEntity>)                     // C/U

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarIntentos(intentos: List<IntentoEntity>)                        // C/U

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarRespuestas(respuestas: List<RespuestaEntity>)                  // C/U

    @Query("DELETE FROM contenidos WHERE id NOT IN (:ids)")
    suspend fun borrarContenidosExcepto(ids: List<Int>)

    @Query("DELETE FROM preguntas WHERE id NOT IN (:ids)")
    suspend fun borrarPreguntasExcepto(ids: List<Int>)

    @Query("DELETE FROM intentos WHERE id NOT IN (:ids)")
    suspend fun borrarIntentosExcepto(ids: List<Int>)

    @Query("DELETE FROM respuestas WHERE id NOT IN (:ids)")
    suspend fun borrarRespuestasExcepto(ids: List<Int>)
}
