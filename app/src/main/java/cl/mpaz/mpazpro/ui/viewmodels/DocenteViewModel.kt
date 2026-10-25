package cl.mpaz.mpazpro.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.mpaz.mpazpro.data.local.ContenidoEntity
import cl.mpaz.mpazpro.data.repository.MPazRepository
import cl.mpaz.mpazpro.domain.model.AsignarEstado
import cl.mpaz.mpazpro.domain.model.Informe
import cl.mpaz.mpazpro.domain.model.LogroPregunta
import cl.mpaz.mpazpro.domain.model.ResultadoEstudiante
import cl.mpaz.mpazpro.domain.model.TipoActividad
import cl.mpaz.mpazpro.domain.model.ValidacionEstado
import cl.mpaz.mpazpro.utils.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class DocenteViewModel(private val repo: MPazRepository) : ViewModel() {

    val nombre: String = repo.usuarioActual?.nombre ?: ""


    // El docente ve todos sus contenidos (pendientes, aprobados y rechazados con la observación UTP)
    val contenidos: StateFlow<List<ContenidoEntity>> = repo.contenidos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var seleccionado: ContenidoEntity? = null


    private val _asignar = MutableStateFlow(AsignarEstado())
    val asignar: StateFlow<AsignarEstado> = _asignar.asStateFlow()

    fun cargarAsignacion(contenidoId: Int) {
        viewModelScope.launch {
            val c = repo.contenido(contenidoId) ?: return@launch
            seleccionado = c
            _asignar.value = AsignarEstado(intentos = c.maxIntentos.toString(), asignado = c.asignado)
        }
    }

    fun onIntentosCambia(valor: String) {
        _asignar.update { it.copy(intentos = valor.take(1), errorIntentos = Validaciones.intentos(valor.take(1))) }
    }

    fun onAsignadoCambia(valor: Boolean) {
        _asignar.update { it.copy(asignado = valor) }
    }

    fun guardarAsignacion() {
        val actual = _asignar.value
        val error = Validaciones.intentos(actual.intentos)
        if (error != null) {
            _asignar.update { it.copy(errorIntentos = error) }
            return
        }
        val c = seleccionado ?: return
        viewModelScope.launch {
            // No se puede dejar visible un objetivo sin desafíos ni prueba final
            if (actual.asignado) {
                Validaciones.preguntasMinimas(repo.todasLasPreguntas(c.id))?.let { msg ->
                    _asignar.update { it.copy(errorIntentos = msg) }
                    return@launch
                }
            }
            repo.actualizar(c.copy(maxIntentos = actual.intentos.toInt(), asignado = actual.asignado))   // [ROOM] Update
            _asignar.update { it.copy(guardado = true) }
        }
    }


    private val _informe = MutableStateFlow(Informe())
    val informe: StateFlow<Informe> = _informe.asStateFlow()

    private val _validacion = MutableStateFlow(ValidacionEstado())
    val validacion: StateFlow<ValidacionEstado> = _validacion.asStateFlow()

    fun cargarInforme(contenidoId: Int) {
        viewModelScope.launch {
            val c = repo.contenido(contenidoId) ?: return@launch
            seleccionado = c
            _validacion.value = ValidacionEstado(comentario = c.comentarioDocente, validado = c.informeValidado)

            val preguntas = repo.preguntas(contenidoId, TipoActividad.PRUEBA)
            val intentos = repo.intentosPrueba(contenidoId)
            if (intentos.isEmpty()) {
                _informe.value = Informe(sinDatos = true)
                return@launch
            }
            val respuestas = repo.respuestasDe(intentos.map { it.id })


            val logros = preguntas.map { p ->
                val deEsta = respuestas.filter { it.preguntaId == p.id }
                val porcentaje = if (deEsta.isEmpty()) 0 else deEsta.count { it.esCorrecta } * 100 / deEsta.size
                LogroPregunta(p.enunciado, porcentaje)
            }

            val estudiantes = repo.estudiantes().mapNotNull { e ->
                val suyos = intentos.filter { it.estudianteId == e.id }
                if (suyos.isEmpty()) null
                else ResultadoEstudiante(e.nombre, suyos.size, suyos.maxOf { it.correctas * 100 / it.total })
            }
            val general = intentos.sumOf { it.correctas } * 100 / intentos.sumOf { it.total }
            _informe.value = Informe(general, logros, estudiantes, sinDatos = false)
        }
    }


    fun onComentarioCambia(valor: String) {
        _validacion.update { it.copy(comentario = valor, errorComentario = Validaciones.comentario(valor)) }
    }

    fun onRevisadoCambia(valor: Boolean) {
        _validacion.update { it.copy(revisado = valor, errorRevisado = null) }
    }

    fun validarInforme() {
        val actual = _validacion.value
        val errorComentario = Validaciones.comentario(actual.comentario)
        val errorRevisado = Validaciones.aceptacion(actual.revisado)
        if (errorComentario != null || errorRevisado != null) {
            _validacion.update { it.copy(errorComentario = errorComentario, errorRevisado = errorRevisado) }
            return
        }
        val c = seleccionado ?: return
        viewModelScope.launch {
            repo.actualizar(c.copy(informeValidado = true, comentarioDocente = actual.comentario.trim()))
            _validacion.update { it.copy(validado = true) }
        }
    }

    fun cerrarSesion() = repo.cerrarSesion()
}