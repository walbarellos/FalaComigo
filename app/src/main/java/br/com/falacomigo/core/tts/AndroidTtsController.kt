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

    private var onStartListener: ((String) -> Unit)? = null
    private var onDoneListener: ((String) -> Unit)? = null
    private var onErrorListener: ((String) -> Unit)? = null

    private val initListener = TextToSpeech.OnInitListener { status ->
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { engine ->
                val result = engine.setLanguage(Locale("pt", "BR"))
                Log.d("TTS", "setLanguage result: $result")
                
                engine.setSpeechRate(speechRate)
                engine.setPitch(pitch)
                
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        Log.d("TTS", "onStart: $utteranceId")
                        utteranceId?.let { onStartListener?.invoke(it) }
                    }

                    override fun onDone(utteranceId: String?) {
                        Log.d("TTS", "onDone: $utteranceId")
                        utteranceId?.let { onDoneListener?.invoke(it) }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        Log.e("TTS", "onError: $utteranceId")
                        utteranceId?.let { onErrorListener?.invoke(it) }
                    }
                })
                
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

    override fun setOnSpeechProgressListener(
        onStart: (String) -> Unit,
        onDone: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        this.onStartListener = onStart
        this.onDoneListener = onDone
        this.onErrorListener = onError
    }

    override fun speak(text: String) {
        if (text.isBlank()) return
        
        if (!isInitialized) {
            Log.w("TTS", "TTS not initialized yet")
            return
        }
        
        try {
            tts?.let { engine ->
                engine.setSpeechRate(speechRate)
                engine.setPitch(pitch)
                
                // Usamos QUEUE_FLUSH para interromper o anterior e falar o novo imediatamente
                // Adicionamos um ID único baseado no tempo para garantir que os callbacks de progresso funcionem
                val utteranceId = "${text.hashCode()}_${System.currentTimeMillis()}"
                engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            }
        } catch (e: Exception) {
            Log.e("TTS", "speak error: ${e.message}")
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