package cl.mpaz.mpazpro.grupo_2.data.remote

import cl.mpaz.mpazpro.grupo_2.model.Pokemon
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Contrato de PokéAPI (API externa) para Retrofit.
 * Retrofit genera la implementación: cada método anotado se convierte en una petición HTTP.
 */
interface ApiService {

    /**
     * `GET https://pokeapi.co/api/v2/pokemon/{idOrName}`
     * `Response<Pokemon>` da acceso al código HTTP: un 404 no lanza excepción, se revisa `isSuccessful`.
     */
    @GET("pokemon/{idOrName}")
    suspend fun getPokemon(
        @Path("idOrName") idOrName: String
    ): Response<Pokemon>
}
