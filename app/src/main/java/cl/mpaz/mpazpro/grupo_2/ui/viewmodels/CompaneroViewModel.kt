package cl.mpaz.mpazpro.grupo_2.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.mpaz.mpazpro.grupo_2.data.repository.PokemonException
import cl.mpaz.mpazpro.grupo_2.data.repository.PokemonRepository
import cl.mpaz.mpazpro.grupo_2.domain.model.Companeros
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estado de "Mis compañeros Pokémon" (pestaña Mis logros del estudiante).
 * Cada medalla desbloquea un compañero que se pide a PokéAPI a través del repositorio.
 */
class CompaneroViewModel(
    private val repository: PokemonRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CompaneroUiState>(CompaneroUiState.Loading)
    val uiState: StateFlow<CompaneroUiState> = _uiState.asStateFlow()

    private val _desbloqueados = MutableStateFlow<List<Int>>(emptyList())
    val desbloqueados: StateFlow<List<Int>> = _desbloqueados.asStateFlow()

    private val _seleccionado = MutableStateFlow<Int?>(null)
    val seleccionado: StateFlow<Int?> = _seleccionado.asStateFlow()

    // Petición en curso: si el estudiante elige otro compañero, se cancela la anterior.
    private var loadJob: Job? = null

    /** La pantalla lo llama con las medallas; si ganó una nueva, muestra el nuevo compañero. */
    fun actualizar(medallas: Int) {
        val nuevos = Companeros.desbloqueados(medallas)
        if (nuevos == _desbloqueados.value) return
        _desbloqueados.value = nuevos
        elegir(nuevos.last())
    }

    fun elegir(id: Int) {
        _seleccionado.value = id
        loadPokemon(id.toString())
    }

    fun reintentar() {
        _seleccionado.value?.let { loadPokemon(it.toString()) }
    }

    private fun loadPokemon(idOrName: String) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.value = CompaneroUiState.Loading
            _uiState.value = try {
                CompaneroUiState.Success(repository.getPokemon(idOrName).toUi())
            } catch (e: PokemonException) {
                CompaneroUiState.Error(e.toMessage())
            }
        }
    }

    private fun PokemonException.toMessage(): String = when (this) {
        is PokemonException.Network -> MENSAJE_SIN_CONEXION
        is PokemonException.NotFound -> "No encontramos a este compañero."
        is PokemonException.Server -> "PokéAPI no respondió bien (error $code). Inténtalo más tarde."
        is PokemonException.Unknown -> "Ocurrió un error inesperado. Inténtalo otra vez."
    }

    companion object {
        const val MENSAJE_SIN_CONEXION = "No pudimos conectar con PokéAPI. Revisa tu internet e inténtalo otra vez."
    }
}
