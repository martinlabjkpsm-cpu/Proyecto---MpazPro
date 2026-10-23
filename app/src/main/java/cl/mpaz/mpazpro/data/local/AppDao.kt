package cl.mpaz.mpazpro.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
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
    suspend fun insertarIntento(intento: IntentoEntity): Long                         // C

    @Insert
    suspend fun insertarRespuestas(respuestas: List<RespuestaEntity>)                 // C

    @Query("SELECT * FROM intentos WHERE estudianteId = :estudianteId")
    fun observarIntentosDe(estudianteId: Int): Flow<List<IntentoEntity>>              // R (avisa)

    @Query("SELECT * FROM intentos WHERE contenidoId = :contenidoId AND tipo = 'PRUEBA'")
    suspend fun intentosPrueba(contenidoId: Int): List<IntentoEntity>                 // R

    @Query("SELECT * FROM respuestas WHERE intentoId IN (:intentoIds)")
    suspend fun respuestasDe(intentoIds: List<Int>): List<RespuestaEntity>            // R
}