package cl.mpaz.mpazpro.grupo_2.data.repository

import cl.mpaz.mpazpro.grupo_2.data.remote.ApiService
import cl.mpaz.mpazpro.grupo_2.model.Pokemon
import kotlinx.coroutines.CancellationException
import java.io.IOException
import java.net.HttpURLConnection.HTTP_NOT_FOUND

/**
 * Implementación de `PokemonRepository` que pide los datos a PokéAPI.
 * Recibe `ApiService` por constructor: en la app, el de `RetrofitInstance`; en un test, uno falso.
 */
class DefaultPokemonRepository(
    private val api: ApiService
) : PokemonRepository {

    override suspend fun getPokemon(idOrName: String): Pokemon {
        val response = try {
            api.getPokemon(idOrName)
        } catch (e: CancellationException) {
            throw e                                   // la cancelación de la corrutina se propaga
        } catch (e: IOException) {
            throw PokemonException.Network(e)         // sin conexión, timeout o DNS
        } catch (e: Exception) {
            throw PokemonException.Unknown(e)         // por ejemplo, JSON ilegible
        }

        if (response.code() == HTTP_NOT_FOUND) throw PokemonException.NotFound()

        return response.body()?.takeIf { response.isSuccessful }
            ?: throw PokemonException.Server(response.code())
    }
}
