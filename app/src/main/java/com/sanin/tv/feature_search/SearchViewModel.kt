package com.sanin.tv.feature_search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanin.tv.connections.anilist.Anilist
import com.sanin.tv.media.Media
import com.sanin.tv.util.Logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor() : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow<List<Media>>(emptyList())
    val results: StateFlow<List<Media>> = _results.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _filter = MutableStateFlow(AdvancedSearchFilter.EMPTY)
    val filter: StateFlow<AdvancedSearchFilter> = _filter.asStateFlow()

    private val _history = MutableStateFlow<List<String>>(emptyList())
    val history: StateFlow<List<String>> = _history.asStateFlow()

    private var debounceJob: Job? = null

    /** Called on every keystroke — debounces at 400 ms. */
    fun onQueryChanged(text: String) {
        _query.value = text
        debounceJob?.cancel()
        if (text.isBlank()) {
            _results.value = emptyList()
            return
        }
        debounceJob = viewModelScope.launch {
            delay(400)
            search(text)
        }
    }

    /** Called on IME submit — fires immediately and saves to history. */
    fun onSearchSubmit(text: String) {
        _query.value = text
        debounceJob?.cancel()
        if (text.isBlank()) return
        addToHistory(text)
        viewModelScope.launch { search(text) }
    }

    fun applyFilter(newFilter: AdvancedSearchFilter) {
        _filter.value = newFilter
        val q = _query.value
        if (q.isNotBlank()) {
            viewModelScope.launch { search(q) }
        }
    }

    fun removeFromHistory(entry: String) {
        _history.value = _history.value.filter { it != entry }
    }

    fun clearHistory() {
        _history.value = emptyList()
    }

    private fun addToHistory(entry: String) {
        val current = _history.value.toMutableList()
        current.remove(entry)
        current.add(0, entry)
        _history.value = current.take(20)
    }

    private suspend fun search(queryText: String) {
        _isLoading.value = true
        try {
            val mediaList = Anilist.query.searchAnime(
                query  = queryText,
                filter = _filter.value,
                page   = 1
            )
            _results.value = mediaList ?: emptyList()
        } catch (e: Exception) {
            Logger.log(e)
            _results.value = emptyList()
        } finally {
            _isLoading.value = false
        }
    }
}
