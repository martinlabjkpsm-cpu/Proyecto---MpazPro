package cl.mpaz.mpazpro.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cl.mpaz.mpazpro.domain.model.ActividadEstado
import cl.mpaz.mpazpro.domain.model.Logros
import cl.mpaz.mpazpro.domain.model.RevisionPregunta
import cl.mpaz.mpazpro.domain.model.TipoActividad
import cl.mpaz.mpazpro.ui.components.BarraSuperior
import cl.mpaz.mpazpro.ui.components.BotonAudio
import cl.mpaz.mpazpro.ui.components.BotonPrincipal
import cl.mpaz.mpazpro.ui.components.MensajeError
import cl.mpaz.mpazpro.ui.theme.AmarilloPista
import cl.mpaz.mpazpro.ui.theme.Verde
import cl.mpaz.mpazpro.ui.viewmodels.EstudianteViewModel

//vista de desafios

@Composable
fun ActividadScreen(viewModel: EstudianteViewModel, contenidoId: Int, tipo: TipoActividad, onVolver: () -> Unit) {

    LaunchedEffect(contenidoId, tipo) { viewModel.iniciarActividad(contenidoId, tipo) }

    val estado by viewModel.actividad.collectAsStateWithLifecycle()
    val preguntas by viewModel.preguntas.collectAsStateWithLifecycle()
    var confirmarFin by rememberSaveable { mutableStateOf(false) }
    val hablando by viewModel.hablando.collectAsStateWithLifecycle()
    val pista by viewModel.pista.collectAsStateWithLifecycle()
    val cargando by viewModel.cargando.collectAsStateWithLifecycle()

    // Salir de la prueba sin terminar pide confirmación (se usará un intento)
    var confirmarSalida by rememberSaveable { mutableStateOf(false) }
    val enPrueba = tipo == TipoActividad.PRUEBA && !estado.terminada && preguntas.isNotEmpty()
    BackHandler(enabled = enPrueba) { confirmarSalida = true }
    val volver: () -> Unit = { if (enPrueba) confirmarSalida = true else onVolver() }

    // al salir de la pantalla se corta la lectura en voz alta
    DisposableEffect(Unit) { onDispose { viewModel.detenerVoz() } }

    val titulo = if (tipo == TipoActividad.DESAFIO) "Desafíos" else "Prueba final"
    Scaffold(topBar = { BarraSuperior(titulo, onVolver = volver) }) { innerPadding ->
        Column(
            Modifier.padding(innerPadding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val pregunta = preguntas.getOrNull(estado.indice)
            when {
                estado.terminada -> Resultado(estado, preguntas.size, onVolver)
                cargando -> CircularProgressIndicator()
                pregunta == null -> Text("Esta actividad todavía no tiene preguntas. Avísale a tu profesor(a).")
                else -> {

                    val progreso by animateFloatAsState(estado.indice.toFloat() / preguntas.size, label = "progreso")
                    LinearProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxWidth())
                    Text("Pregunta ${estado.indice + 1} de ${preguntas.size} · dificultad ${pregunta.dificultad}/3")
                    Text(pregunta.enunciado, style = MaterialTheme.typography.titleLarge)

                    // Lectura en voz alta (TextToSpeech): pregunta + alternativas
                    LaunchedEffect(estado.indice) { viewModel.detenerVoz() }
                    BotonAudio(hablando = hablando, onClick = {
                        val letras = listOf("A", "B", "C")
                        val alternativas = pregunta.opciones().mapIndexed { i, o -> "Opción ${letras[i]}: $o" }
                        viewModel.leerEnVozAlta(
                            pregunta.enunciado.replace("___", "espacio en blanco") + ". " + alternativas.joinToString(". ")
                        )
                    })


                    pregunta.opciones().forEachIndexed { i, texto ->
                        Opcion(
                            texto = texto,
                            elegida = estado.seleccion == i,
                            esCorrecta = estado.comprobada && i == pregunta.correcta,
                            esIncorrecta = estado.comprobada && i == estado.seleccion && i != pregunta.correcta,
                            onClick = { viewModel.seleccionar(i) }
                        )
                    }
                    MensajeError(estado.errorSeleccion)

                    // Pista con la explicación del objetivo (solo en desafíos, antes de comprobar)
                    if (tipo == TipoActividad.DESAFIO && pista.isNotBlank() && !estado.comprobada) {
                        var verPista by rememberSaveable(estado.indice) { mutableStateOf(false) }
                        TextButton(onClick = { verPista = !verPista }) {
                            Text(if (verPista) "Ocultar pista" else "💡 ¿Necesitas una pista?")
                        }
                        AnimatedVisibility(visible = verPista) {
                            Surface(color = AmarilloPista, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
                                Text("💡 $pista", Modifier.padding(12.dp))
                            }
                        }
                    }


                    AnimatedVisibility(visible = estado.comprobada) {
                        val bien = estado.seleccion == pregunta.correcta
                        Card { Text((if (bien) "¡Muy bien! " else "¡Casi! ") + pregunta.ayuda, Modifier.padding(12.dp)) }
                    }

                    val esUltima = estado.indice == preguntas.lastIndex
                    val terminaAhora = esUltima && (tipo == TipoActividad.PRUEBA || estado.comprobada)
                    BotonPrincipal(
                        texto = when {
                            terminaAhora -> "Terminar"
                            tipo == TipoActividad.DESAFIO && !estado.comprobada -> "Comprobar"
                            else -> "Siguiente"
                        },
                        onClick = {
                            when {
                                terminaAhora && estado.seleccion != null -> confirmarFin = true   // pide confirmación
                                tipo == TipoActividad.DESAFIO && estado.comprobada -> viewModel.siguiente()
                                else -> viewModel.comprobar()
                            }
                        }
                    )
                }
            }
        }
    }

    if (confirmarFin) {
        AlertDialog(
            onDismissRequest = { confirmarFin = false },
            title = { Text("¿Terminar la actividad?") },
            text = { Text("Se guardarán tus respuestas.") },
            confirmButton = {
                Button(onClick = {
                    confirmarFin = false
                    if (tipo == TipoActividad.PRUEBA) viewModel.comprobar() else viewModel.siguiente()
                }) { Text("Sí, terminar") }
            },
            dismissButton = { TextButton(onClick = { confirmarFin = false }) { Text("Revisar") } }
        )
    }

    if (confirmarSalida) {
        DialogoSalida(
            onSalir = { confirmarSalida = false; viewModel.abandonarPrueba(); onVolver() },
            onQuedarse = { confirmarSalida = false }
        )
    }
}

