package cl.mpaz.mpazpro.grupo_2.apiexterna

import cl.mpaz.mpazpro.grupo_2.data.repository.PokemonException
import cl.mpaz.mpazpro.grupo_2.ui.viewmodels.CompaneroUiState
import cl.mpaz.mpazpro.grupo_2.ui.viewmodels.CompaneroViewModel
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import java.io.IOException

// ViewModel de "Mis compañeros Pokémon" con un repositorio falso.
// Herramientas: Kotest + kotlinx-coroutines-test (advanceUntilIdle, sin Thread.sleep).

@OptIn(ExperimentalCoroutinesApi::class)
private fun conMain(cuerpo: suspend TestScope.() -> Unit) {
    Dispatchers.setMain(StandardTestDispatcher())
    try { runTest { cuerpo() } } finally { Dispatchers.resetMain() }
}

private fun repoNormal() = FakePokemonRepository { id ->
    if (id == "133") PokemonDatos.eevee else PokemonDatos.pikachu
}

@OptIn(ExperimentalCoroutinesApi::class)
class CompaneroViewModelTest : StringSpec({

    "sin medallas muestra a Pikachu como primer compañero" {
        conMain {
            val vm = CompaneroViewModel(repoNormal())
            vm.actualizar(0)
            vm.uiState.value shouldBe CompaneroUiState.Loading
            advanceUntilIdle()

            vm.desbloqueados.value shouldContainExactly listOf(25)
            (vm.uiState.value as CompaneroUiState.Success).companero.titulo shouldBe "#25 - Pikachu"
        }
    }

    "al ganar una medalla nueva muestra el compañero recién desbloqueado" {
        conMain {
            val vm = CompaneroViewModel(repoNormal())
            vm.actualizar(0); advanceUntilIdle()
            vm.actualizar(1); advanceUntilIdle()

            vm.seleccionado.value shouldBe 133
            (vm.uiState.value as CompaneroUiState.Success).companero.titulo shouldBe "#133 - Eevee"
        }
    }

    "si las medallas no cambian no vuelve a llamar a PokéAPI" {
        conMain {
            val repo = repoNormal()
            val vm = CompaneroViewModel(repo)
            vm.actualizar(0); advanceUntilIdle()
            vm.actualizar(0); advanceUntilIdle()

            repo.requestedIds shouldContainExactly listOf("25")
        }
    }

    "sin internet muestra un mensaje y Reintentar vuelve a cargar" {
        conMain {
            val repo = FakePokemonRepository { throw PokemonException.Network(IOException("sin red")) }
            val vm = CompaneroViewModel(repo)
            vm.actualizar(0); advanceUntilIdle()
            (vm.uiState.value as CompaneroUiState.Error).message shouldBe CompaneroViewModel.MENSAJE_SIN_CONEXION

            repo.handler = { PokemonDatos.pikachu }
            vm.reintentar(); advanceUntilIdle()
            vm.uiState.value.shouldBeInstanceOf<CompaneroUiState.Success>()
        }
    }

    "si PokéAPI falla muestra el código del error" {
        conMain {
            val vm = CompaneroViewModel(FakePokemonRepository { throw PokemonException.Server(500) })
            vm.actualizar(0); advanceUntilIdle()
            (vm.uiState.value as CompaneroUiState.Error).message shouldBe
                "PokéAPI no respondió bien (error 500). Inténtalo más tarde."
        }
    }

    "si elige otro compañero mientras carga, gana el último elegido" {
        conMain {
            val vm = CompaneroViewModel(repoNormal())
            vm.actualizar(1)          // empieza a cargar a Eevee
            vm.elegir(25)             // y antes de terminar elige a Pikachu
            advanceUntilIdle()

            (vm.uiState.value as CompaneroUiState.Success).companero.titulo shouldBe "#25 - Pikachu"
        }
    }
})
