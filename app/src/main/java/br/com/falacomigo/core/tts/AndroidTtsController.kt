package br.com.falacomigo.core.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class AndroidTtsController(private val context: Context) : TtsController {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var speechRate = 1.5f  // Default mais alto
    private var pitch = 1.0f

    private val initListener = TextToSpeech.OnInitListener { status ->
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { engine ->
                val result = engine.setLanguage(Locale("pt", "BR"))
                Log.d("TTS", "setLanguage result: $result")
                
                engine.setSpeechRate(speechRate)
                engine.setPitch(pitch)
                isInitialized = true
                Log.d("TTS", "TTS initialized successfully!")
            }
        } else {
            Log.e("TTS", "TTS init failed with status: $status")
        }
    }

    init {
        Log.d("TTS", "Creating AndroidTtsController...")
        tts = TextToSpeech(context, initListener)
    }

    override fun speak(text: String) {
        Log.d("TTS", "speak() called with: $text, initialized: $isInitialized")
        
        if (text.isBlank()) {
            Log.w("TTS", "Text is blank, skipping")
            return
        }
        
        if (!isInitialized) {
            Log.w("TTS", "TTS not initialized yet, queuing initialization")
            // Retry after delay
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                speak(text)
            }, 500)
            return
        }
        
        try {
            tts?.let { engine ->
                engine.setSpeechRate(speechRate)
                engine.setPitch(pitch)
                
                val result = engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, text.hashCode().toString())
                Log.d("TTS", "speak() result: $result")
            }
        } catch (e: Exception) {
            Log.e("TTS", "speak() exception: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun stop() {
        tts?.stop()
    }

    override fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }

    override fun isAvailable(): Boolean = isInitialized

    fun setSpeechRate(rate: Float) {
        speechRate = rate.coerceIn(0.5f, 2.5f)
        tts?.setSpeechRate(speechRate)
        Log.d("TTS", "SpeechRate set to: $speechRate")
    }

    fun setPitch(pitchValue: Float) {
        pitch = pitchValue.coerceIn(0.5f, 2.0f)
        tts?.setPitch(pitch)
    }

    fun setSpeedNormal() {
        setSpeechRate(1.5f)  // Mais alto que 1.0
        setPitch(1.0f)
    }

    fun setSpeedSlow() {
        setSpeechRate(1.0f)
        setPitch(1.0f)
    }

    fun setSpeedFast() {
        setSpeechRate(2.0f)
        setPitch(1.0f)
    }
}