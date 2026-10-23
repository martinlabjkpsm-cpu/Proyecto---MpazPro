package cl.mpaz.mpazpro.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.mpaz.mpazpro.data.repository.MPazRepository
import cl.mpaz.mpazpro.domain.model.LoginEstado
import cl.mpaz.mpazpro.utils.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class LoginViewModel(private val repo: MPazRepository) : ViewModel() {

    private val _estado = MutableStateFlow(LoginEstado())
    val estado: StateFlow<LoginEstado> = _estado.asStateFlow()

    init {

        viewModelScope.launch {
            val recordado = repo.usuarioRecordado.first()
            if (recordado.isNotBlank()) _estado.update { it.copy(usuario = recordado, recordar = true) }
        }
    }


    fun onUsuarioCambia(valor: String) {
        _estado.update { it.copy(usuario = valor.trim(), errorUsuario = Validaciones.usuario(valor.trim()), errorGeneral = null) }
    }

    fun onPinCambia(valor: String) {
        val pin = valor.take(4)
        _estado.update { it.copy(pin = pin, errorPin = Validaciones.pin(pin), errorGeneral = null) }
    }

    fun onRecordarCambia(valor: Boolean) {
        _estado.update { it.copy(recordar = valor) }
    }

    fun ingresar() {
        val actual = _estado.value

        val errorUsuario = Validaciones.usuario(actual.usuario)
        val errorPin = Validaciones.pin(actual.pin)
        if (errorUsuario != null || errorPin != null) {
            _estado.update { it.copy(errorUsuario = errorUsuario, errorPin = errorPin) }
            return
        }

        viewModelScope.launch {
            _estado.update { it.copy(cargando = true) }
            val usuario = repo.iniciarSesion(actual.usuario, actual.pin)
            if (usuario == null) {
                _estado.update { it.copy(cargando = false, pin = "", errorGeneral = "Usuario o PIN incorrectos") }
            } else {
                repo.recordarUsuario(if (actual.recordar) actual.usuario else "")
                _estado.update { it.copy(cargando = false, rolIngresado = usuario.rol) }
            }
        }
    }


    fun navegacionHecha() {
        _estado.update { it.copy(rolIngresado = null, pin = "") }
    }
}