package cl.mpaz.mpazpro.grupo_2.nativo

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

// Recurso nativo de texto a voz (TextToSpeech). Lee en voz alta las preguntas,
// pensado para estudiantes de 3.º básico que aún están afianzando la lectura.

class LectorVoz(context: Context) : TextToSpeech.OnInitListener, ServicioVoz {

    private val tts = TextToSpeech(context.applicationContext, this)
    private var listo = false

    private val _hablando = MutableStateFlow(false)
    override val hablando: StateFlow<Boolean> = _hablando.asStateFlow()

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) return
        val resultado = tts.setLanguage(Locale.forLanguageTag("es-CL"))
        if (resultado == TextToSpeech.LANG_MISSING_DATA || resultado == TextToSpeech.LANG_NOT_SUPPORTED) {
            tts.setLanguage(Locale.forLanguageTag("es"))   // si no hay voz chilena, español general
        }
        tts.setSpeechRate(0.9f)   // un poco más lento para niños
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) { _hablando.value = true }
            override fun onDone(utteranceId: String?) { _hablando.value = false }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) { _hablando.value = false }
        })
        listo = true
    }

    override fun leer(texto: String) {
        if (!listo) return
        tts.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "mpaz_lectura")
    }

    override fun detener() {
        if (listo) tts.stop()
        _hablando.value = false
    }
}
