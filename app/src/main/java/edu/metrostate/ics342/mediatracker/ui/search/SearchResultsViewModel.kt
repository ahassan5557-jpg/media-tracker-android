package edu.metrostate.ics342.mediatracker.ui.search


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.metrostate.ics342.mediatracker.data.datastore.DefaultSessionRepository
import edu.metrostate.ics342.mediatracker.data.model.Media
import edu.metrostate.ics342.mediatracker.data.network.DefaultMediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchResultsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DefaultMediaRepository(DefaultSessionRepository(application))

    private val _results = MutableStateFlow<List<Media>>(emptyList())
    val results: StateFlow<List<Media>> = _results.asStateFlow()

    private val _selectedType = MutableStateFlow("")
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentQuery = ""

    fun search(query: String) {
        currentQuery = query
        fetchResults()
    }

    fun onTypeSelect(type: String) {
        _selectedType.value = type
        fetchResults()
    }

    private fun fetchResults() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val page = repository.search(
                    query = currentQuery,
                    type  = _selectedType.value.ifBlank { null },
                    after = null
                )
                _results.value = page.items
            } catch (e: Exception) {
                _error.value = e.message ?: "Search failed"
                _results.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
