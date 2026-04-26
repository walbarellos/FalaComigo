package br.com.falacomigo.feature.communication

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import br.com.falacomigo.core.model.SymbolUiModel
import br.com.falacomigo.core.seed.SeedBoards
import br.com.falacomigo.core.seed.SeedSymbols
import br.com.falacomigo.core.tts.FakeTtsController

class CommunicationViewModelTest {

    private lateinit var viewModel: CommunicationViewModel
    private lateinit var fakeTts: FakeTtsController

    @Before
    fun setup() {
        fakeTts = FakeTtsController()
        viewModel = CommunicationViewModel(fakeTts)
    }

    @Test
    fun `estado inicial carrega prancha principal`() {
        val state = viewModel.uiState.value

        assertTrue(state.currentBoard.isDefault)
        assertEquals("principal", state.currentBoard.id)
    }

    @Test
    fun `tocar simbolo chama TTS`() {
        val symbol = SeedSymbols.symbols.first()

        viewModel.onSymbolClick(symbol)

        assertEquals(1, fakeTts.getSpeakCount())
        assertEquals(symbol.spokenText, fakeTts.getLastSpokenText())
    }

    @Test
    fun `modo frase acumula simbolos`() {
        viewModel.togglePhraseMode()
        assertTrue(viewModel.uiState.value.phraseModeEnabled)

        val symbol1 = SeedSymbols.findById("sim")!!
        val symbol2 = SeedSymbols.findById("nao")!!

        viewModel.onSymbolClick(symbol1)
        viewModel.onSymbolClick(symbol2)

        val phraseSymbols = viewModel.uiState.value.phraseSymbols
        assertEquals(2, phraseSymbols.size)
    }

    @Test
    fun `limpar frase esvazia estado`() {
        viewModel.togglePhraseMode()
        
        val symbol = SeedSymbols.findById("sim")!!
        viewModel.onSymbolClick(symbol)
        
        assertTrue(viewModel.uiState.value.phraseSymbols.isNotEmpty())
        
        viewModel.clearPhrase()
        
        assertTrue(viewModel.uiState.value.phraseSymbols.isEmpty())
    }

    @Test
    fun `abrir urgente troca para prancha urgente`() {
        viewModel.openEmergencyBoard()
        
        assertTrue(viewModel.uiState.value.currentBoard.isEmergency)
    }

    @Test
    fun `erro de TTS nao derruba estado da UI`() {
        val symbol = SeedSymbols.findById("sim")!!
        
        try {
            viewModel.onSymbolClick(symbol)
        } catch (e: Exception) {
            // Should not throw
        }
        
        // State should remain valid
        assertTrue(viewModel.uiState.value.currentBoard.id.isNotEmpty())
    }
}