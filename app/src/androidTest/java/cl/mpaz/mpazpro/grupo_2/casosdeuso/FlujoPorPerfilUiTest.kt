package cl.mpaz.mpazpro.grupo_2.casosdeuso

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.mpaz.mpazpro.grupo_2.MainActivity
import cl.mpaz.mpazpro.grupo_2.ui.components.TagsPrueba
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Prueba de interfaz de la app completa (MainActivity) en emulador o teléfono.
// CU01: cada perfil entra a su pantalla.
// Restricción del caso (3.1 y 5.1): el estudiante no ve el informe ni puede editar ni aprobar contenidos.
// Herramientas: JUnit + Compose UI Test (ComposeTestRule) buscando los componentes por testTag.
// Ejecutar con: ./gradlew connectedAndroidTest

@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class FlujoPorPerfilUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private fun ingresar(usuario: String, pin: String) {
        composeRule.onNodeWithTag(TagsPrueba.LOGIN_USUARIO).performTextClearance()
        composeRule.onNodeWithTag(TagsPrueba.LOGIN_USUARIO).performTextInput(usuario)
        composeRule.onNodeWithTag(TagsPrueba.LOGIN_PIN).performTextInput(pin)
        composeRule.onNodeWithTag(TagsPrueba.LOGIN_ENTRAR).performClick()
    }

    private fun cuantos(tag: String) = composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().size

    @Test
    fun pin_incorrecto_muestra_error_en_pantalla() {
        ingresar("est01", "9999")
        composeRule.waitUntilAtLeastOneExists(hasTestTag(TagsPrueba.LOGIN_ERROR), 5_000)
        composeRule.onNodeWithTag(TagsPrueba.LOGIN_ERROR).assertIsDisplayed()
    }

    @Test
    fun estudiante_entra_a_sus_materias_y_no_ve_informe_ni_edicion() {
        ingresar("est01", "1111")
        composeRule.waitUntilAtLeastOneExists(hasTestTag(TagsPrueba.ESTUDIANTE_SALUDO), 5_000)
        composeRule.onNodeWithTag(TagsPrueba.ESTUDIANTE_MATERIAS).assertIsDisplayed()
        assertEquals(0, cuantos(TagsPrueba.DOCENTE_INFORME))
        assertEquals(0, cuantos(TagsPrueba.DOCENTE_EDITAR))
        assertEquals(0, cuantos(TagsPrueba.UTP_REVISAR))
    }

    @Test
    fun docente_entra_y_ve_asignar_informe_y_nuevo_contenido() {
        ingresar("docente01", "1234")
        composeRule.waitUntilAtLeastOneExists(hasTestTag(TagsPrueba.DOCENTE_NUEVO), 5_000)
        composeRule.onAllNodesWithTag(TagsPrueba.DOCENTE_INFORME)[0].assertIsDisplayed()
        composeRule.onAllNodesWithTag(TagsPrueba.DOCENTE_ASIGNAR)[0].assertIsDisplayed()
        assertEquals(0, cuantos(TagsPrueba.UTP_REVISAR))
    }

    @Test
    fun utp_entra_y_puede_abrir_la_revision_de_un_contenido() {
        ingresar("utp01", "1234")
        composeRule.waitUntilAtLeastOneExists(hasTestTag(TagsPrueba.UTP_REVISAR), 5_000)
        composeRule.onAllNodesWithTag(TagsPrueba.UTP_REVISAR)[0].performClick()
        composeRule.onNodeWithTag(TagsPrueba.UTP_APROBAR).assertIsDisplayed()
        composeRule.onNodeWithTag(TagsPrueba.UTP_RECHAZAR).assertIsDisplayed()
        assertEquals(0, cuantos(TagsPrueba.DOCENTE_INFORME))
    }
}
