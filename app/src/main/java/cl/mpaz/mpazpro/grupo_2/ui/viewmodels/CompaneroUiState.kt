package cl.mpaz.mpazpro.grupo_2.ui.viewmodels

/** Estados de la tarjeta de compañeros. Al ser `sealed`, el `when` de la UI debe cubrir los tres. */
sealed class CompaneroUiState {
    data object Loading : CompaneroUiState()
    data class Success(val companero: CompaneroUi) : CompaneroUiState()
    data class Error(val message: String) : CompaneroUiState()
}
