package br.com.falacomigo.feature.communication.domain

import br.com.falacomigo.core.model.SymbolUiModel
import br.com.falacomigo.core.tts.TtsController
import br.com.falacomigo.data.repository.SymbolRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Caso de Uso: Executa a ação de falar um símbolo e atualiza sua métrica de uso.
 * Aplica os princípios de Coesão e Separação de Preocupações (SoC).
 */
class SpeakSymbolUseCase @Inject constructor(
    private val ttsController: TtsController,
    private val symbolRepository: SymbolRepository
) {
    suspend operator fun invoke(symbol: SymbolUiModel) = withContext(Dispatchers.IO) {
        // 1. Ação de Hardware (TTS)
        val text = symbol.spokenText.ifEmpty { symbol.label }
        if (text.isNotBlank()) {
            ttsController.speak(text)
        }

        // 2. Atualização de Dados (Data-Driven)
        if (!symbol.id.startsWith("routine_")) {
            symbolRepository.updateUsage(symbol.id)
        }
    }
}
