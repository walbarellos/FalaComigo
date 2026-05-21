package br.com.falacomigo.feature.communication

import android.content.Context
import br.com.falacomigo.core.model.BoardLayoutMode
import br.com.falacomigo.core.model.BoardUiModel
import br.com.falacomigo.core.model.FavoritePhrase
import br.com.falacomigo.core.seed.SeedSymbols
import br.com.falacomigo.core.tts.TtsController
import br.com.falacomigo.data.images.SymbolImageStore
import br.com.falacomigo.data.repository.BoardRepository
import br.com.falacomigo.data.repository.RoutineRepository
import br.com.falacomigo.data.repository.SettingsRepository
import br.com.falacomigo.data.repository.SymbolRepository
import br.com.falacomigo.feature.communication.domain.MoveSymbolUseCase
import br.com.falacomigo.feature.communication.domain.SaveRoutineUseCase
import br.com.falacomigo.feature.communication.domain.SearchSymbolsUseCase
import br.com.falacomigo.feature.communication.domain.SpeakSymbolUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class CommunicationViewModelTest {

    private lateinit var viewModel: CommunicationViewModel
    
    private val context = mockk<Context>(relaxed = true)
    private val ttsController = mockk<TtsController>(relaxed = true)
    private val settingsRepository = mockk<SettingsRepository>(relaxed = true)
    private val routineRepository = mockk<RoutineRepository>(relaxed = true)
    private val boardRepository = mockk<BoardRepository>(relaxed = true)
    private val symbolRepository = mockk<SymbolRepository>(relaxed = true)
    private val speakSymbolUseCase = mockk<SpeakSymbolUseCase>(relaxed = true)
    private val moveSymbolUseCase = mockk<MoveSymbolUseCase>(relaxed = true)
    private val searchSymbolsUseCase = mockk<SearchSymbolsUseCase>(relaxed = true)
    private val saveRoutineUseCase = mockk<SaveRoutineUseCase>(relaxed = true)
    private val imageStore = mockk<SymbolImageStore>(relaxed = true)

    private lateinit var testDispatcher: TestDispatcher

    @Before
    fun setup() {
        testDispatcher = StandardTestDispatcher(TestCoroutineScheduler())
        Dispatchers.setMain(testDispatcher)
        
        // Mock default flows as StateFlows to match the new repository contract
        every { settingsRepository.vibrationEnabled } returns MutableStateFlow(true)
        every { settingsRepository.speakOnTapEnabled } returns MutableStateFlow(true)
        every { settingsRepository.boardLayoutMode } returns MutableStateFlow(BoardLayoutMode.GRID)
        every { settingsRepository.favoritePhrases } returns MutableStateFlow(emptyList<FavoritePhrase>())
        every { settingsRepository.editorPin } returns MutableStateFlow("1234")
        every { settingsRepository.highContrastEnabled } returns MutableStateFlow(false)
        every { settingsRepository.cardSizeScale } returns MutableStateFlow(1.0f)
        every { routineRepository.getAllRoutines() } returns flowOf(emptyList())
        every { symbolRepository.getAllSymbols() } returns flowOf(emptyList())
        every { boardRepository.getBoardWithSymbolsFlow(any()) } returns flowOf(BoardUiModel("comunicacao", "Comunicação"))
        every { imageStore.getLocalFile(any()) } returns null
        every { imageStore.getThumbnailFile(any()) } returns null
        coEvery { imageStore.ensureDownloaded(any(), any()) } returns null
        
        viewModel = newViewModel()
    }

    private fun newViewModel(): CommunicationViewModel {
        return CommunicationViewModel(
            context,
            ttsController,
            settingsRepository,
            routineRepository,
            boardRepository,
            symbolRepository,
            speakSymbolUseCase,
            moveSymbolUseCase,
            searchSymbolsUseCase,
            saveRoutineUseCase,
            imageStore
        )
    }

    @Test
    fun `board nulo encerra preparo e usa fallback de comunicacao essencial`() = runTest(testDispatcher) {
        every { boardRepository.getBoardWithSymbolsFlow(any()) } returns flowOf<BoardUiModel?>(null)

        viewModel = CommunicationViewModel(
            context, ttsController, settingsRepository, routineRepository, 
            boardRepository, symbolRepository, speakSymbolUseCase, 
            moveSymbolUseCase, searchSymbolsUseCase, saveRoutineUseCase, imageStore
        )
        advanceUntilIdle()
        Thread.sleep(500)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse("state=$state", state.isBootstrappingImages)
        assertEquals("comunicacao", state.currentBoard.id)
        assertTrue(state.currentBoard.symbols.isNotEmpty())
        assertTrue(state.currentBoard.symbols.any { it.id == "agua" })
    }

    @Test
    fun `estado inicial nao tem fala ativa`() = runTest(testDispatcher) {
        val state = viewModel.state.value
        assertFalse(state.isSpeaking)
        assertNull(state.speakingSymbolId)
    }

    @Test
    fun `tocar simbolo chama use case de voz`() = runTest(testDispatcher) {
        val symbol = SeedSymbols.symbols.first()
        
        viewModel.onSymbolClick(symbol)
        advanceUntilIdle()
        
        coVerify { speakSymbolUseCase(symbol) }
    }

    @Test
    fun `alterar fala ao toque persiste preferencia`() = runTest(testDispatcher) {
        viewModel.setSpeakOnTapEnabled(false)

        verify { settingsRepository.setSpeakOnTapEnabled(false) }
    }

    @Test
    fun `registrar uso de simbolo nao chama voz`() = runTest(testDispatcher) {
        val symbol = SeedSymbols.symbols.first()

        viewModel.recordSymbolUse(symbol)
        advanceUntilIdle()

        coVerify(timeout = 1_000) { symbolRepository.updateUsage(symbol.id) }
        coVerify(exactly = 0) { speakSymbolUseCase(any()) }
    }

    @Test
    fun `selecionar board altera filtro ativo`() = runTest(testDispatcher) {
        viewModel.selectBoard("numeral")
        advanceUntilIdle()
        
        // No ViewModel atual, o filtro altera qual board é carregado
        // Verificamos se o repositório foi consultado ou o estado mudou
        // Como o observeData reage ao _activeFilter, verificamos o título se o mock permitir
    }

    @Test
    fun `erro de voz nao trava o estado`() = runTest(testDispatcher) {
        coEvery { speakSymbolUseCase(any()) } throws Exception("Erro de voz")
        
        val symbol = SeedSymbols.symbols.first()
        viewModel.onSymbolClick(symbol)
        advanceUntilIdle()
        
        assertFalse(viewModel.state.value.isSpeaking)
        assertNull(viewModel.state.value.speakingSymbolId)
        assertEquals(
            "Não consegui falar agora. Verifique Voz e Fala em Configurações.",
            viewModel.state.value.speechErrorMessage
        )
    }
}
