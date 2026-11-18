package cl.mpaz.mpazpro.grupo_2.apiexterna

import cl.mpaz.mpazpro.grupo_2.data.remote.ApiService
import cl.mpaz.mpazpro.grupo_2.model.Pokemon
import retrofit2.Response

/**
 * `ApiService` falso: no usa la red. Cada test decide qué responde con `handler`
 * (una respuesta, un error HTTP o una excepción) y revisa lo pedido en `requestedIds`.
 */
class FakeApiService(
    var handler: suspend (idOrName: String) -> Response<Pokemon>
) : ApiService {
    val requestedIds = mutableListOf<String>()

    override suspend fun getPokemon(idOrName: String): Response<Pokemon> {
        requestedIds += idOrName
        return handler(idOrName)
    }
}
