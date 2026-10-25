package cl.mpaz.mpazpro.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.mpaz.mpazpro.di.AppViewModelFactory
import cl.mpaz.mpazpro.domain.model.CampoContenido
import cl.mpaz.mpazpro.domain.model.CampoPregunta
import cl.mpaz.mpazpro.domain.model.Estado
import cl.mpaz.mpazpro.domain.model.TipoActividad
import cl.mpaz.mpazpro.ui.components.BarraSuperior
import cl.mpaz.mpazpro.ui.components.BotonPrincipal
import cl.mpaz.mpazpro.ui.components.CampoValidado
import cl.mpaz.mpazpro.ui.components.EtiquetaEstado
import cl.mpaz.mpazpro.ui.components.MensajeError
import cl.mpaz.mpazpro.ui.theme.Ambar
import cl.mpaz.mpazpro.ui.theme.Rojo
import cl.mpaz.mpazpro.ui.theme.Verde
import cl.mpaz.mpazpro.ui.viewmodels.ContenidoFormViewModel
import cl.mpaz.mpazpro.utils.Validaciones

// vista del docente para proponer o corregir un contenido y sus preguntas

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContenidoFormScreen(
    contenidoId: Int,
    onVolver: () -> Unit,
    viewModel: ContenidoFormViewModel = viewModel(factory = AppViewModelFactory.Factory)
) {
    LaunchedEffect(contenidoId) { viewModel.cargar(contenidoId) }
    val e by viewModel.estado.collectAsStateWithLifecycle()
    val preguntas by viewModel.preguntas.collectAsStateWithLifecycle()
    val p by viewModel.pregunta.collectAsStateWithLifecycle()

    val titulo = if (e.id > 0) "Editar contenido" else "Nuevo contenido"
    Scaffold(topBar = { BarraSuperior(titulo, onVolver = onVolver) }) { innerPadding ->
        Column(
            Modifier.padding(innerPadding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (e.id > 0) {
                when (e.estado) {
                    Estado.PENDIENTE -> EtiquetaEstado("Pendiente de revisión UTP", Ambar)
                    Estado.APROBADO -> EtiquetaEstado("Aprobado por UTP", Verde)
                    Estado.RECHAZADO -> EtiquetaEstado("Rechazado por UTP", Rojo)
                }
            }
            if (e.observacionUtp.isNotBlank()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text("Observación UTP: ${e.observacionUtp}", Modifier.padding(12.dp))
                }
            }

            // ---------- Datos curriculares ----------
            Text("Asignatura", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Validaciones.ASIGNATURAS.keys.forEach { a ->
                    FilterChip(
                        selected = e.asignatura == a,
                        onClick = { viewModel.onCambio(CampoContenido.ASIGNATURA, a) },
                        label = { Text(a) }
                    )
                }
            }
            MensajeError(e.errores[CampoContenido.ASIGNATURA])

            CampoValidado(e.unidad, { viewModel.onCambio(CampoContenido.UNIDAD, it) }, "Unidad",
                Icons.Filled.Folder, e.errores[CampoContenido.UNIDAD])
            CampoValidado(e.eje, { viewModel.onCambio(CampoContenido.EJE, it) }, "Eje curricular",
                Icons.Filled.Category, e.errores[CampoContenido.EJE])
            CampoValidado(e.codigoOa, { viewModel.onCambio(CampoContenido.CODIGO, it) }, "Código OA (ej: MA03 OA 01)",
                Icons.Filled.Tag, e.errores[CampoContenido.CODIGO])
            CampoValidado(e.objetivo, { viewModel.onCambio(CampoContenido.OBJETIVO, it) }, "Objetivo de aprendizaje",
                Icons.Filled.Flag, e.errores[CampoContenido.OBJETIVO], lineas = 2)
            CampoValidado(e.explicacion, { viewModel.onCambio(CampoContenido.EXPLICACION, it) }, "Explicación para el estudiante",
                Icons.Filled.Description, e.errores[CampoContenido.EXPLICACION], lineas = 4)
            CampoValidado(e.ejemplo, { viewModel.onCambio(CampoContenido.EJEMPLO, it) }, "Ejemplo (opcional)",
                Icons.Filled.Lightbulb, e.errores[CampoContenido.EJEMPLO], lineas = 2)
            CampoValidado(e.recurso, { viewModel.onCambio(CampoContenido.RECURSO, it) }, "Recurso de referencia",
                Icons.Filled.Link, e.errores[CampoContenido.RECURSO])

            BotonPrincipal(if (e.id > 0) "Guardar y reenviar a UTP" else "Guardar y enviar a UTP", viewModel::guardar)
            AnimatedVisibility(visible = e.guardado) {
                Text("✓ Contenido enviado a revisión UTP", color = Verde, style = MaterialTheme.typography.titleMedium)
            }

            // ---------- Preguntas: disponibles después de guardar el contenido ----------
            if (e.id > 0) {
                HorizontalDivider()
                Text("Preguntas (${preguntas.size})", style = MaterialTheme.typography.titleLarge)
                MensajeError(e.errorPreguntas)
                preguntas.forEach { q ->
                    Card(Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "${if (q.tipo == TipoActividad.DESAFIO) "Desafío" else "Prueba"} · dificultad ${q.dificultad}",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Text(q.enunciado)
                            }
                            IconButton(onClick = { viewModel.eliminarPregunta(q) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Eliminar pregunta")
                            }
                        }
                    }
                }

                Text("Agregar pregunta", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TipoActividad.entries.forEach { t ->
                        FilterChip(
                            selected = p.tipo == t,
                            onClick = { viewModel.onPregunta { it.copy(tipo = t) } },
                            label = { Text(if (t == TipoActividad.DESAFIO) "Desafío" else "Prueba final") }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Dificultad:")
                    (1..3).forEach { d ->
                        FilterChip(
                            selected = p.dificultad == d,
                            onClick = { viewModel.onPregunta { it.copy(dificultad = d) } },
                            label = { Text("$d") }
                        )
                    }
                }
                CampoValidado(p.enunciado, { v -> viewModel.onPregunta { it.copy(enunciado = v) } }, "Enunciado",
                    Icons.Filled.Quiz, p.errores[CampoPregunta.ENUNCIADO], lineas = 2)

                val opciones = listOf(
                    Triple(p.opcionA, CampoPregunta.OPCION_A, "A"),
                    Triple(p.opcionB, CampoPregunta.OPCION_B, "B"),
                    Triple(p.opcionC, CampoPregunta.OPCION_C, "C")
                )
                opciones.forEachIndexed { i, (valor, campo, letra) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = p.correcta == i, onClick = { viewModel.onPregunta { it.copy(correcta = i) } })
                        Column(Modifier.weight(1f)) {
                            CampoValidado(
                                valor,
                                { v ->
                                    viewModel.onPregunta {
                                        when (i) {
                                            0 -> it.copy(opcionA = v)
                                            1 -> it.copy(opcionB = v)
                                            else -> it.copy(opcionC = v)
                                        }
                                    }
                                },
                                "Alternativa $letra", Icons.Filled.Tag, p.errores[campo]
                            )
                        }
                    }
                }
                Text("Marca con el círculo la alternativa correcta.", style = MaterialTheme.typography.bodyMedium)
                MensajeError(p.errores[CampoPregunta.CORRECTA])
                CampoValidado(p.ayuda, { v -> viewModel.onPregunta { it.copy(ayuda = v) } }, "Retroalimentación al responder",
                    Icons.Filled.Lightbulb, p.errores[CampoPregunta.AYUDA], lineas = 2)
                BotonPrincipal("Agregar pregunta", viewModel::agregarPregunta)
            }
        }
    }
}