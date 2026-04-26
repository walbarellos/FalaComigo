package br.com.falacomigo.core.tts

interface TtsController {
    fun speak(text: String)
    fun stop()
    fun shutdown()
    fun isAvailable(): Boolean
    fun setOnSpeechProgressListener(onStart: (String) -> Unit, onDone: (String) -> Unit, onError: (String) -> Unit)
}