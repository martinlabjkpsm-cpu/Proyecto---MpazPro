package cl.mpaz.mpazpro.grupo_2.data.local


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

//creacion de la base de datos mpazpro.db
// Ruta del emulador /data/data/cl.mpaz.mpazpro.grupo_2/databases/mpazpro.db

@Database(
    entities = [UsuarioEntity::class, ContenidoEntity::class, PreguntaEntity::class,
        IntentoEntity::class, RespuestaEntity::class],
    version = 2,   // v2: campos curriculares, fechas y opcionElegida
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun dao(): AppDao

    companion object {

        // v1 -> v2: agrega las columnas nuevas sin borrar los datos que ya existen
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE contenidos ADD COLUMN unidad TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE contenidos ADD COLUMN eje TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE contenidos ADD COLUMN ejemplo TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE contenidos ADD COLUMN recursoReferencia TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE contenidos ADD COLUMN fechaValidacion INTEGER")
                db.execSQL("ALTER TABLE contenidos ADD COLUMN intentosAlValidar INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE intentos ADD COLUMN fecha INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE respuestas ADD COLUMN opcionElegida INTEGER NOT NULL DEFAULT -1")
            }
        }

        @Volatile private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(context, AppDatabase::class.java, "mpazpro.db")
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { instancia = it }
            }
    }
}