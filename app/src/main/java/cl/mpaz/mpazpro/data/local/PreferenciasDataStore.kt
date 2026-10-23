package cl.mpaz.mpazpro.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

//guarda el usuario recordado del login, no se ocupa room, ya que data store es para datos simples

private val Context.dataStore by preferencesDataStore(name = "preferencias")

class PreferenciasDataStore(private val context: Context) {

    private val claveUsuario = stringPreferencesKey("usuario_recordado")


    val usuarioRecordado: Flow<String> = context.dataStore.data.map { it[claveUsuario] ?: "" }

    suspend fun guardarUsuario(usuario: String) {
        context.dataStore.edit { it[claveUsuario] = usuario }
    }
}