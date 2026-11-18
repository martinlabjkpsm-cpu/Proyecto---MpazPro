package cl.mpaz.mpazpro.grupo_2.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

//guarda el usuario recordado del login, no se ocupa room, ya que data store es para datos simples

private val Context.dataStore by preferencesDataStore(name = "preferencias")

class PreferenciasDataStore(private val context: Context) : Preferencias {

    private val claveUsuario = stringPreferencesKey("usuario_recordado")


    override val usuarioRecordado: Flow<String> = context.dataStore.data.map { it[claveUsuario] ?: "" }

    override suspend fun guardarUsuario(usuario: String) {
        context.dataStore.edit { it[claveUsuario] = usuario }
    }
}