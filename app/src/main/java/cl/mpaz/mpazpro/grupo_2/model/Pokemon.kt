package cl.mpaz.mpazpro.grupo_2.model

import com.google.gson.annotations.SerializedName

/**
 * Respuesta de `GET pokemon/{idOrName}` de PokéAPI. Solo se declaran los campos que usa
 * la app: Gson ignora el resto del JSON.
 *
 * `@SerializedName` indica el nombre de la clave en el JSON. Todos los campos tienen valor
 * por defecto y son nullable, porque Gson no respeta la nulabilidad de Kotlin.
 */
data class Pokemon(
    @SerializedName("id")
    val id: Int? = null,
    @SerializedName("name")
    val name: String? = null,
    // En decímetros. `toUi()` lo pasa a centímetros.
    @SerializedName("height")
    val height: Int? = null,
    // En hectogramos. `toUi()` lo pasa a kilogramos.
    @SerializedName("weight")
    val weight: Int? = null,
    @SerializedName("sprites")
    val sprites: Sprites? = Sprites(),
    @SerializedName("types")
    val types: List<Types>? = null
) {
    /** Ilustración oficial si existe y, si no, el sprite básico. Gson ignora esta propiedad. */
    val imageUrl: String?
        get() = sprites?.other?.officialArtwork?.frontDefault ?: sprites?.frontDefault
}
