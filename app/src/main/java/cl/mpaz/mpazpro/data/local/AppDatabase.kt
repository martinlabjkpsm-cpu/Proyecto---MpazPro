package cl.mpaz.mpazpro.data.local


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

//creacion de la base de datos mpazpro.db
// Ruta del emulador /data/data/cl.mpaz.mpazpro/databases/mpazpro.db

@Database(
    entities = [UsuarioEntity::class, ContenidoEntity::class, PreguntaEntity::class,
        IntentoEntity::class, RespuestaEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dao(): AppDao

    companion object {

        @Volatile private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(context, AppDatabase::class.java, "mpazpro.db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instancia = it }
            }
    }
}