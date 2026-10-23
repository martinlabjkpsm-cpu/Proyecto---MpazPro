package cl.mpaz.mpazpro.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

// Color, ícono y emoji de cada asignatura para la vista del estudiante.

data class EstiloAsignatura(val color: Color, val icono: ImageVector, val emoji: String)

fun estiloAsignatura(asignatura: String): EstiloAsignatura = when {
    asignatura.startsWith("Lenguaje") -> EstiloAsignatura(ColorLenguaje, Icons.AutoMirrored.Filled.MenuBook, "📖")
    asignatura.startsWith("Matem") -> EstiloAsignatura(ColorMatematica, Icons.Filled.Calculate, "🧮")
    asignatura.startsWith("Ciencias") -> EstiloAsignatura(ColorCiencias, Icons.Filled.Science, "🔬")
    asignatura.startsWith("Historia") -> EstiloAsignatura(ColorHistoria, Icons.Filled.Public, "🌎")
    else -> EstiloAsignatura(Azul, Icons.Filled.School, "⭐")
}
