package cl.mpaz.mpazpro.nativo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// Recurso nativo de cámara: guarda la foto de perfil de cada estudiante
// en el almacenamiento interno de la app (no necesita permisos).

class FotoPerfil(private val context: Context) {

    private fun archivo(usuarioId: Int) = File(context.filesDir, "avatar_$usuarioId.jpg")

    suspend fun guardar(usuarioId: Int, foto: Bitmap) = withContext(Dispatchers.IO) {
        archivo(usuarioId).outputStream().use { foto.compress(Bitmap.CompressFormat.JPEG, 90, it) }
    }

    suspend fun cargar(usuarioId: Int): Bitmap? = withContext(Dispatchers.IO) {
        val f = archivo(usuarioId)
        if (f.exists()) BitmapFactory.decodeFile(f.path) else null
    }
}
