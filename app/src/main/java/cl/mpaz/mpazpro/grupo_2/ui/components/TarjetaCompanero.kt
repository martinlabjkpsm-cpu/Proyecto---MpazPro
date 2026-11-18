package cl.mpaz.mpazpro.grupo_2.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.mpaz.mpazpro.grupo_2.di.AppViewModelFactory
import cl.mpaz.mpazpro.grupo_2.domain.model.Companeros
import cl.mpaz.mpazpro.grupo_2.ui.viewmodels.CompaneroUi
import cl.mpaz.mpazpro.grupo_2.ui.viewmodels.CompaneroUiState
import cl.mpaz.mpazpro.grupo_2.ui.viewmodels.CompaneroViewModel
import coil3.compose.SubcomposeAsyncImage

// Tarjeta "Mis compañeros Pokémon": premio por las medallas, con datos de la API externa PokéAPI.

@Composable
fun TarjetaCompanero(
    medallas: Int,
    viewModel: CompaneroViewModel = viewModel(factory = AppViewModelFactory.Factory)
) {
    LaunchedEffect(medallas) { viewModel.actualizar(medallas) }

    val desbloqueados by viewModel.desbloqueados.collectAsStateWithLifecycle()
    val seleccionado by viewModel.seleccionado.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Card(Modifier.fillMaxWidth().testTag(TagsPrueba.COMPANERO_TARJETA)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("🐾 Mis compañeros Pokémon", style = MaterialTheme.typography.titleMedium)
            val faltan = Companeros.faltan(medallas)
            Text(
                if (faltan == 0) "¡Desbloqueaste a todos tus compañeros!"
                else "Gana medallas en las pruebas finales para desbloquear más ($faltan por descubrir).",
                style = MaterialTheme.typography.bodyMedium
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(desbloqueados) { id ->
                    FilterChip(selected = id == seleccionado, onClick = { viewModel.elegir(id) },
                        label = { Text("Nº $id") })
                }
            }

            when (val state = uiState) {
                CompaneroUiState.Loading -> Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.padding(16.dp))
                }
                is CompaneroUiState.Error -> Column {
                    Text(state.message, color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag(TagsPrueba.COMPANERO_ERROR))
                    TextButton(onClick = viewModel::reintentar) { Text("Reintentar") }
                }
                is CompaneroUiState.Success -> DetalleCompanero(state.companero)
            }
        }
    }
}

@Composable
private fun DetalleCompanero(c: CompaneroUi) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        // Coil descarga y muestra la imagen; mientras carga, un spinner; si falla, una huella.
        SubcomposeAsyncImage(
            model = c.imageUrl,
            contentDescription = c.titulo,
            modifier = Modifier.size(110.dp),
            loading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } },
            error = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("🐾", style = MaterialTheme.typography.headlineLarge) } }
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(c.titulo, style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.testTag(TagsPrueba.COMPANERO_NOMBRE))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                c.tipos.forEach { AssistChip(onClick = {}, label = { Text(it) }) }
            }
            Text("📏 ${c.medidas}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
