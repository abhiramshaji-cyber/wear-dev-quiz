package com.abhiram.devquiz

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

class Speech(context: Context) {

    var ready by mutableStateOf(false)
        private set

    private var engine: TextToSpeech? = null

    init {
        engine = TextToSpeech(context) { status ->
            val tts = engine
            if (status == TextToSpeech.SUCCESS && tts != null) {
                val installed = tts.setLanguage(QUEBEC) >= TextToSpeech.LANG_AVAILABLE ||
                    tts.setLanguage(Locale.FRENCH) >= TextToSpeech.LANG_AVAILABLE
                tts.setSpeechRate(SLOW)
                ready = installed
            }
        }
    }

    fun say(text: String) {
        if (!ready) return
        engine?.speak(text, TextToSpeech.QUEUE_FLUSH, null, text)
    }

    fun shutdown() {
        engine?.stop()
        engine?.shutdown()
        engine = null
        ready = false
    }

    private companion object {
        val QUEBEC: Locale = Locale.CANADA_FRENCH
        const val SLOW = 0.6f
    }
}
