package cl.mpaz.mpazpro.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.mpaz.mpazpro.data.local.ContenidoEntity
import cl.mpaz.mpazpro.data.local.IntentoEntity
import cl.mpaz.mpazpro.data.local.PreguntaEntity
import cl.mpaz.mpazpro.data.repository.MPazRepository
import cl.mpaz.mpazpro.domain.model.ActividadEstado
import cl.mpaz.mpazpro.domain.model.Estado
import cl.mpaz.mpazpro.domain.model.TipoActividad
import cl.mpaz.mpazpro.nativo.Notificaciones
import cl.mpaz.mpazpro.nativo.Vibracion
import cl.mpaz.mpazpro.utils.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// viewmodel compartido

class EstudianteViewModel(
    private val repo: MPazRepository,
    private val vibracion: Vibracion,
    private val notificaciones: Notificaciones
) : ViewModel() {

    val nombre: String = repo.usuarioActual?.nombre ?: ""
    private val estudianteId: Int = repo.usuarioActual?.id ?: -1


    val contenidos: StateFlow<List<ContenidoEntity>> = repo.contenidos()
        .map { lista -> lista.filter { it.estado == Estado.APROBADO && it.asignado } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val intentos: StateFlow<List<IntentoEntity>> = repo.intentosDe(estudianteId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())


    fun desafioHecho(contenidoId: Int, intentos: List<IntentoEntity>) =
        intentos.any { it.contenidoId == contenidoId && it.tipo == TipoActividad.DESAFIO }

    fun intentosUsados(contenidoId: Int, intentos: List<IntentoEntity>) =
        intentos.count { it.contenidoId == contenidoId && it.tipo == TipoActividad.PRUEBA }


    fun puedeRendirPrueba(c: ContenidoEntity, intentos: List<IntentoEntity>) =
        desafioHecho(c.id, intentos) && intentosUsados(c.id, intentos) < c.maxIntentos

    private val _preguntas = MutableStateFlow<List<PreguntaEntity>>(emptyList())
    val preguntas: StateFlow<List<PreguntaEntity>> = _preguntas.asStateFlow()

    var tipoActual: TipoActividad = TipoActividad.DESAFIO
        private set
    private var contenidoActual: Int = 0

    private val _actividad = MutableStateFlow(ActividadEstado())
    val actividad: StateFlow<ActividadEstado> = _actividad.asStateFlow()

    fun iniciarActividad(contenidoId: Int, tipo: TipoActividad) {

        val enCurso = contenidoId == contenidoActual && tipo == tipoActual &&
                _preguntas.value.isNotEmpty() && !_actividad.value.terminada
        if (enCurso) return
        contenidoActual = contenidoId
        tipoActual = tipo
        _actividad.value = ActividadEstado()
        _preguntas.value = emptyList()
        viewModelScope.launch {
            _preguntas.value = repo.preguntas(contenidoId, tipo)
        }
    }

    fun seleccionar(opcion: Int) {
        if (_actividad.value.comprobada) return
        _actividad.update { it.copy(seleccion = opcion, errorSeleccion = null) }
    }


    fun comprobar() {
        val actual = _actividad.value
        val error = Validaciones.seleccion(actual.seleccion)
        if (error != null) {
            _actividad.update { it.copy(errorSeleccion = error) }
            return
        }
        val elegida = actual.seleccion ?: return
        val nuevas = actual.respuestas + elegida
        if (tipoActual == TipoActividad.DESAFIO) {

            if (elegida == _preguntas.value[actual.indice].correcta) vibracion.correcto() else vibracion.incorrecto()
            _actividad.update { it.copy(respuestas = nuevas, comprobada = true) }
        } else {

            _actividad.update { it.copy(respuestas = nuevas) }
            siguiente()
        }
    }

    fun siguiente() {
        val actual = _actividad.value
        if (actual.indice >= _preguntas.value.lastIndex) terminar()
        else _actividad.update { it.copy(indice = it.indice + 1, seleccion = null, comprobada = false) }
    }

    private fun terminar() {
        val respuestas = _actividad.value.respuestas
        val preguntas = _preguntas.value
        viewModelScope.launch {
            repo.guardarIntento(contenidoActual, tipoActual, preguntas, respuestas)
            val correctas = preguntas.indices.count { respuestas[it] == preguntas[it].correcta }

            notificaciones.mostrar("¡Actividad terminada!", "$correctas de ${preguntas.size} correctas. Tu avance quedó guardado.")
            _actividad.update { it.copy(terminada = true, correctas = correctas) }
        }
    }

    fun cerrarSesion() = repo.cerrarSesion()
}