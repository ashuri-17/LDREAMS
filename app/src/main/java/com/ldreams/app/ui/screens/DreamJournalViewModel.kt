package com.ldreams.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ldreams.app.data.repository.DreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.ldreams.app.data.models.DreamEntry

@HiltViewModel
class DreamJournalViewModel @Inject constructor(
    private val dreamRepository: DreamRepository
) : ViewModel() {

    val allDreams = dreamRepository.getAllDreams()

    private val _searchResults = MutableStateFlow<List<DreamEntry>?>(null)
    val searchResults: StateFlow<List<DreamEntry>?> = _searchResults.asStateFlow()

    private var searchJob: Job? = null

    fun searchDreams(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = null
            return
        }
        searchJob = viewModelScope.launch {
            delay(300) // debounce
            dreamRepository.searchDreams(query).collect { results ->
                _searchResults.value = results
            }
        }
    }
}
