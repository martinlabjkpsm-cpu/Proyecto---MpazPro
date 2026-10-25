package cl.mpaz.mpazpro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.mpaz.mpazpro.di.AppViewModelFactory
import cl.mpaz.mpazpro.domain.model.Estado
import cl.mpaz.mpazpro.ui.components.BarraSuperior
import cl.mpaz.mpazpro.ui.components.EtiquetaEstado
import cl.mpaz.mpazpro.ui.theme.Ambar
import cl.mpaz.mpazpro.ui.theme.Rojo
import cl.mpaz.mpazpro.ui.theme.Verde
import cl.mpaz.mpazpro.ui.viewmodels.DocenteViewModel

//vista docente: todos los contenidos con su estado UTP, asignar, informe, editar y crear nuevos

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DocenteScreen(
    esTablet: Boolean,
    onAsignar: (Int) -> Unit,
    onInforme: (Int) -> Unit,
    onEditar: (Int) -> Unit,
    onNuevo: () -> Unit,
    onSalir: () -> Unit,
    viewModel: DocenteViewModel = viewModel(factory = AppViewModelFactory.Factory)
) {
    val contenidos by viewModel.contenidos.collectAsStateWithLifecycle()

    // id del contenido aprobado que se quiere editar (pide confirmación porque vuelve a revisión)
    var confirmarEdicion by rememberSaveable { mutableStateOf<Int?>(null) }

    Scaffold(
        topBar = { BarraSuperior(viewModel.nombre, onSalir = { viewModel.cerrarSesion(); onSalir() }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNuevo,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nuevo contenido") }
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (esTablet) 2 else 1),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            items(contenidos, key = { it.id }) { c ->
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            if (c.unidad.isBlank()) c.asignatura else "${c.asignatura} · ${c.unidad}",
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(c.codigoOa, style = MaterialTheme.typography.titleLarge)
                        Text(c.objetivo)
                        when (c.estado) {
                            Estado.PENDIENTE -> EtiquetaEstado("En revisión UTP", Ambar)
                            Estado.RECHAZADO -> EtiquetaEstado("Rechazado: ${c.observacionUtp}", Rojo)
                            Estado.APROBADO ->
                                if (c.asignado) EtiquetaEstado("Asignado · ${c.maxIntentos} intentos", Verde)
                                else EtiquetaEstado("Aprobado · sin asignar", Ambar)
                        }
                        if (c.informeValidado) EtiquetaEstado("Informe validado", Verde)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { onAsignar(c.id) }, enabled = c.estado == Estado.APROBADO) { Text("Asignar") }
                            OutlinedButton(onClick = { onInforme(c.id) }, enabled = c.estado == Estado.APROBADO) { Text("Informe") }
                            TextButton(onClick = {
                                if (c.estado == Estado.APROBADO) confirmarEdicion = c.id else onEditar(c.id)
                            }) { Text("Editar") }
                        }
                    }
                }
            }
        }
    }

    confirmarEdicion?.let { id ->
        AlertDialog(
            onDismissRequest = { confirmarEdicion = null },
            title = { Text("¿Editar contenido aprobado?") },
            text = { Text("Si guardas cambios, volverá a revisión UTP y dejará de verse para los estudiantes hasta que lo aprueben.") },
            confirmButton = { Button(onClick = { confirmarEdicion = null; onEditar(id) }) { Text("Editar") } },
            dismissButton = { TextButton(onClick = { confirmarEdicion = null }) { Text("Cancelar") } }
        )
    }
}