package edu.metrostate.ics342.mediatracker.ui.library

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.metrostate.ics342.mediatracker.data.datastore.DefaultSessionRepository
import edu.metrostate.ics342.mediatracker.data.model.LibraryItem
import edu.metrostate.ics342.mediatracker.data.model.LibraryStatus
import edu.metrostate.ics342.mediatracker.data.network.DefaultMediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LibraryUiState {
    data object Loading : LibraryUiState
    data class Error(val message: String) : LibraryUiState
    data class Success(val items: List<LibraryItem>) : LibraryUiState
}

class LibraryViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: DefaultMediaRepository =
        DefaultMediaRepository(DefaultSessionRepository(application))
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<LibraryUiState>(LibraryUiState.Loading)
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private var currentStatus = LibraryStatus.WANT_TO

    init {
        loadLibrary(currentStatus)
    }

    fun loadLibrary(status: LibraryStatus) {
        currentStatus = status
        _uiState.value = LibraryUiState.Loading
        viewModelScope.launch {
            try {
                val page = repository.getLibrary(status)
                _uiState.value = LibraryUiState.Success(page.items)
            } catch (e: Exception) {
                _uiState.value = LibraryUiState.Error(e.message ?: "Failed to load library")
            }
        }
    }

    fun retry() = loadLibrary(currentStatus)

    fun clearError() {
        _errorMessage.value = null
    }

    /** Optimistic: the item disappears instantly; DELETE /library/{mediaId} happens in the background. */
    fun removeItem(mediaId: Int) {
        val current = _uiState.value as? LibraryUiState.Success ?: return
        val backup = current.items.find { it.mediaId == mediaId } ?: return
        _uiState.value = current.copy(items = current.items.filter { it.mediaId != mediaId })
        viewModelScope.launch {
            try {
                repository.removeFromLibrary(mediaId)
            } catch (e: Exception) {
                val latest = _uiState.value as? LibraryUiState.Success ?: return@launch
                _uiState.value = latest.copy(items = latest.items + backup)
                _errorMessage.value = "Couldn't remove item. Try again."
            }
        }
    }

    /**
     * Optimistic: since the active tab already filters to [currentStatus] server-side, a status
     * change away from that status makes the item disappear from the visible list instantly.
     * PUT /library/{mediaId} happens in the background; a failure restores the item with its
     * original status.
     */
    fun updateStatus(mediaId: Int, newStatus: LibraryStatus) {
        val current = _uiState.value as? LibraryUiState.Success ?: return
        val backup = current.items.find { it.mediaId == mediaId } ?: return
        if (backup.status == newStatus) return
        _uiState.value = current.copy(items = current.items.filter { it.mediaId != mediaId })
        viewModelScope.launch {
            try {
                repository.updateLibraryStatus(mediaId, newStatus)
            } catch (e: Exception) {
                val latest = _uiState.value as? LibraryUiState.Success ?: return@launch
                _uiState.value = latest.copy(items = latest.items + backup)
                _errorMessage.value = "Couldn't update status. Try again."
            }
        }
    }
}