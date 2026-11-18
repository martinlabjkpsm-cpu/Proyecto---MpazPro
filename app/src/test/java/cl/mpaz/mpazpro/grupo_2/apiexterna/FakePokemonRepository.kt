package cl.mpaz.mpazpro.grupo_2.apiexterna

import cl.mpaz.mpazpro.grupo_2.data.repository.PokemonRepository
import cl.mpaz.mpazpro.grupo_2.model.Pokemon

/** `PokemonRepository` falso para los tests del ViewModel: no usa red ni Retrofit. */
class FakePokemonRepository(
    var handler: suspend (idOrName: String) -> Pokemon
) : PokemonRepository {
    val requestedIds = mutableListOf<String>()

    override suspend fun getPokemon(idOrName: String): Pokemon {
        requestedIds += idOrName
        return handler(idOrName)
    }
}
