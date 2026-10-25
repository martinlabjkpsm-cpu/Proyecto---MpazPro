package cl.mpaz.mpazpro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.mpaz.mpazpro.domain.model.Estado
import cl.mpaz.mpazpro.ui.components.BarraSuperior
import cl.mpaz.mpazpro.ui.components.CampoValidado
import cl.mpaz.mpazpro.ui.components.EtiquetaEstado
import cl.mpaz.mpazpro.ui.components.MensajeError
import cl.mpaz.mpazpro.ui.theme.Ambar
import cl.mpaz.mpazpro.ui.theme.Rojo
import cl.mpaz.mpazpro.ui.theme.Verde
import cl.mpaz.mpazpro.di.AppViewModelFactory
import cl.mpaz.mpazpro.ui.viewmodels.UtpViewModel

//vista jefatura
@Composable
fun UtpScreen(
    esTablet: Boolean,
    onSalir: () -> Unit,
    viewModel: UtpViewModel = viewModel(factory = AppViewModelFactory.Factory)
) {
    val contenidos by viewModel.contenidos.collectAsStateWithLifecycle()
    val revision by viewModel.revision.collectAsStateWithLifecycle()

    Scaffold(topBar = { BarraSuperior(viewModel.nombre, onSalir = { viewModel.cerrarSesion(); onSalir() }) }) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (esTablet) 2 else 1),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            items(contenidos, key = { it.id }) { c ->
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${c.asignatura} · ${c.codigoOa}", style = MaterialTheme.typography.titleMedium)
                        if (c.unidad.isNotBlank()) {
                            Text("${c.unidad} · Eje: ${c.eje}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(c.objetivo)
                        if (c.recursoReferencia.isNotBlank()) {
                            Text("Referencia: ${c.recursoReferencia}", style = MaterialTheme.typography.bodySmall)
                        }
                        when (c.estado) {
                            Estado.PENDIENTE -> EtiquetaEstado("Pendiente", Ambar)
                            Estado.APROBADO -> EtiquetaEstado("Aprobado", Verde)
                            Estado.RECHAZADO -> EtiquetaEstado("Rechazado: ${c.observacionUtp}", Rojo)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { viewModel.abrirRevision(c) }) { Text("Revisar") }
                            if (c.estado == Estado.RECHAZADO) {
                                OutlinedButton(onClick = { viewModel.eliminar(c) }) { Text("Eliminar") }
                            }
                        }
                    }
                }
            }
        }
    }

    if (revision.contenidoId != null) {
        AlertDialog(
            onDismissRequest = viewModel::cerrarRevision,
            title = { Text("Revisar contenido") },
            text = {
                val c = contenidos.firstOrNull { it.id == revision.contenidoId }
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (c != null) {
                        Text(c.objetivo, style = MaterialTheme.typography.titleSmall)
                        Text(c.explicacion)
                        if (c.ejemplo.isNotBlank()) Text("Ejemplo: ${c.ejemplo}")
                    }
                    Text("Preguntas (${revision.preguntas.size})", style = MaterialTheme.typography.titleSmall)
                    val letras = listOf("A", "B", "C")
                    revision.preguntas.forEach { q ->
                        Text(
                            "• [${q.tipo.name.lowercase()} · nivel ${q.dificultad}] ${q.enunciado} " +
                                    "→ correcta: ${letras[q.correcta]}) ${q.opciones()[q.correcta]}"
                        )
                    }
                    MensajeError(revision.errorAprobacion)
                    CampoValidado(revision.observacion, viewModel::onObservacionCambia, "Observación (obligatoria si rechazas)",
                        Icons.Filled.RateReview, revision.errorObservacion, lineas = 3)
                }
            },
            confirmButton = { Button(onClick = viewModel::aprobar) { Text("Aprobar") } },
            dismissButton = { TextButton(onClick = viewModel::rechazar) { Text("Rechazar") } }
        )
    }
}