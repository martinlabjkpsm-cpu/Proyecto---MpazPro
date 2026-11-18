package cl.mpaz.mpazpro.grupo_2.data.repository

import cl.mpaz.mpazpro.grupo_2.model.Pokemon

/**
 * Fuente de datos de Pokémon para el ViewModel. Es una interfaz: en la app se usa
 * `DefaultPokemonRepository` (PokéAPI); en los tests, un repositorio falso.
 */
interface PokemonRepository {

    /** Devuelve el Pokémon pedido. Si falla, lanza una `PokemonException`. */
    suspend fun getPokemon(idOrName: String): Pokemon
}
