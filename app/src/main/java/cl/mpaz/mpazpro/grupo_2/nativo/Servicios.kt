package cl.mpaz.mpazpro.grupo_2.nativo

import android.graphics.Bitmap
import android.net.Uri
import kotlinx.coroutines.flow.StateFlow

// Contratos de los recursos nativos que usan los ViewModel.
// Las clases reales (Vibracion, Notificaciones, LectorVoz, FotoPerfil, ExportadorCsv) los implementan
// con Android; los tests unitarios usan versiones falsas, así ./gradlew test corre sin emulador.

interface ServicioVibracion {
    fun correcto()
    fun incorrecto()
}

interface ServicioAvisos {
    fun mostrar(titulo: String, texto: String)
}

interface ServicioVoz {
    val hablando: StateFlow<Boolean>
    fun leer(texto: String)
    fun detener()
}

interface ServicioFoto {
    suspend fun guardar(usuarioId: Int, foto: Bitmap)
    suspend fun cargar(usuarioId: Int): Bitmap?
}

interface ServicioExportacion {
    suspend fun escribir(uri: Uri, contenido: String): Boolean
}
