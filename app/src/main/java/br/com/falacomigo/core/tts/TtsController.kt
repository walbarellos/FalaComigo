package br.com.falacomigo.core.tts

data class TtsVoiceOption(
    val id: String,
    val name: String,
    val locale: String,
    val isNetworkRequired: Boolean
)

interface TtsController {
    fun speak(text: String)
    fun warmUp() // NASA: Acorda o motor de áudio no background
    fun stop()
    fun shutdown()
    fun isAvailable(): Boolean
    fun getAvailableVoices(): List<TtsVoiceOption> = emptyList()
    fun getCurrentVoiceId(): String? = null
    fun selectVoice(voiceId: String): Boolean = false
    fun setSpeechRate(rate: Float) {}
    fun setPitch(pitch: Float) {}
    fun refreshVoices(offlineOnly: Boolean) {}
    fun setOnSpeechProgressListener(onStart: (String) -> Unit, onDone: (String) -> Unit, onError: (String) -> Unit)
}
