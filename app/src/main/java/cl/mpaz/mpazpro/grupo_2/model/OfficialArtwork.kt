package cl.mpaz.mpazpro.grupo_2.model

import com.google.gson.annotations.SerializedName

/** Objeto `sprites.other.official-artwork`: ilustración oficial del Pokémon. */
data class OfficialArtwork(
    @SerializedName("front_default")
    val frontDefault: String? = null
)
