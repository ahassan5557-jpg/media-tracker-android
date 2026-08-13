package edu.metrostate.ics342.mediatracker.data.network

//import edu.metrostate.ics342.mediatracker.data.SessionRepository
import edu.metrostate.ics342.mediatracker.data.model.DuplicateFavoriteException
import edu.metrostate.ics342.mediatracker.data.model.DuplicateLibraryException
import edu.metrostate.ics342.mediatracker.data.model.ErrorResponse
import edu.metrostate.ics342.mediatracker.data.model.Favorite
import edu.metrostate.ics342.mediatracker.data.model.LibraryItem
import edu.metrostate.ics342.mediatracker.data.model.LibraryStatus
import edu.metrostate.ics342.mediatracker.data.model.Media
import edu.metrostate.ics342.mediatracker.data.model.MediaDetail
import edu.metrostate.ics342.mediatracker.data.model.MediaNotFoundException
import edu.metrostate.ics342.mediatracker.data.model.Quote
import edu.metrostate.ics342.mediatracker.data.model.Review
import retrofit2.Response

data class MediaPage(
    val items: List<Media>,
    val nextCursor: String?,
    val hasMore: Boolean
)

data class LibraryPage(
    val items: List<LibraryItem>,
    val nextCursor: String?,
    val hasMore: Boolean
)

class DefaultMediaRepository(sessionRepository: SessionRepository) {

    private val api = RetrofitInstance.mediaApiService(sessionRepository)

    private fun parseErrorMessage(response: Response<*>): String? = try {
        response.errorBody()?.string()?.let {
            RetrofitInstance.json.decodeFromString<ErrorResponse>(it).message
        }
    } catch (e: Exception) { null }

    suspend fun search(query: String, type: String?, after: String?): MediaPage {
        val response = api.searchMedia(
            query = query.ifBlank { null },
            type  = type?.ifBlank { null },
            after = after
        )
        val items      = response.body() ?: emptyList()
        val nextCursor = response.headers()["X-Next-Cursor"]
        val hasMore    = response.headers()["X-Has-More"] == "true"
        return MediaPage(items, nextCursor, hasMore)
    }

    suspend fun getMediaDetail(id: Int): MediaDetail {
        val response = api.getMediaDetail(id)
        if (response.code() == 404) {
            val message = parseErrorMessage(response) ?: "Media not found"
            throw MediaNotFoundException(message)
        }
        if (!response.isSuccessful) {
            val message = parseErrorMessage(response) ?: "Failed to load media (${response.code()})"
            error(message)
        }
        return response.body() ?: error("Empty body for media detail $id")
    }

    /** Returns null when the item is not in the library (HTTP 404). Throws for other errors. */
    suspend fun getLibraryItem(mediaId: Int): LibraryItem? {
        val response = api.getLibraryItem(mediaId)
        if (response.code() == 404) return null
        if (!response.isSuccessful) error("Failed to load library item: ${response.code()}")
        return response.body()
    }

    /** Throws [DuplicateLibraryException] when the item is already in the library (HTTP 409). */
    suspend fun addToLibrary(mediaId: Int, status: LibraryStatus): LibraryItem {
        val response = api.addToLibrary(AddToLibraryRequest(mediaId, status))
        if (response.code() == 409) throw DuplicateLibraryException()
        if (!response.isSuccessful) {
            val message = parseErrorMessage(response) ?: "Failed to add to library (${response.code()})"
            error(message)
        }
        return response.body() ?: error("Empty body adding mediaId $mediaId to library")
    }

    suspend fun updateLibraryStatus(mediaId: Int, status: LibraryStatus): LibraryItem {
        val response = api.updateLibraryStatus(mediaId, UpdateLibraryStatusRequest(status))
        if (!response.isSuccessful) {
            val message = parseErrorMessage(response) ?: "Failed to update status (${response.code()})"
            error(message)
        }
        return response.body() ?: error("Empty body updating status for mediaId $mediaId")
    }

    suspend fun removeFromLibrary(mediaId: Int) {
        val response = api.removeFromLibrary(mediaId)
        if (!response.isSuccessful) {
            val message = parseErrorMessage(response) ?: "Failed to remove from library (${response.code()})"
            error(message)
        }
    }

    suspend fun getLibrary(status: LibraryStatus?, after: String? = null): LibraryPage {
        val response = api.getLibrary(status = status?.toApiString(), after = after)
        if (!response.isSuccessful) {
            val message = parseErrorMessage(response) ?: "Failed to load library (${response.code()})"
            error(message)
        }
        val items      = response.body() ?: emptyList()
        val nextCursor = response.headers()["X-Next-Cursor"]
        val hasMore    = response.headers()["X-Has-More"] == "true"
        return LibraryPage(items, nextCursor, hasMore)
    }

    /** Returns null when the item is not favorited (HTTP 404). Throws for other errors. */
    suspend fun getFavorite(mediaId: Int): Favorite? {
        val response = api.getFavorite(mediaId)
        if (response.code() == 404) return null
        if (!response.isSuccessful) error("Failed to load favorite: ${response.code()}")
        return response.body()
    }

    /** Throws [DuplicateFavoriteException] when the item is already favorited (HTTP 409). */
    suspend fun addFavorite(mediaId: Int): Favorite {
        val response = api.addFavorite(AddToFavoritesRequest(mediaId))
        if (response.code() == 409) throw DuplicateFavoriteException()
        if (!response.isSuccessful) {
            val message = parseErrorMessage(response) ?: "Failed to save favorite (${response.code()})"
            error(message)
        }
        return response.body() ?: error("Empty body adding mediaId $mediaId to favorites")
    }

    suspend fun removeFavorite(mediaId: Int) {
        val response = api.removeFavorite(mediaId)
        if (!response.isSuccessful) {
            val message = parseErrorMessage(response) ?: "Failed to remove favorite (${response.code()})"
            error(message)
        }
    }

    suspend fun getReviews(mediaId: Int): List<Review> {
        val response = api.getReviews(mediaId)
        if (!response.isSuccessful) return emptyList()
        return response.body() ?: emptyList()
    }

    // ── Quotes (Week 1) ─────────────────────────────────────────────────

    /**
     * Returns the current user's own quotes (public + private mixed, no filter yet).
     * Failures are swallowed to an empty list, matching [getReviews]'s best-effort style —
     * quotes are supplementary content on the detail screen, not something that should
     * fail the whole screen if this one call has trouble.
     */
    suspend fun getQuotes(): List<Quote> {
        val response = api.getQuotes()
        if (!response.isSuccessful) return emptyList()
        return response.body() ?: emptyList()
    }

    suspend fun createQuote(
        mediaId: Int,
        quoteText: String,
        pageNumber: Int?,
        isPublic: Boolean
    ): Quote {
        val response = api.createQuote(
            CreateQuoteRequest(
                mediaId    = mediaId,
                quoteText  = quoteText,
                pageNumber = pageNumber,
                isPublic   = isPublic
            )
        )
        if (!response.isSuccessful) {
            val message = parseErrorMessage(response) ?: "Failed to save quote (${response.code()})"
            error(message)
        }
        return response.body() ?: error("Empty body creating quote for mediaId $mediaId")
    }
}
