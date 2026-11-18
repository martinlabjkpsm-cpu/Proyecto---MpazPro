package cl.mpaz.mpazpro.grupo_2.apiexterna

import cl.mpaz.mpazpro.grupo_2.model.OfficialArtwork
import cl.mpaz.mpazpro.grupo_2.model.OtherSprites
import cl.mpaz.mpazpro.grupo_2.model.Pokemon
import cl.mpaz.mpazpro.grupo_2.model.Sprites
import cl.mpaz.mpazpro.grupo_2.model.Type
import cl.mpaz.mpazpro.grupo_2.model.Types

// Pokémon de ejemplo con la misma forma que la respuesta de PokéAPI.
object PokemonDatos {
    val pikachu = Pokemon(
        id = 25, name = "pikachu", height = 4, weight = 60,
        sprites = Sprites("https://img/25.png", OtherSprites(OfficialArtwork("https://img/arte/25.png"))),
        types = listOf(Types(1, Type("electric")))
    )
    val eevee = Pokemon(id = 133, name = "eevee", height = 3, weight = 65, types = listOf(Types(1, Type("normal"))))
}
