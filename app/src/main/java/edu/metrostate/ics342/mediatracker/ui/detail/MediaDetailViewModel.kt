
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import edu.metrostate.ics342.mediatracker.data.datastore.DefaultSessionRepository
import edu.metrostate.ics342.mediatracker.data.model.LibraryStatus
import edu.metrostate.ics342.mediatracker.data.model.MediaDetail
import edu.metrostate.ics342.mediatracker.data.model.MediaNotFoundException
import edu.metrostate.ics342.mediatracker.data.model.Review
import edu.metrostate.ics342.mediatracker.data.network.DefaultMediaRepository
import kotlinx.coroutines.async
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
        val reviews: List<Review>,
        val isAddingToLibrary: Boolean = false
    ) : MediaDetailUiState
}

class MediaDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DefaultMediaRepository(DefaultSessionRepository(application))

    private val _uiState = MutableStateFlow<MediaDetailUiState>(MediaDetailUiState.Loading)
    val uiState: StateFlow<MediaDetailUiState> = _uiState.asStateFlow()

    private var currentMediaId: Int? = null

    fun load(mediaId: Int) {
        currentMediaId = mediaId
        _uiState.value = MediaDetailUiState.Loading
        viewModelScope.launch {
            // All three start concurrently. Library and reviews are best-effort:
            // their failures are swallowed so only the detail request can fail the screen.
            val detailDeferred  = async { repository.getMediaDetail(mediaId) }
            val libraryDeferred = async { runCatching { repository.getLibraryItem(mediaId) }.getOrNull() }
            val reviewsDeferred = async { runCatching { repository.getReviews(mediaId) }.getOrElse { emptyList() } }

            // Await detail first — if it fails fast we don't block on slower secondary calls.
            val detail = try {
                detailDeferred.await()
            } catch (e: MediaNotFoundException) {
                libraryDeferred.cancel()
                reviewsDeferred.cancel()
                _uiState.value = MediaDetailUiState.NotFound
                return@launch
            } catch (e: Exception) {
                libraryDeferred.cancel()
                reviewsDeferred.cancel()
                _uiState.value = MediaDetailUiState.Error(e.message ?: "Unknown error")
                return@launch
            }

            _uiState.value = MediaDetailUiState.Success(
                detail        = detail,
                libraryStatus = libraryDeferred.await()?.status,
                reviews       = reviewsDeferred.await()
            )
        }
    }

    fun addToLibrary() {
        val current = _uiState.value as? MediaDetailUiState.Success ?: return
        val mediaId = currentMediaId ?: return
        if (current.isAddingToLibrary) return
        _uiState.value = current.copy(isAddingToLibrary = true)
        viewModelScope.launch {
            try {
                val item = repository.addToLibrary(mediaId, LibraryStatus.WANT_TO)
                val updated = _uiState.value as? MediaDetailUiState.Success ?: return@launch
                _uiState.value = updated.copy(
                    libraryStatus     = item.status,
                    isAddingToLibrary = false
                )
            } catch (e: Exception) {
                val updated = _uiState.value as? MediaDetailUiState.Success ?: return@launch
                _uiState.value = updated.copy(isAddingToLibrary = false)
            }
        }
    }
}