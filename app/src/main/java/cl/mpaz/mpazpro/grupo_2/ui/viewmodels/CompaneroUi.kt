package cl.mpaz.mpazpro.grupo_2.ui.viewmodels

import cl.mpaz.mpazpro.grupo_2.model.Pokemon

/**
 * Compañero Pokémon listo para mostrar: la UI lo pinta tal cual, sin revisar nulls.
 * Lo crea el ViewModel desde el modelo de red con `toUi()`, así la UI no depende de Gson.
 */
data class CompaneroUi(
    val titulo: String,          // "#25 - Pikachu"
    val imageUrl: String?,
    val tipos: List<String>,     // en español: "Eléctrico"
    val medidas: String          // "Mide 40 cm y pesa 6 kg" (unidades de medida, Matemática 3.º Básico)
)

private const val SIN_DATO = "—"

internal fun Pokemon.toUi(): CompaneroUi = CompaneroUi(
    titulo = formatTitle(id, name),
    imageUrl = imageUrl,
    tipos = types.orEmpty().sortedBy { it.slot ?: 0 }.map { tipoEnEspanol(it.type?.name ?: "normal") },
    medidas = formatMedidas(height, weight)
)

/** `mr-mime` → "Mr Mime". */
internal fun formatName(name: String): String =
    name.split('-').filter { it.isNotEmpty() }.joinToString(" ") { w -> w.replaceFirstChar { it.uppercase() } }

internal fun formatTitle(id: Int?, name: String?): String {
    val nombre = name?.let(::formatName)
    return when {
        id != null && nombre != null -> "#$id - $nombre"
        id != null -> "#$id - Pokémon desconocido"
        nombre != null -> nombre
        else -> "Pokémon desconocido"
    }
}

/** Altura en decímetros → cm. Peso en hectogramos → kg con coma decimal. */
internal fun formatMedidas(alturaDm: Int?, pesoHg: Int?): String {
    val alto = alturaDm?.let { "${it * 10} cm" } ?: SIN_DATO
    val peso = pesoHg?.let { if (it % 10 == 0) "${it / 10} kg" else "${it / 10},${it % 10} kg" } ?: SIN_DATO
    return "Mide $alto y pesa $peso"
}

/** PokéAPI entrega los tipos en inglés; para 3.º Básico se muestran en español. */
internal fun tipoEnEspanol(tipo: String): String = when (tipo.lowercase()) {
    "normal" -> "Normal"; "fire" -> "Fuego"; "water" -> "Agua"; "grass" -> "Planta"
    "electric" -> "Eléctrico"; "ice" -> "Hielo"; "fighting" -> "Lucha"; "poison" -> "Veneno"
    "ground" -> "Tierra"; "flying" -> "Volador"; "psychic" -> "Psíquico"; "bug" -> "Bicho"
    "rock" -> "Roca"; "ghost" -> "Fantasma"; "dragon" -> "Dragón"; "dark" -> "Siniestro"
    "steel" -> "Acero"; "fairy" -> "Hada"
    else -> tipo.replaceFirstChar { it.uppercase() }
}
