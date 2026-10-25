package cl.mpaz.mpazpro.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.mpaz.mpazpro.data.local.ContenidoEntity
import cl.mpaz.mpazpro.data.repository.MPazRepository
import cl.mpaz.mpazpro.domain.model.Estado
import cl.mpaz.mpazpro.domain.model.RevisionEstado
import cl.mpaz.mpazpro.utils.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UtpViewModel(private val repo: MPazRepository) : ViewModel() {

    val nombre: String = repo.usuarioActual?.nombre ?: ""


    val contenidos: StateFlow<List<ContenidoEntity>> = repo.contenidos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _revision = MutableStateFlow(RevisionEstado())
    val revision: StateFlow<RevisionEstado> = _revision.asStateFlow()

    fun abrirRevision(contenido: ContenidoEntity) {
        _revision.value = RevisionEstado(contenidoId = contenido.id, observacion = contenido.observacionUtp)
        viewModelScope.launch {
            val preguntas = repo.todasLasPreguntas(contenido.id)
            _revision.update { it.copy(preguntas = preguntas) }
        }
    }

    fun cerrarRevision() {
        _revision.value = RevisionEstado()
    }

    fun onObservacionCambia(valor: String) {
        _revision.update { it.copy(observacion = valor, errorObservacion = Validaciones.observacion(valor, esRechazo = false)) }
    }

    fun aprobar() = resolver(Estado.APROBADO)
    fun rechazar() = resolver(Estado.RECHAZADO)

    private fun resolver(nuevo: Estado) {
        val actual = _revision.value

        val error = Validaciones.observacion(actual.observacion, esRechazo = nuevo == Estado.RECHAZADO)
        if (error != null) {
            _revision.update { it.copy(errorObservacion = error) }
            return
        }
        // Solo se aprueba si el contenido tiene desafíos y prueba final
        if (nuevo == Estado.APROBADO) {
            Validaciones.preguntasMinimas(actual.preguntas)?.let { msg ->
                _revision.update { it.copy(errorAprobacion = "No se puede aprobar: $msg") }
                return
            }
        }
        val c = contenidos.value.firstOrNull { it.id == actual.contenidoId } ?: return
        viewModelScope.launch {
            // si se rechaza, se quita la asignación para que deje de verse en el curso
            repo.actualizar(
                c.copy(
                    estado = nuevo, observacionUtp = actual.observacion.trim(),
                    asignado = if (nuevo == Estado.APROBADO) c.asignado else false
                )
            )
            cerrarRevision()
        }
    }


    fun eliminar(contenido: ContenidoEntity) {
        viewModelScope.launch { repo.eliminar(contenido) }
    }

    fun cerrarSesion() = repo.cerrarSesion()
}