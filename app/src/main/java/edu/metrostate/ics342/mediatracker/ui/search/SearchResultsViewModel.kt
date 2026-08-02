package edu.metrostate.ics342.mediatracker.ui.search


import androidx.lifecycle.ViewModel
import edu.metrostate.ics342.mediatracker.data.datastore.fakeSearchResults
import edu.metrostate.ics342.mediatracker.data.model.Media
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SearchResultsViewModel : ViewModel() {
    private val _results = MutableStateFlow<List<Media>>(emptyList())
    val results: StateFlow<List<Media>> = _results.asStateFlow()

    private val _selectedType = MutableStateFlow("")
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    private var currentQuery = ""

    fun search(query: String) {
        currentQuery = query
        applyFilter()
    }

    fun onTypeSelect(type: String) {
        _selectedType.value = type
        applyFilter()
    }

    private fun applyFilter() {
        val type = _selectedType.value
        _results.value = fakeSearchResults
    }
}