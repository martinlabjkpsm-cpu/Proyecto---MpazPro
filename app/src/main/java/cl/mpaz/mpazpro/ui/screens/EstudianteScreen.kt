package cl.mpaz.mpazpro.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.mpaz.mpazpro.MPazApp
import cl.mpaz.mpazpro.data.local.ContenidoEntity
import cl.mpaz.mpazpro.domain.model.TipoActividad
import cl.mpaz.mpazpro.ui.components.BarraSuperior
import cl.mpaz.mpazpro.di.AppViewModelFactory
import cl.mpaz.mpazpro.ui.viewmodels.EstudianteViewModel

//vista estudiante

@Composable
fun EstudianteScreen(
    esTablet: Boolean,
    onAbrirActividad: (Int, TipoActividad) -> Unit,
    onSalir: () -> Unit,
    viewModel: EstudianteViewModel = viewModel(factory = AppViewModelFactory.Factory)
) {
    val contenidos by viewModel.contenidos.collectAsStateWithLifecycle()
    val intentos by viewModel.intentos.collectAsStateWithLifecycle()

    var confirmarSalida by rememberSaveable { mutableStateOf(false) }

    Scaffold(topBar = { BarraSuperior("Hola, ${viewModel.nombre}", onSalir = { confirmarSalida = true }) }) { innerPadding ->

        LazyVerticalGrid(
            columns = GridCells.Fixed(if (esTablet) 2 else 1),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) { TarjetaPermiso() }
            if (contenidos.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) { Text("Aún no tienes actividades asignadas.") }
            }
            items(contenidos, key = { it.id }) { c ->
                TarjetaContenido(
                    c = c,
                    desafioHecho = viewModel.desafioHecho(c.id, intentos),
                    usados = viewModel.intentosUsados(c.id, intentos),
                    puedePrueba = viewModel.puedeRendirPrueba(c, intentos),
                    onAbrir = onAbrirActividad
                )
            }
        }
    }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Tu avance ya está guardado.") },
            confirmButton = { Button(onClick = { viewModel.cerrarSesion(); onSalir() }) { Text("Salir") } },
            dismissButton = { TextButton(onClick = { confirmarSalida = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun TarjetaContenido(c: ContenidoEntity, desafioHecho: Boolean, usados: Int, puedePrueba: Boolean,
                             onAbrir: (Int, TipoActividad) -> Unit) {

    var abierta by remember { mutableStateOf(false) }

    Card(onClick = { abierta = !abierta }) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(c.asignatura, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            Text(c.codigoOa, style = MaterialTheme.typography.titleLarge)
            Text(c.objetivo)
            Text("Prueba final: $usados de ${c.maxIntentos} intentos usados", style = MaterialTheme.typography.bodyMedium)


            AnimatedVisibility(visible = abierta) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Text(c.explicacion, Modifier.padding(12.dp))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { onAbrir(c.id, TipoActividad.DESAFIO) }) { Text("Desafíos") }
                Button(onClick = { onAbrir(c.id, TipoActividad.PRUEBA) }, enabled = puedePrueba) { Text("Prueba final") }
            }
            if (!desafioHecho) Text("Primero completa los desafíos.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun TarjetaPermiso() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    val app = LocalContext.current.applicationContext as MPazApp
    var concedido by rememberSaveable { mutableStateOf(app.notificaciones.tienePermiso()) }
    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { aceptado ->
        concedido = aceptado
    }
    if (!concedido) {
        Card {
            Column(Modifier.padding(16.dp)) {
                Text("¿Te avisamos cuando termines una actividad?", style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { lanzador.launch(Manifest.permission.POST_NOTIFICATIONS) }) { Text("Permitir avisos") }
            }
        }
    }
}