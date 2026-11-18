package cl.mpaz.mpazpro.grupo_2.model

import com.google.gson.annotations.SerializedName

/** Objeto `sprites` del JSON: URLs de las imágenes del Pokémon. */
data class Sprites(
    @SerializedName("front_default")
    val frontDefault: String? = null,
    @SerializedName("other")
    val other: OtherSprites? = null
)
