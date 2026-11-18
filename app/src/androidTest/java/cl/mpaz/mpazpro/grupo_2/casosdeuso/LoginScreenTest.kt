package cl.mpaz.mpazpro.grupo_2.casosdeuso

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.mpaz.mpazpro.grupo_2.data.local.AppDatabase
import cl.mpaz.mpazpro.grupo_2.data.local.Preferencias
import cl.mpaz.mpazpro.grupo_2.data.repository.MPazRepository
import cl.mpaz.mpazpro.grupo_2.domain.model.Rol
import cl.mpaz.mpazpro.grupo_2.ui.components.CampoValidado
import cl.mpaz.mpazpro.grupo_2.ui.components.TagsPrueba
import cl.mpaz.mpazpro.grupo_2.ui.screens.LoginScreen
import cl.mpaz.mpazpro.grupo_2.ui.viewmodels.LoginViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Prueba de UI de la pantalla de login y de un componente, renderizados solos (sin abrir toda la app),
// como en la guía: createAndroidComposeRule<ComponentActivity>() + setContent.
// CU01 · Iniciar sesión. Herramientas: JUnit + Compose UI Test (ComposeTestRule + testTag).

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var db: AppDatabase
    private lateinit var vm: LoginViewModel

    // Preferencias en memoria para no tocar las de la app
    private val preferencias = object : Preferencias {
        override val usuarioRecordado = MutableStateFlow("")
        override suspend fun guardarUsuario(usuario: String) { usuarioRecordado.value = usuario }
    }

    @Before
    fun preparar() {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(contexto, AppDatabase::class.java).build()
        vm = LoginViewModel(MPazRepository(db.dao(), preferencias))
    }

    @After
    fun cerrar() = db.close()

    @Test
    fun la_pantalla_muestra_los_campos_y_el_boton_entrar() {
        composeRule.setContent { LoginScreen(onIngreso = {}, viewModel = vm) }
        composeRule.onNodeWithTag(TagsPrueba.LOGIN_USUARIO).assertIsDisplayed()
        composeRule.onNodeWithTag(TagsPrueba.LOGIN_PIN).assertIsDisplayed()
        composeRule.onNodeWithTag(TagsPrueba.LOGIN_ENTRAR).assertIsEnabled()
    }

    @Test
    fun usuario_y_pin_correctos_llaman_a_onIngreso_con_el_rol() {
        var rolIngresado: Rol? = null
        composeRule.setContent { LoginScreen(onIngreso = { rolIngresado = it }, viewModel = vm) }

        composeRule.onNodeWithTag(TagsPrueba.LOGIN_USUARIO).performTextInput("est01")
        composeRule.onNodeWithTag(TagsPrueba.LOGIN_PIN).performTextInput("1111")
        composeRule.onNodeWithTag(TagsPrueba.LOGIN_ENTRAR).performClick()

        composeRule.waitUntil(5_000) { rolIngresado != null }
        assertEquals(Rol.ESTUDIANTE, rolIngresado)
    }

    @Test
    fun marcar_recordar_mi_usuario_queda_activado() {
        composeRule.setContent { LoginScreen(onIngreso = {}, viewModel = vm) }
        composeRule.onNodeWithTag(TagsPrueba.LOGIN_RECORDAR).performClick()
        composeRule.onNodeWithTag(TagsPrueba.LOGIN_RECORDAR).assertIsOn()
    }

    @Test
    fun un_campo_con_error_muestra_el_mensaje() {
        composeRule.setContent {
            CampoValidado("ab", {}, "Usuario", Icons.Filled.Person, "El usuario tiene al menos 4 caracteres")
        }
        composeRule.onNodeWithTag(TagsPrueba.CAMPO_ERROR).assertIsDisplayed()
        composeRule.onNodeWithText("⚠ El usuario tiene al menos 4 caracteres").assertIsDisplayed()
    }
}
