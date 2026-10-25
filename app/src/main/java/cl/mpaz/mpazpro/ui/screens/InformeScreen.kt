package cl.mpaz.mpazpro.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.mpaz.mpazpro.domain.model.LogroPregunta
import cl.mpaz.mpazpro.ui.components.BarraSuperior
import cl.mpaz.mpazpro.ui.components.BotonPrincipal
import cl.mpaz.mpazpro.ui.components.CampoValidado
import cl.mpaz.mpazpro.ui.components.MensajeError
import cl.mpaz.mpazpro.ui.theme.Verde
import cl.mpaz.mpazpro.ui.viewmodels.DocenteViewModel
import cl.mpaz.mpazpro.utils.Fechas

//vista informe
@Composable
fun InformeScreen(viewModel: DocenteViewModel, contenidoId: Int, onVolver: () -> Unit) {
    LaunchedEffect(contenidoId) { viewModel.cargarInforme(contenidoId) }
    val informe by viewModel.informe.collectAsStateWithLifecycle()
    val validacion by viewModel.validacion.collectAsStateWithLifecycle()

    // Descarga: el sistema abre el selector "Guardar como" y devuelve dónde guardar
    val lanzadorCsv = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let(viewModel::exportar)
    }

    Scaffold(topBar = { BarraSuperior("Informe pedagógico", onVolver = onVolver) }) { innerPadding ->
        Column(
            Modifier.padding(innerPadding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Informe preliminar de uso docente. Describe resultados; no es un diagnóstico ni una decisión académica.")

            if (informe.sinDatos) {
                Text("Aún no hay pruebas finales rendidas para este objetivo.")
            } else {
                Text("Rendimiento general: ${informe.porcentajeGeneral}%", style = MaterialTheme.typography.titleLarge)
                Text("Logro por pregunta", style = MaterialTheme.typography.titleMedium)
                informe.logros.forEach { BarraLogro(it) }

                HorizontalDivider()
                Text("Fortalezas (70% o más)", style = MaterialTheme.typography.titleMedium)
                if (informe.fortalezas.isEmpty()) Text("—")
                informe.fortalezas.forEach { Text("• ${it.enunciado}") }
                Text("Aspectos a reforzar (menos de 50%)", style = MaterialTheme.typography.titleMedium)
                if (informe.aReforzar.isEmpty()) Text("—")
                informe.aReforzar.forEach { Text("• ${it.enunciado}") }

                HorizontalDivider()
                Text("Resultados por estudiante (ficticios)", style = MaterialTheme.typography.titleMedium)
                informe.estudiantes.forEach { e -> Text("${e.nombre}: ${e.intentos} intento(s), mejor ${e.mejorPorcentaje}%") }

                if (validacion.intentosNuevos > 0) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(
                            "Hay ${validacion.intentosNuevos} intento(s) nuevo(s) desde la última validación. " +
                                    "Revisa y valida nuevamente.",
                            Modifier.padding(12.dp)
                        )
                    }
                }
                OutlinedButton(onClick = { lanzadorCsv.launch(viewModel.nombreArchivo()) }) {
                    Icon(Icons.Filled.Download, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Descargar informe (CSV)")
                }
                validacion.mensajeExportacion?.let { Text(it, color = Verde) }

                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Validación docente", style = MaterialTheme.typography.titleMedium)
                        CampoValidado(validacion.comentario, viewModel::onComentarioCambia, "Comentario profesional",
                            Icons.Filled.RateReview, validacion.errorComentario, lineas = 3)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = validacion.revisado, onCheckedChange = viewModel::onRevisadoCambia)
                            Text("Revisé profesionalmente este informe")
                        }
                        MensajeError(validacion.errorRevisado)
                        BotonPrincipal("Validar informe", viewModel::validarInforme)
                        AnimatedVisibility(visible = validacion.validado) {
                            Text("✓ Informe validado el ${Fechas.formato(validacion.fechaValidacion)}", color = Verde)
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun BarraLogro(logro: LogroPregunta) {
    val progreso by animateFloatAsState(logro.porcentaje / 100f, label = "logro")
    Column {
        Text("${logro.enunciado} — ${logro.porcentaje}%", style = MaterialTheme.typography.bodyMedium)
        LinearProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxWidth())
    }
}