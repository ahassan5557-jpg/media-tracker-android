package edu.metrostate.ics342.mediatracker.ui.search

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SearchViewModel : ViewModel() {
    // TODO (Week 5): Add query StateFlow, results StateFlow, loading/error states.
    // Wire to GET /media with debounced LaunchedEffect.
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    private val _selectedType = MutableStateFlow("")
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    fun onQueryChange(value: String) { _query.value = value }
    fun clearQuery() { _query.value = "" }
    fun onTypeSelect(type: String) { _selectedType.value = type }
}
