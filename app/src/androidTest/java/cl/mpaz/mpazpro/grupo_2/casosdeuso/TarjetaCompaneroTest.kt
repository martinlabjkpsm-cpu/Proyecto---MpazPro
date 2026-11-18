package cl.mpaz.mpazpro.grupo_2.casosdeuso

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.mpaz.mpazpro.grupo_2.data.repository.PokemonException
import cl.mpaz.mpazpro.grupo_2.data.repository.PokemonRepository
import cl.mpaz.mpazpro.grupo_2.model.Pokemon
import cl.mpaz.mpazpro.grupo_2.ui.components.TagsPrueba
import cl.mpaz.mpazpro.grupo_2.ui.components.TarjetaCompanero
import cl.mpaz.mpazpro.grupo_2.ui.viewmodels.CompaneroViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

// Prueba de UI de la tarjeta "Mis compañeros Pokémon" (API externa), sin internet:
// el repositorio es falso, como en el ejemplo my-pokemon del docente.
// Herramientas: JUnit + Compose UI Test (ComposeTestRule + testTag).

@RunWith(AndroidJUnit4::class)
class TarjetaCompaneroTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    // Repositorio falso: cada test decide qué responde
    private class RepositorioFalso(var respuesta: suspend (String) -> Pokemon) : PokemonRepository {
        override suspend fun getPokemon(idOrName: String): Pokemon = respuesta(idOrName)
    }

    private val pikachu = Pokemon(id = 25, name = "pikachu", height = 4, weight = 60)

    @Test
    fun muestra_el_compañero_con_sus_medidas() {
        val vm = CompaneroViewModel(RepositorioFalso { pikachu })
        composeRule.setContent { TarjetaCompanero(medallas = 0, viewModel = vm) }

        composeRule.waitUntil(5_000) { composeRule.onAllNodes(hasText("#25 - Pikachu")).fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNodeWithTag(TagsPrueba.COMPANERO_NOMBRE).assertIsDisplayed()
        composeRule.onNodeWithText("📏 Mide 40 cm y pesa 6 kg").assertIsDisplayed()
    }

    @Test
    fun sin_internet_muestra_el_error_y_reintentar_lo_carga() {
        val repo = RepositorioFalso { throw PokemonException.Network(IOException("sin red")) }
        val vm = CompaneroViewModel(repo)
        composeRule.setContent { TarjetaCompanero(medallas = 0, viewModel = vm) }

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodes(hasText(CompaneroViewModel.MENSAJE_SIN_CONEXION)).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(TagsPrueba.COMPANERO_ERROR).assertIsDisplayed()

        repo.respuesta = { pikachu }
        composeRule.onNodeWithText("Reintentar").performClick()
        composeRule.waitUntil(5_000) { composeRule.onAllNodes(hasText("#25 - Pikachu")).fetchSemanticsNodes().isNotEmpty() }
    }
}
