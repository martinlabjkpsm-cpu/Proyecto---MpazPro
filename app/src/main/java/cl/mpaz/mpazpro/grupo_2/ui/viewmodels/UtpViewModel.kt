package cl.mpaz.mpazpro.grupo_2.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.mpaz.mpazpro.grupo_2.data.local.ContenidoEntity
import cl.mpaz.mpazpro.grupo_2.data.repository.MPazRepository
import cl.mpaz.mpazpro.grupo_2.domain.model.Estado
import cl.mpaz.mpazpro.grupo_2.domain.model.RevisionEstado
import cl.mpaz.mpazpro.grupo_2.utils.Validaciones
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

    /** Pide al servidor los datos más recientes (la pantalla lo llama cada vez que se abre). */
    fun refrescar() {
        viewModelScope.launch { repo.sincronizar() }
    }

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