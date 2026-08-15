package edu.metrostate.ics342.mediatracker.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.metrostate.ics342.mediatracker.data.datastore.DefaultSessionRepository
import edu.metrostate.ics342.mediatracker.data.model.DuplicateFavoriteException
import edu.metrostate.ics342.mediatracker.data.model.DuplicateLibraryException
import edu.metrostate.ics342.mediatracker.data.model.LibraryStatus
import edu.metrostate.ics342.mediatracker.data.model.MediaDetail
import edu.metrostate.ics342.mediatracker.data.model.MediaNotFoundException
import edu.metrostate.ics342.mediatracker.data.model.Quote
import edu.metrostate.ics342.mediatracker.data.model.Review
import edu.metrostate.ics342.mediatracker.data.network.DefaultMediaRepository
import edu.metrostate.ics342.mediatracker.data.network.SessionRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MediaDetailUiState {
    data object Loading : MediaDetailUiState
    data object NotFound : MediaDetailUiState
    data class Error(val message: String) : MediaDetailUiState
    data class Success(
        val detail: MediaDetail,
        val libraryStatus: LibraryStatus?,
        val isFavorited: Boolean,
        val reviews: List<Review>,
        val quotes: List<Quote> = emptyList(),
        val currentUserId: String? = null
    ) : MediaDetailUiState
}

class MediaDetailViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: DefaultMediaRepository =
        DefaultMediaRepository(DefaultSessionRepository(application)),
    private val sessionRepository: SessionRepository =
        DefaultSessionRepository(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<MediaDetailUiState>(MediaDetailUiState.Loading)
    val uiState: StateFlow<MediaDetailUiState> = _uiState.asStateFlow()

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    private var currentMediaId: Int? = null

    fun load(mediaId: Int) {
        currentMediaId = mediaId
        _uiState.value = MediaDetailUiState.Loading
        viewModelScope.launch {
            val userId = runCatching { sessionRepository.getUser()?.id }.getOrNull()

            supervisorScope {
                val detailDeferred   = async { repository.getMediaDetail(mediaId) }
                val libraryDeferred  = async { runCatching { repository.getLibraryItem(mediaId) }.getOrNull() }
                val favoriteDeferred = async { runCatching { repository.getFavorite(mediaId) }.getOrNull() }
                val reviewsDeferred  = async { runCatching { repository.getReviews(mediaId) }.getOrElse { emptyList() } }
                val quotesDeferred   = async {
                    runCatching { repository.getQuotes() }.getOrElse { emptyList() }
                        .filter { it.mediaId == mediaId }
                }

                val detail = try {
                    detailDeferred.await()
                } catch (e: MediaNotFoundException) {
                    libraryDeferred.cancel(); favoriteDeferred.cancel()
                    reviewsDeferred.cancel(); quotesDeferred.cancel()
                    _uiState.value = MediaDetailUiState.NotFound
                    return@supervisorScope
                } catch (e: Exception) {
                    libraryDeferred.cancel(); favoriteDeferred.cancel()
                    reviewsDeferred.cancel(); quotesDeferred.cancel()
                    _uiState.value = MediaDetailUiState.Error(e.message ?: "Unknown error")
                    return@supervisorScope
                }

                _uiState.value = MediaDetailUiState.Success(
                    detail        = detail,
                    libraryStatus = libraryDeferred.await()?.status,
                    isFavorited   = favoriteDeferred.await() != null,
                    reviews       = reviewsDeferred.await(),
                    quotes        = quotesDeferred.await(),
                    currentUserId = userId
                )
            }
        }
    }

    fun addToLibrary() {
        val current = _uiState.value as? MediaDetailUiState.Success ?: return
        val mediaId = currentMediaId ?: return
        if (current.libraryStatus != null) return
        _uiState.value = current.copy(libraryStatus = LibraryStatus.WANT_TO)
        viewModelScope.launch {
            try {
                repository.addToLibrary(mediaId, LibraryStatus.WANT_TO)
            } catch (e: DuplicateLibraryException) {
            } catch (e: Exception) {
                val updated = _uiState.value as? MediaDetailUiState.Success ?: return@launch
                _uiState.value = updated.copy(libraryStatus = null)
                _actionError.value = "Couldn't add to library. Try again."
            }
        }
    }

    fun toggleFavorite() {
        val current = _uiState.value as? MediaDetailUiState.Success ?: return
        val mediaId = currentMediaId ?: return
        val wasFavorited = current.isFavorited
        _uiState.value = current.copy(isFavorited = !wasFavorited)
        viewModelScope.launch {
            try {
                if (wasFavorited) repository.removeFavorite(mediaId) else repository.addFavorite(mediaId)
            } catch (e: DuplicateFavoriteException) {
            } catch (e: Exception) {
                val updated = _uiState.value as? MediaDetailUiState.Success ?: return@launch
                _uiState.value = updated.copy(isFavorited = wasFavorited)
                _actionError.value = "Couldn't update favorite. Try again."
            }
        }
    }

    fun addQuote(quoteText: String, pageNumber: Int?, isPublic: Boolean) {
        val current = _uiState.value as? MediaDetailUiState.Success ?: return
        val mediaId = currentMediaId ?: return
        if (quoteText.isBlank()) return

        viewModelScope.launch {
            try {
                val newQuote = repository.createQuote(mediaId, quoteText, pageNumber, isPublic)
                val updated = _uiState.value as? MediaDetailUiState.Success ?: return@launch
                _uiState.value = updated.copy(quotes = updated.quotes + newQuote)
            } catch (e: Exception) {
                _actionError.value = "Couldn't save quote. Try again."
            }
        }
    }

    fun editQuote(quoteId: Int, quoteText: String, pageNumber: Int?, isPublic: Boolean) {
        val current = _uiState.value as? MediaDetailUiState.Success ?: return
        if (quoteText.isBlank()) return

        viewModelScope.launch {
            try {
                val updatedQuote = repository.updateQuote(quoteId, quoteText, pageNumber, isPublic)
                val latest = _uiState.value as? MediaDetailUiState.Success ?: return@launch
                _uiState.value = latest.copy(
                    quotes = latest.quotes.map { if (it.id == quoteId) updatedQuote else it }
                )
            } catch (e: Exception) {
                _actionError.value = "Couldn't update quote. Try again."
            }
        }
    }

    fun deleteQuote(quoteId: Int) {
        val current = _uiState.value as? MediaDetailUiState.Success ?: return

        _uiState.value = current.copy(quotes = current.quotes.filterNot { it.id == quoteId })
        viewModelScope.launch {
            try {
                repository.deleteQuote(quoteId)
            } catch (e: Exception) {
                val latest = _uiState.value as? MediaDetailUiState.Success ?: return@launch
                _uiState.value = latest.copy(quotes = current.quotes)
                _actionError.value = "Couldn't delete quote. Try again."
            }
        }
    }

    fun clearActionError() {
        _actionError.value = null
    }
}
