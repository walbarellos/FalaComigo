package br.com.falacomigo.core.tts

class FakeTtsController : TtsController {
    private var lastSpoken: String = ""
    private var speakCount = 0
    private var stopped = false

    override fun speak(text: String) {
        if (text.isBlank()) return
        lastSpoken = text
        speakCount++
        stopped = false
        println("FAKE TTS: $text")
    }

    override fun stop() {
        stopped = true
    }

    override fun shutdown() {
        stopped = true
        lastSpoken = ""
        speakCount = 0
    }

    override fun isAvailable(): Boolean = true

    fun getLastSpoken() = lastSpoken
    fun getSpeakCount() = speakCount
}