@Composable
private fun DialogoSalida(onSalir: () -> Unit, onQuedarse: () -> Unit) {
    AlertDialog(
        onDismissRequest = onQuedarse,
        title = { Text("¿Salir de la prueba?") },
        text = { Text("Si sales ahora se usará 1 intento y las preguntas sin responder contarán como incorrectas.") },
        confirmButton = { Button(onClick = onSalir) { Text("Salir") } },
        dismissButton = { TextButton(onClick = onQuedarse) { Text("Seguir respondiendo") } }
    )
}

@Composable
private fun Opcion(texto: String, elegida: Boolean, esCorrecta: Boolean, esIncorrecta: Boolean, onClick: () -> Unit) {

    val color by animateColorAsState(
        when {
            esCorrecta -> Verde
            esIncorrecta -> MaterialTheme.colorScheme.error
            elegida -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline
        },
        label = "colorOpcion"
    )
    val borde by animateDpAsState(if (elegida || esCorrecta) 3.dp else 1.dp, label = "bordeOpcion")

    Surface(
        border = BorderStroke(borde, color),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth().selectable(selected = elegida, onClick = onClick)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = elegida, onClick = onClick)
            Text(texto, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun Resultado(estado: ActividadEstado, total: Int, onVolver: () -> Unit) {
    val porcentaje = Logros.porcentaje(estado.correctas, total)
    val estrellas = Logros.estrellas(porcentaje)

    // Animación de celebración: las estrellas "rebotan" al aparecer
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val escala by animateFloatAsState(
        targetValue = if (visible) 1f else 0.3f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "escalaEstrellas"
    )
    Text(
        "⭐".repeat(estrellas) + "☆".repeat(3 - estrellas),
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier.scale(escala)
    )
    Text("$porcentaje% · ${Logros.mensaje(estrellas)}", style = MaterialTheme.typography.headlineSmall)
    Text("${estado.correctas} de $total respuestas correctas. Tu avance quedó guardado.")
    estado.intentosRestantes?.let { Text("Te quedan $it intento(s) de la prueba final.") }

    if (estado.revision.isNotEmpty()) {
        Text("Revisa tus respuestas", style = MaterialTheme.typography.titleMedium)
        estado.revision.forEachIndexed { i, r -> TarjetaRevision(i + 1, r, estado.mostrarCorrectas) }
        if (!estado.mostrarCorrectas) {
            Text(
                "Las respuestas correctas se mostrarán cuando apruebes o cuando uses todos tus intentos.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
    BotonPrincipal("Volver", onVolver)
}

// Tarjeta de revisión: verde si acertó, roja si se equivocó
@Composable
private fun TarjetaRevision(numero: Int, r: RevisionPregunta, mostrarCorrecta: Boolean) {
    val letras = listOf("A", "B", "C")
    val color = if (r.esCorrecta) Verde else MaterialTheme.colorScheme.error
    Card(border = BorderStroke(2.dp, color), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (r.esCorrecta) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                    contentDescription = if (r.esCorrecta) "Respuesta correcta" else "Respuesta incorrecta",
                    tint = color
                )
                Spacer(Modifier.width(8.dp))
                Text("$numero. ${r.enunciado}", style = MaterialTheme.typography.titleSmall)
            }
            if (r.elegida in r.opciones.indices) {
                Text("Tu respuesta: ${letras[r.elegida]}) ${r.opciones[r.elegida]}")
            } else {
                Text("Sin responder")
            }
            if (!r.esCorrecta && mostrarCorrecta) {
                Text("Respuesta correcta: ${letras[r.correcta]}) ${r.opciones[r.correcta]}", color = Verde)
            }
            if (mostrarCorrecta) Text("💡 ${r.ayuda}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}