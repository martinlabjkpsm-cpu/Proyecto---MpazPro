package cl.mpaz.mpazpro.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.mpaz.mpazpro.data.local.ContenidoEntity
import cl.mpaz.mpazpro.data.local.PreguntaEntity
import cl.mpaz.mpazpro.data.repository.MPazRepository
import cl.mpaz.mpazpro.domain.model.CampoContenido
import cl.mpaz.mpazpro.domain.model.ContenidoFormEstado
import cl.mpaz.mpazpro.domain.model.Estado
import cl.mpaz.mpazpro.domain.model.PreguntaFormEstado
import cl.mpaz.mpazpro.utils.Validaciones
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Formulario del docente para proponer o corregir un contenido y sus preguntas.
// Todo cambio deja el contenido PENDIENTE para que la UTP lo autorice de nuevo.

class ContenidoFormViewModel(private val repo: MPazRepository) : ViewModel() {

    private val _estado = MutableStateFlow(ContenidoFormEstado())
    val estado: StateFlow<ContenidoFormEstado> = _estado.asStateFlow()

    private val _preguntas = MutableStateFlow<List<PreguntaEntity>>(emptyList())
    val preguntas: StateFlow<List<PreguntaEntity>> = _preguntas.asStateFlow()

    private val _pregunta = MutableStateFlow(PreguntaFormEstado())
    val pregunta: StateFlow<PreguntaFormEstado> = _pregunta.asStateFlow()

    private var original: ContenidoEntity? = null
    private var cargadoId: Int? = null

    fun cargar(id: Int) {
        if (cargadoId == id) return          // evita recargar al rotar la pantalla
        cargadoId = id
        if (id <= 0) return                  // contenido nuevo: formulario vacío
        viewModelScope.launch {
            val c = repo.contenido(id) ?: return@launch
            original = c
            _estado.value = ContenidoFormEstado(
                id = c.id, asignatura = c.asignatura, unidad = c.unidad, eje = c.eje, codigoOa = c.codigoOa,
                objetivo = c.objetivo, explicacion = c.explicacion, ejemplo = c.ejemplo,
                recurso = c.recursoReferencia, estado = c.estado, observacionUtp = c.observacionUtp
            )
            refrescarPreguntas()
        }
    }

    fun onCambio(campo: CampoContenido, valor: String) {
        _estado.update { e ->
            val n = when (campo) {
                CampoContenido.ASIGNATURA -> e.copy(asignatura = valor)
                CampoContenido.UNIDAD -> e.copy(unidad = valor)
                CampoContenido.EJE -> e.copy(eje = valor)
                CampoContenido.CODIGO -> e.copy(codigoOa = valor.uppercase())
                CampoContenido.OBJETIVO -> e.copy(objetivo = valor)
                CampoContenido.EXPLICACION -> e.copy(explicacion = valor)
                CampoContenido.EJEMPLO -> e.copy(ejemplo = valor)
                CampoContenido.RECURSO -> e.copy(recurso = valor)
            }
            // valida el campo que cambió (y el código, si cambió la asignatura)
            val todos = Validaciones.contenido(n)
            val revisar = if (campo == CampoContenido.ASIGNATURA) listOf(campo, CampoContenido.CODIGO) else listOf(campo)
            var errores = n.errores
            revisar.forEach { k ->
                val error = todos[k]
                errores = if (error != null) errores + (k to error) else errores - k
            }
            n.copy(errores = errores, guardado = false)
        }
    }

    fun guardar() {
        val e = _estado.value
        viewModelScope.launch {
            val errores = Validaciones.contenido(e).toMutableMap()
            if (e.asignatura.isNotBlank()) {
                val unidades = repo.contenidosDe(e.asignatura).filter { it.id != e.id }.map { it.unidad }
                Validaciones.unidadUnica(e.unidad, unidades)?.let { errores[CampoContenido.UNIDAD] = it }
            }
            if (errores.isNotEmpty()) {
                _estado.update { it.copy(errores = errores) }
                return@launch
            }
            val base = original ?: ContenidoEntity(asignatura = "", codigoOa = "", objetivo = "", explicacion = "")
            val id = repo.guardarContenido(
                base.copy(
                    asignatura = e.asignatura, unidad = e.unidad.trim(), eje = e.eje.trim(),
                    codigoOa = e.codigoOa.trim(), objetivo = e.objetivo.trim(),
                    explicacion = e.explicacion.trim(), ejemplo = e.ejemplo.trim(),
                    recursoReferencia = e.recurso.trim(),
                    estado = Estado.PENDIENTE, asignado = false          // vuelve a revisión UTP
                )
            )
            original = repo.contenido(id)
            cargadoId = id
            _estado.update { it.copy(id = id, estado = Estado.PENDIENTE, errores = emptyMap(), guardado = true) }
            refrescarPreguntas()
        }
    }


    // ---------- Preguntas ----------

    fun onPregunta(cambio: (PreguntaFormEstado) -> PreguntaFormEstado) {
        _pregunta.update { actual ->
            val n = cambio(actual)
            n.copy(errores = if (n.intentado) Validaciones.pregunta(n) else emptyMap())
        }
    }

    fun agregarPregunta() {
        val p = _pregunta.value
        val errores = Validaciones.pregunta(p)
        if (errores.isNotEmpty()) {
            _pregunta.update { it.copy(errores = errores, intentado = true) }
            return
        }
        val contenidoId = _estado.value.id
        val correcta = p.correcta ?: return
        if (contenidoId <= 0) return
        viewModelScope.launch {
            repo.agregarPregunta(
                PreguntaEntity(
                    contenidoId = contenidoId, tipo = p.tipo, enunciado = p.enunciado.trim(),
                    opcionA = p.opcionA.trim(), opcionB = p.opcionB.trim(), opcionC = p.opcionC.trim(),
                    correcta = correcta, dificultad = p.dificultad, ayuda = p.ayuda.trim()
                )
            )
            repo.volverARevision(contenidoId)
            _pregunta.value = PreguntaFormEstado(tipo = p.tipo)     // limpia el formulario
            _estado.update { it.copy(estado = Estado.PENDIENTE) }
            refrescarPreguntas()
        }
    }

    fun eliminarPregunta(p: PreguntaEntity) {
        viewModelScope.launch {
            repo.eliminarPregunta(p)
            repo.volverARevision(p.contenidoId)
            _estado.update { it.copy(estado = Estado.PENDIENTE) }
            refrescarPreguntas()
        }
    }

    private suspend fun refrescarPreguntas() {
        val id = _estado.value.id
        _preguntas.value = if (id > 0) repo.todasLasPreguntas(id) else emptyList()
        _estado.update {
            it.copy(errorPreguntas = if (id > 0) Validaciones.preguntasMinimas(_preguntas.value) else null)
        }
    }
}
