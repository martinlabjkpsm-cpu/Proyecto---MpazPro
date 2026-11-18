package cl.mpaz.mpazpro.grupo_2.model

import com.google.gson.annotations.SerializedName

/** Tipo de un Pokémon: `{ "name": "electric", "url": "..." }`. El nombre viene en inglés. */
data class Type(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("url")
    val url: String? = null
)
