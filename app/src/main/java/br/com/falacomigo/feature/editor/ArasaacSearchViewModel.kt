package br.com.falacomigo.feature.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.falacomigo.data.remote.ArasaacPictogram
import br.com.falacomigo.data.repository.ArasaacRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArasaacSearchState(
    val query: String = "",
    val results: List<ArasaacPictogram> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selected: ArasaacPictogram? = null,
)

sealed interface ArasaacSearchEvent {
    data class QueryChanged(val query: String) : ArasaacSearchEvent
    data class PictogramSelected(val pictogram: ArasaacPictogram) : ArasaacSearchEvent
    data object ClearSearch : ArasaacSearchEvent
}

@HiltViewModel
class ArasaacSearchViewModel @Inject constructor(
    private val repository: ArasaacRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ArasaacSearchState())
    val state: StateFlow<ArasaacSearchState> = _state.asStateFlow()

    private var searchJob: Job? = null

    fun onEvent(event: ArasaacSearchEvent) {
        when (event) {
            is ArasaacSearchEvent.QueryChanged -> onQueryChanged(event.query)
            is ArasaacSearchEvent.PictogramSelected -> {
                _state.update { it.copy(selected = event.pictogram) }
            }
            ArasaacSearchEvent.ClearSearch -> {
                _state.update { ArasaacSearchState() }
            }
        }
    }

    private fun onQueryChanged(query: String) {
        _state.update { it.copy(query = query, selected = null) }

        searchJob?.cancel()
        if (query.length < 2) {
            _state.update { it.copy(results = emptyList(), isLoading = false) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(400) // Debounce
            _state.update { it.copy(isLoading = true, error = null) }
            
            repository.search(query)
                .onSuccess { results ->
                    _state.update { it.copy(results = results.take(20), isLoading = false) }
                }
                .onFailure {
                    _state.update { it.copy(isLoading = false, error = "Erro ao buscar na ARASAAC") }
                }
        }
    }

    fun savePictogram(pictogram: ArasaacPictogram) {
        viewModelScope.launch {
            repository.savePictogram(pictogram)
        }
    }
}