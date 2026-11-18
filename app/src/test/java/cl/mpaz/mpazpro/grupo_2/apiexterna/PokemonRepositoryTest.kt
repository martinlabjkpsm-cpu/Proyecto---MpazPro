package cl.mpaz.mpazpro.grupo_2.apiexterna

import cl.mpaz.mpazpro.grupo_2.data.repository.DefaultPokemonRepository
import cl.mpaz.mpazpro.grupo_2.data.repository.PokemonException
import com.google.gson.JsonSyntaxException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Response
import java.io.IOException

// Repositorio de la API externa PokéAPI con un ApiService falso (no usa internet).
// Herramientas: Kotest + kotlinx-coroutines-test.
class PokemonRepositoryTest : StringSpec({

    "devuelve el Pokémon si la respuesta es correcta" {
        runTest {
            val api = FakeApiService { Response.success(PokemonDatos.pikachu) }
            DefaultPokemonRepository(api).getPokemon("25") shouldBe PokemonDatos.pikachu
            api.requestedIds shouldContainExactly listOf("25")
        }
    }

    "lanza NotFound si PokéAPI responde 404" {
        runTest {
            val repo = DefaultPokemonRepository(FakeApiService { Response.error(404, "".toResponseBody()) })
            shouldThrow<PokemonException.NotFound> { repo.getPokemon("9999") }
        }
    }

    "lanza Server con el código si PokéAPI responde 500" {
        runTest {
            val repo = DefaultPokemonRepository(FakeApiService { Response.error(500, "".toResponseBody()) })
            shouldThrow<PokemonException.Server> { repo.getPokemon("25") }.code shouldBe 500
        }
    }

    "lanza Server si la respuesta correcta llega sin cuerpo" {
        runTest {
            val repo = DefaultPokemonRepository(FakeApiService { Response.success(null) })
            shouldThrow<PokemonException.Server> { repo.getPokemon("25") }.code shouldBe 200
        }
    }

    "sin internet lanza Network con la causa original" {
        runTest {
            val repo = DefaultPokemonRepository(FakeApiService { throw IOException("sin red") })
            shouldThrow<PokemonException.Network> { repo.getPokemon("25") }.cause.shouldBeInstanceOf<IOException>()
        }
    }

    "un JSON ilegible se traduce a Unknown" {
        runTest {
            val repo = DefaultPokemonRepository(FakeApiService { throw JsonSyntaxException("json roto") })
            shouldThrow<PokemonException.Unknown> { repo.getPokemon("25") }
        }
    }
})
