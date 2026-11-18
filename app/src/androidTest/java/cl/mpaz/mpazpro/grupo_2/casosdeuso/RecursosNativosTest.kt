package cl.mpaz.mpazpro.grupo_2.casosdeuso

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.mpaz.mpazpro.grupo_2.domain.model.Informe
import cl.mpaz.mpazpro.grupo_2.domain.model.LogroPregunta
import cl.mpaz.mpazpro.grupo_2.domain.model.aCsv
import cl.mpaz.mpazpro.grupo_2.nativo.ExportadorCsv
import cl.mpaz.mpazpro.grupo_2.nativo.LectorVoz
import cl.mpaz.mpazpro.grupo_2.nativo.Notificaciones
import cl.mpaz.mpazpro.grupo_2.nativo.Vibracion
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

// Herramientas: JUnit (AndroidJUnit4) + kotlinx-coroutines-test.
// Prueba instrumentada de los recursos nativos REALES (se ejecuta con ./gradlew connectedAndroidTest).
// CU17 · aviso al terminar la actividad, vibración y audio de apoyo (TextToSpeech).
// CU11 · descarga del informe CSV.

@RunWith(AndroidJUnit4::class)
class RecursosNativosTest {

    private val contexto: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun avisoDeActividadTerminadaApareceEnElTelefono() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            InstrumentationRegistry.getInstrumentation().uiAutomation
                .grantRuntimePermission(contexto.packageName, Manifest.permission.POST_NOTIFICATIONS)
        }
        val avisos = Notificaciones(contexto)
        avisos.crearCanal()
        assertTrue(avisos.tienePermiso())

        avisos.mostrar("¡Actividad terminada!", "3 de 3 correctas. Tu avance quedó guardado.")

        val gestor = contexto.getSystemService(NotificationManager::class.java)
        fun titulos() = gestor.activeNotifications.map {
            it.notification.extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        }
        // El sistema publica el aviso en otro proceso: se espera de verdad (máximo 5 s) sin Thread.sleep
        runBlocking { withTimeoutOrNull(5_000) { while ("¡Actividad terminada!" !in titulos()) delay(100) } }
        assertTrue("Debería verse el aviso: ${titulos()}", "¡Actividad terminada!" in titulos())
        gestor.cancelAll()
    }

    @Test
    fun vibracionNoFalla() {
        val vibracion = Vibracion(contexto)
        vibracion.correcto()
        vibracion.incorrecto()
    }

    @Test
    fun lecturaEnVozAltaSePuedeDetener() {
        val lector = InstrumentationRegistry.getInstrumentation().let { inst ->
            var l: LectorVoz? = null
            inst.runOnMainSync { l = LectorVoz(contexto) }
            l!!
        }
        lector.leer("¿Qué sigue? 130, 140, 150")
        lector.detener()
        assertFalse(lector.hablando.value)
    }

    @Test
    fun informeCsvSeGuardaEnUnArchivo() = runTest {
        val archivo = File(contexto.cacheDir, "informe_prueba.csv").apply { delete() }
        val csv = Informe(50, listOf(LogroPregunta("¿Qué sigue?", 50)), emptyList(), sinDatos = false)
            .aCsv("MA03 OA 01", "Reforzar conteo", "Pendiente de validación")

        val ok = ExportadorCsv(contexto).escribir(Uri.fromFile(archivo), csv)

        assertTrue(ok)
        val texto = archivo.readText(Charsets.UTF_8)
        assertTrue(texto.contains("Informe preliminar de uso docente;MA03 OA 01"))
        assertTrue(texto.contains("Estado de validación;Pendiente de validación"))
    }
}
