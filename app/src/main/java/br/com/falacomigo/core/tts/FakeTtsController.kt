package br.com.falacomigo.core.tts

class FakeTtsController : TtsController {
    private var lastSpoken: String = ""
    private var speakCount = 0
    private var stopped = false
    private var onStartListener: ((String) -> Unit)? = null
    private var onDoneListener: ((String) -> Unit)? = null

    override fun speak(text: String) {
        if (text.isBlank()) return
        lastSpoken = text
        speakCount++
        stopped = false
        println("FAKE TTS: $text")
        
        onStartListener?.invoke(text.hashCode().toString())
        // Simulate speech delay
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            onDoneListener?.invoke(text.hashCode().toString())
        }, 1000)
    }

    override fun warmUp() {}

    override fun stop() {
        stopped = true
    }

    override fun shutdown() {
        stopped = true
        lastSpoken = ""
        speakCount = 0
    }

    override fun isAvailable(): Boolean = true

    override fun getAvailableVoices(): List<TtsVoiceOption> = listOf(
        TtsVoiceOption(
            id = "fake-pt-br",
            name = "Português Brasil · Fake",
            locale = "pt-BR",
            isNetworkRequired = false
        )
    )

    override fun getCurrentVoiceId(): String? = "fake-pt-br"

    override fun selectVoice(voiceId: String): Boolean = voiceId == "fake-pt-br"

    override fun setOnSpeechProgressListener(
        onStart: (String) -> Unit,
        onDone: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        this.onStartListener = onStart
        this.onDoneListener = onDone
    }

    fun getLastSpoken() = lastSpoken
    fun getSpeakCount() = speakCount
}
