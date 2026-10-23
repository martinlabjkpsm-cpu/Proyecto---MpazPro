package cl.mpaz.mpazpro.ui.components

import android.content.ActivityNotFoundException
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cl.mpaz.mpazpro.ui.theme.Azul
import cl.mpaz.mpazpro.ui.theme.Naranjo
import cl.mpaz.mpazpro.ui.theme.Verde

// Botón que lee en voz alta (TextToSpeech). Cambia de color mientras está leyendo.
@Composable
fun BotonAudio(hablando: Boolean, onClick: () -> Unit, color: Color = MaterialTheme.colorScheme.secondary) {
    val fondo by animateColorAsState(if (hablando) Verde else color, label = "colorAudio")
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = fondo),
        shape = RoundedCornerShape(20.dp)
    ) {
        Icon(if (hablando) Icons.Filled.Stop else Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(if (hablando) "Detener lectura" else "🔊 Escuchar pregunta")
    }
}

// Foto de perfil con la cámara nativa (TakePicturePreview). Al tocarla se abre la cámara.
@Composable
fun AvatarCamara(foto: Bitmap?, onFoto: (Bitmap) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) onFoto(bitmap)
    }
    val abrirCamara: () -> Unit = {
        try {
            lanzador.launch(null)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No se encontró una app de cámara", Toast.LENGTH_SHORT).show()
        }
    }
    val imagen = remember(foto) { foto?.asImageBitmap() }

    Box(
        contentAlignment = Alignment.BottomEnd,
        modifier = modifier.size(84.dp).clickable(onClickLabel = "Tomar foto de perfil", onClick = abrirCamara)
    ) {
        if (imagen != null) {
            Image(
                bitmap = imagen,
                contentDescription = "Foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape).border(3.dp, Color.White, CircleShape)
            )
        } else {
            Surface(Modifier.fillMaxSize(), shape = CircleShape, color = Color.White.copy(alpha = 0.9f)) {
                Icon(Icons.Filled.Face, contentDescription = "Sin foto", tint = Azul, modifier = Modifier.padding(16.dp))
            }
        }
        Surface(shape = CircleShape, color = Naranjo, modifier = Modifier.size(30.dp)) {
            Icon(Icons.Filled.CameraAlt, contentDescription = "Tomar foto", tint = Color.White, modifier = Modifier.padding(6.dp))
        }
    }
}
