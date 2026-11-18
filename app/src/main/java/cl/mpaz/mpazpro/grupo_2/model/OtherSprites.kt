package cl.mpaz.mpazpro.grupo_2.model

import com.google.gson.annotations.SerializedName

/** Objeto `sprites.other`. La clave `official-artwork` lleva guion: `@SerializedName` lo resuelve. */
data class OtherSprites(
    @SerializedName("official-artwork")
    val officialArtwork: OfficialArtwork? = null
)
