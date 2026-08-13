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
        val quotes: List<Quote> = emptyList()
    ) : MediaDetailUiState
}

class MediaDetailViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: DefaultMediaRepository =
        DefaultMediaRepository(DefaultSessionRepository(application))
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
            // IMPORTANT: wrapped in supervisorScope. Without it, any one of these async{}
            // children throwing (e.g. MediaNotFoundException from detailDeferred) cancels
            // ALL siblings and the parent immediately, via structured concurrency — before
            // the try/catch below ever gets a chance to run. That was crashing the app.
            supervisorScope {
                // All five start concurrently. Library, favorite, reviews, and quotes are
                // best-effort: their failures are swallowed so only the detail request can
                // fail the screen.
                val detailDeferred   = async { repository.getMediaDetail(mediaId) }
                val libraryDeferred  = async { runCatching { repository.getLibraryItem(mediaId) }.getOrNull() }
                val favoriteDeferred = async { runCatching { repository.getFavorite(mediaId) }.getOrNull() }
                val reviewsDeferred  = async { runCatching { repository.getReviews(mediaId) }.getOrElse { emptyList() } }
                // getQuotes() returns *all* of the current user's quotes across every piece
                // of media, so it's filtered down to this screen's mediaId client-side —
                // there's no server-side filter for "quotes for this media."
                val quotesDeferred   = async {
                    runCatching { repository.getQuotes() }.getOrElse { emptyList() }
                        .filter { it.mediaId == mediaId }
                }

                // Await detail first — if it fails fast we don't block on slower secondary calls.
                val detail = try {
                    detailDeferred.await()
                } catch (e: MediaNotFoundException) {
                    libraryDeferred.cancel()
                    favoriteDeferred.cancel()
                    reviewsDeferred.cancel()
                    quotesDeferred.cancel()
                    _uiState.value = MediaDetailUiState.NotFound
                    return@supervisorScope
                } catch (e: Exception) {
                    libraryDeferred.cancel()
                    favoriteDeferred.cancel()
                    reviewsDeferred.cancel()
                    quotesDeferred.cancel()
                    _uiState.value = MediaDetailUiState.Error(e.message ?: "Unknown error")
                    return@supervisorScope
                }

                _uiState.value = MediaDetailUiState.Success(
                    detail        = detail,
                    libraryStatus = libraryDeferred.await()?.status,
                    isFavorited   = favoriteDeferred.await() != null,
                    reviews       = reviewsDeferred.await(),
                    quotes        = quotesDeferred.await()
                )
            }
        }
    }

    /** Optimistic: the button flips to "in library" instantly; the POST happens in the background. */
    fun addToLibrary() {
        val current = _uiState.value as? MediaDetailUiState.Success ?: return
        val mediaId = currentMediaId ?: return
        if (current.libraryStatus != null) return
        _uiState.value = current.copy(libraryStatus = LibraryStatus.WANT_TO)
        viewModelScope.launch {
            try {
                repository.addToLibrary(mediaId, LibraryStatus.WANT_TO)
            } catch (e: DuplicateLibraryException) {
                // Already in the library server-side — the optimistic state is already correct.
            } catch (e: Exception) {
                val updated = _uiState.value as? MediaDetailUiState.Success ?: return@launch
                _uiState.value = updated.copy(libraryStatus = null)
                _actionError.value = "Couldn't add to library. Try again."
            }
        }
    }

    /** Optimistic: the heart flips instantly; POST/DELETE /favorites happens in the background. */
    fun toggleFavorite() {
        val current = _uiState.value as? MediaDetailUiState.Success ?: return
        val mediaId = currentMediaId ?: return
        val wasFavorited = current.isFavorited
        _uiState.value = current.copy(isFavorited = !wasFavorited)
        viewModelScope.launch {
            try {
                if (wasFavorited) repository.removeFavorite(mediaId) else repository.addFavorite(mediaId)
            } catch (e: DuplicateFavoriteException) {
                // Already favorited server-side — the optimistic "favorited" state is already correct.
            } catch (e: Exception) {
                val updated = _uiState.value as? MediaDetailUiState.Success ?: return@launch
                _uiState.value = updated.copy(isFavorited = wasFavorited)
                _actionError.value = "Couldn't update favorite. Try again."
            }
        }
    }

    /**
     * Not optimistic (unlike addToLibrary/toggleFavorite) because we need the server-assigned
     * quote `id` back before it can be edited/deleted/liked — so the list only updates once
     * the POST actually succeeds.
     */
    fun addQuote(quoteText: String, pageNumber: Int?, isPublic: Boolean) {
        val current = _uiState.value as? MediaDetailUiState.Success ?: return
        val mediaId = currentMediaId ?: return
        if (quoteText.isBlank()) return

        viewModelScope.launch {
            try {
                val newQuote = repository.createQuote(
                    mediaId    = mediaId,
                    quoteText  = quoteText,
                    pageNumber = pageNumber,
                    isPublic   = isPublic
                )
                val updated = _uiState.value as? MediaDetailUiState.Success ?: return@launch
                _uiState.value = updated.copy(quotes = updated.quotes + newQuote)
            } catch (e: Exception) {
                _actionError.value = "Couldn't save quote. Try again."
            }
        }
    }

    fun clearActionError() {
        _actionError.value = null
    }
}
