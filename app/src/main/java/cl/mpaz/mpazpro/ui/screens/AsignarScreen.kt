package cl.mpaz.mpazpro.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.mpaz.mpazpro.ui.components.BarraSuperior
import cl.mpaz.mpazpro.ui.components.BotonPrincipal
import cl.mpaz.mpazpro.ui.components.CampoValidado
import cl.mpaz.mpazpro.ui.theme.Verde
import cl.mpaz.mpazpro.ui.viewmodels.DocenteViewModel

//vista de asignacion para el docente

@Composable
fun AsignarScreen(viewModel: DocenteViewModel, contenidoId: Int, onVolver: () -> Unit) {
    LaunchedEffect(contenidoId) { viewModel.cargarAsignacion(contenidoId) }
    val estado by viewModel.asignar.collectAsStateWithLifecycle()

    Scaffold(topBar = { BarraSuperior("Asignar actividad", onVolver = onVolver) }) { innerPadding ->
        Column(Modifier.padding(innerPadding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            CampoValidado(
                valor = estado.intentos,
                onCambio = viewModel::onIntentosCambia,
                etiqueta = "Máximo de intentos de la prueba (1 a 5)",
                icono = Icons.Filled.Repeat,
                error = estado.errorIntentos,
                soloNumeros = true
            )

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Visible para los estudiantes", Modifier.weight(1f))
                Switch(checked = estado.asignado, onCheckedChange = viewModel::onAsignadoCambia)
            }
            BotonPrincipal("Guardar", viewModel::guardarAsignacion)
            AnimatedVisibility(visible = estado.guardado) {
                Text("✓ Asignación guardada", color = Verde, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}