package cl.mpaz.mpazpro.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.mpaz.mpazpro.ui.components.BarraSuperior
import cl.mpaz.mpazpro.ui.components.EtiquetaEstado
import cl.mpaz.mpazpro.ui.theme.Ambar
import cl.mpaz.mpazpro.ui.theme.Verde
import cl.mpaz.mpazpro.di.AppViewModelFactory
import cl.mpaz.mpazpro.ui.viewmodels.DocenteViewModel

//vista docente

@Composable
fun DocenteScreen(
    esTablet: Boolean,
    onAsignar: (Int) -> Unit,
    onInforme: (Int) -> Unit,
    onSalir: () -> Unit,
    viewModel: DocenteViewModel = viewModel(factory = AppViewModelFactory.Factory)
) {
    val contenidos by viewModel.contenidos.collectAsStateWithLifecycle()

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
                        Text(c.asignatura, color = MaterialTheme.colorScheme.primary)
                        Text(c.codigoOa, style = MaterialTheme.typography.titleLarge)
                        Text(c.objetivo)
                        if (c.asignado) EtiquetaEstado("Asignado · ${c.maxIntentos} intentos", Verde)
                        else EtiquetaEstado("Sin asignar", Ambar)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { onAsignar(c.id) }) { Text("Asignar") }
                            OutlinedButton(onClick = { onInforme(c.id) }) { Text("Informe") }
                        }
                    }
                }
            }
        }
    }
}