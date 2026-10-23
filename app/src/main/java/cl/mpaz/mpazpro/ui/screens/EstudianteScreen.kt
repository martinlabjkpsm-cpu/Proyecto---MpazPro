package cl.mpaz.mpazpro.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.mpaz.mpazpro.MPazApp
import cl.mpaz.mpazpro.data.local.ContenidoEntity
import cl.mpaz.mpazpro.data.local.IntentoEntity
import cl.mpaz.mpazpro.di.AppViewModelFactory
import cl.mpaz.mpazpro.domain.model.Logros
import cl.mpaz.mpazpro.domain.model.TipoActividad
import cl.mpaz.mpazpro.ui.components.AvatarCamara
import cl.mpaz.mpazpro.ui.components.BarraSuperior
import cl.mpaz.mpazpro.ui.theme.Azul
import cl.mpaz.mpazpro.ui.theme.Verde
import cl.mpaz.mpazpro.ui.theme.Violeta
import cl.mpaz.mpazpro.ui.theme.estiloAsignatura
import cl.mpaz.mpazpro.ui.viewmodels.EstudianteViewModel

//vista estudiante: cabecera con foto y medallas, pestañas "Mis materias" y "Mis logros"

@Composable
fun EstudianteScreen(
    esTablet: Boolean,
    onAbrirActividad: (Int, TipoActividad) -> Unit,
    onSalir: () -> Unit,
    viewModel: EstudianteViewModel = viewModel(factory = AppViewModelFactory.Factory)
) {
    val contenidos by viewModel.contenidos.collectAsStateWithLifecycle()
    val intentos by viewModel.intentos.collectAsStateWithLifecycle()
    val foto by viewModel.foto.collectAsStateWithLifecycle()

    var confirmarSalida by rememberSaveable { mutableStateOf(false) }
    var pestana by rememberSaveable { mutableIntStateOf(0) }   // 0 = materias, 1 = logros

    Scaffold(topBar = { BarraSuperior("🚀 MPAZ PRO · 3.º Básico", onSalir = { confirmarSalida = true }) }) { innerPadding ->

        LazyVerticalGrid(
            columns = GridCells.Fixed(if (esTablet) 2 else 1),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Cabecera(viewModel.nombre, viewModel.medallas(intentos), foto, viewModel::guardarFoto)
            }
            item(span = { GridItemSpan(maxLineSpan) }) { TarjetaPermiso() }
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = pestana == 0, onClick = { pestana = 0 }, label = { Text("📚 Mis materias") })
                    FilterChip(selected = pestana == 1, onClick = { pestana = 1 }, label = { Text("🏆 Mis logros") })
                }
            }

            if (pestana == 0) {
                if (contenidos.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) { Text("Aún no tienes actividades asignadas.") }
                } else {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text("Elige un desafío para jugar y aprender:", style = MaterialTheme.typography.titleMedium)
                    }
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
            } else {
                val conAvance = contenidos.filter { c -> intentos.any { it.contenidoId == c.id } }
                if (conAvance.isEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text("Todavía no completas actividades. ¡Empieza con un desafío! 🚀")
                    }
                }
                items(conAvance, key = { "logro-${it.id}" }) { c -> TarjetaLogro(c, intentos) }
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
private fun Cabecera(nombre: String, medallas: Int, foto: Bitmap?, onFoto: (Bitmap) -> Unit) {
    val medallasAnimadas by animateIntAsState(medallas, label = "medallas")
    Card(shape = MaterialTheme.shapes.large) {
        Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Azul, Violeta)))) {
            Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("¡Hola, $nombre! 👋", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                    Text("3.º Básico · Escuela Marcela Paz", color = Color.White.copy(alpha = 0.85f))
                    Spacer(Modifier.size(4.dp))
                    Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.2f)) {
                        Text(
                            "🏅 $medallasAnimadas ${if (medallasAnimadas == 1) "medalla" else "medallas"}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
                AvatarCamara(foto = foto, onFoto = onFoto)
            }
        }
    }
}

@Composable
private fun TarjetaContenido(c: ContenidoEntity, desafioHecho: Boolean, usados: Int, puedePrueba: Boolean,
                             onAbrir: (Int, TipoActividad) -> Unit) {

    var abierta by remember { mutableStateOf(false) }
    val estilo = estiloAsignatura(c.asignatura)

    Card(
        onClick = { abierta = !abierta },
        colors = CardDefaults.cardColors(containerColor = estilo.color.copy(alpha = 0.10f)),
        border = BorderStroke(1.dp, estilo.color.copy(alpha = 0.35f))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = estilo.color, modifier = Modifier.size(44.dp)) {
                    Icon(estilo.icono, contentDescription = null, tint = Color.White, modifier = Modifier.padding(10.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("${estilo.emoji} ${c.asignatura}", color = estilo.color, style = MaterialTheme.typography.labelLarge)
                    Text(c.codigoOa, style = MaterialTheme.typography.titleLarge)
                }
            }
            Text(c.objetivo)
            Text(
                if (abierta) "Toca para ocultar la explicación" else "💡 Toca la tarjeta para ver la explicación",
                style = MaterialTheme.typography.bodyMedium,
                color = estilo.color
            )

            AnimatedVisibility(visible = abierta, enter = expandVertically(), exit = shrinkVertically()) {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Text(c.explicacion, Modifier.padding(12.dp))
                }
            }

            Text("Prueba final: $usados de ${c.maxIntentos} intentos usados", style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { onAbrir(c.id, TipoActividad.DESAFIO) },
                    border = BorderStroke(1.dp, estilo.color),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = estilo.color)
                ) { Text("🎯 Desafíos") }
                Button(
                    onClick = { onAbrir(c.id, TipoActividad.PRUEBA) },
                    enabled = puedePrueba,
                    colors = ButtonDefaults.buttonColors(containerColor = estilo.color)
                ) { Text("📝 Prueba final") }
            }
            if (desafioHecho) Text("✅ Desafíos completados", color = Verde, style = MaterialTheme.typography.bodyMedium)
            else Text("Primero completa los desafíos.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun TarjetaLogro(c: ContenidoEntity, intentos: List<IntentoEntity>) {
    val estilo = estiloAsignatura(c.asignatura)
    val desafio = Logros.mejorPorcentaje(c.id, TipoActividad.DESAFIO, intentos)
    val prueba = Logros.mejorPorcentaje(c.id, TipoActividad.PRUEBA, intentos)
    val estrellas = Logros.estrellas(prueba ?: desafio ?: 0)

    Card(colors = CardDefaults.cardColors(containerColor = estilo.color.copy(alpha = 0.10f))) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("${estilo.emoji} ${c.asignatura} · ${c.codigoOa}", color = estilo.color, style = MaterialTheme.typography.labelLarge)
            Text("⭐".repeat(estrellas) + "☆".repeat(3 - estrellas) + "  " + Logros.mensaje(estrellas),
                style = MaterialTheme.typography.titleMedium)
            Text("Desafíos: " + (desafio?.let { "mejor resultado $it %" } ?: "sin intentar"))
            Text("Prueba final: " + (prueba?.let { "mejor resultado $it %" } ?: "sin rendir"))
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
