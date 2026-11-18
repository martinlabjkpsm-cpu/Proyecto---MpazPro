package cl.mpaz.mpazpro.grupo_2.model

import com.google.gson.annotations.SerializedName

/** Elemento de la lista `types`: `{ "slot": 1, "type": { ... } }` (1 = tipo principal). */
data class Types(
    @SerializedName("slot")
    val slot: Int? = null,
    @SerializedName("type")
    val type: Type? = Type()
)
