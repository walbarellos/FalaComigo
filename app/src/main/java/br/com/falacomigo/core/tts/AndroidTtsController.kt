package br.com.falacomigo.core.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import br.com.falacomigo.data.repository.SettingsRepository
import java.util.Locale

class AndroidTtsController(
    context: Context,
    private val settingsRepository: SettingsRepository
) : TtsController {
    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var isInitializing = false
    private var pendingText: String? = null
    private var speechRate = settingsRepository.voiceSpeechRate.value
    private var pitch = settingsRepository.voicePitch.value
    private var offlineOnly = settingsRepository.voiceOfflineOnly.value
    private var availableVoices: List<TtsVoiceOption> = emptyList()
    private var selectedVoiceId: String? = settingsRepository.voiceId.value

    private var onStartListener: ((String) -> Unit)? = null
    private var onDoneListener: ((String) -> Unit)? = null
    private var onErrorListener: ((String) -> Unit)? = null

    private val initListener = TextToSpeech.OnInitListener { status ->
        isInitializing = false
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { engine ->
                val result = engine.setLanguage(Locale("pt", "BR"))
                Log.d("TTS", "setLanguage result: $result")
                refreshAvailableVoices(engine, offlineOnly)
                selectedVoiceId?.let { selectVoice(it) }
                
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
                pendingText?.let { text ->
                    pendingText = null
                    speak(text)
                }
            }
        } else {
            isInitialized = false
            Log.e("TTS", "TTS init failed with status: $status")
        }
    }

    init {
        initializeTts()
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
        val normalizedText = normalizeSpeechText(text)
        if (normalizedText.isBlank()) return
        
        if (!isInitialized) {
            Log.w("TTS", "TTS not initialized. Scheduling speech after init.")
            pendingText = normalizedText
            initializeTts()
            return
        }
        
        try {
            tts?.let { engine ->
                engine.setSpeechRate(speechRate)
                engine.setPitch(pitch)
                
                // Usamos QUEUE_FLUSH para interromper o anterior e falar o novo imediatamente
                // Adicionamos um ID único baseado no tempo para garantir que os callbacks de progresso funcionem
                val utteranceId = "${normalizedText.hashCode()}_${System.currentTimeMillis()}"
                engine.speak(normalizedText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            }
        } catch (e: Exception) {
            Log.e("TTS", "speak error: ${e.message}")
            recoverTts(normalizedText)
        }
    }

    override fun warmUp() {
        if (!isInitialized) return
        try {
            // Toca 10ms de silêncio para acordar o hardware de áudio do Android
            tts?.playSilentUtterance(10L, TextToSpeech.QUEUE_ADD, null)
        } catch (e: Exception) {
            // Ignorado, é apenas um warm-up
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
        isInitializing = false
    }

    override fun isAvailable(): Boolean = isInitialized

    override fun getAvailableVoices(): List<TtsVoiceOption> = availableVoices

    override fun getCurrentVoiceId(): String? = selectedVoiceId

    override fun selectVoice(voiceId: String): Boolean {
        val engine = tts ?: return false
        val voice = engine.voices?.firstOrNull { it.name == voiceId } ?: return false
        val result = engine.setVoice(voice)
        val success = result == TextToSpeech.SUCCESS
        if (success) {
            selectedVoiceId = voice.name
            settingsRepository.setVoiceId(voice.name)
        }
        return success
    }

    override fun setSpeechRate(rate: Float) {
        speechRate = rate.coerceIn(0.5f, 1.5f)
        settingsRepository.setVoiceSpeechRate(speechRate)
        tts?.setSpeechRate(speechRate)
        Log.d("TTS", "SpeechRate set to: $speechRate")
    }

    override fun setPitch(pitch: Float) {
        this.pitch = pitch.coerceIn(0.7f, 1.3f)
        settingsRepository.setVoicePitch(this.pitch)
        tts?.setPitch(this.pitch)
    }

    override fun refreshVoices(offlineOnly: Boolean) {
        this.offlineOnly = offlineOnly
        settingsRepository.setVoiceOfflineOnly(offlineOnly)
        tts?.let { refreshAvailableVoices(it, offlineOnly) }
    }

    private fun refreshAvailableVoices(engine: TextToSpeech, offlineOnly: Boolean) {
        val portugueseVoices = engine.voices
            ?.filter {
                it.locale.language == "pt" &&
                    it.locale.country == "BR" &&
                    (!offlineOnly || !it.isNetworkConnectionRequired)
            }
            ?.distinctBy { voiceListKey(it) }
            ?.let { voices ->
                val specificVoices = voices.filterNot { isGenericLanguageVoice(it) }
                specificVoices.ifEmpty { voices }
            }
            ?.sortedWith(compareBy<Voice> { it.isNetworkConnectionRequired }.thenBy { it.name })
            .orEmpty()

        availableVoices = portugueseVoices.mapIndexed { index, voice ->
            TtsVoiceOption(
                id = voice.name,
                name = formatVoiceName(index, voice),
                locale = voice.locale.toLanguageTag(),
                isNetworkRequired = voice.isNetworkConnectionRequired
            )
        }
        selectedVoiceId = settingsRepository.voiceId.value ?: engine.voice?.name
    }

    private fun voiceListKey(voice: Voice): String {
        return voice.name
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
    }

    private fun isGenericLanguageVoice(voice: Voice): Boolean {
        val normalizedName = voice.name.lowercase(Locale.ROOT)
        return normalizedName.endsWith("-language") || normalizedName.contains("-language-")
    }

    private fun formatVoiceName(index: Int, voice: Voice): String {
        val suffix = if (voice.isNetworkConnectionRequired) "online" else "offline"
        return "Português Brasil · Voz ${index + 1} · $suffix"
    }

    private fun normalizeSpeechText(text: String): String {
        val cleaned = text.trim().replace(Regex("\\s+"), " ")
        if (cleaned.isBlank()) return cleaned
        val capitalized = cleaned.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() }
        return if (capitalized.last() in ".!?") capitalized else "$capitalized."
    }

    private fun initializeTts() {
        if (isInitialized || isInitializing) return
        Log.d("TTS", "Creating AndroidTtsController...")
        isInitializing = true
        tts = TextToSpeech(appContext, initListener)
    }

    private fun recoverTts(textToRetry: String) {
        pendingText = textToRetry
        shutdown()
        initializeTts()
    }
}
