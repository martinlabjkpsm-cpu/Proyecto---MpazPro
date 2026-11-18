package cl.mpaz.mpazpro.grupo_2.data.local

import kotlinx.coroutines.flow.Flow

// Contrato del almacenamiento simple (usuario recordado). La app usa PreferenciasDataStore;
// los tests unitarios usan una versión en memoria.

interface Preferencias {
    val usuarioRecordado: Flow<String>
    suspend fun guardarUsuario(usuario: String)
}
