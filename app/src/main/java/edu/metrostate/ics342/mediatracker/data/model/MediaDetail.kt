package edu.metrostate.ics342.mediatracker.data.model


import android.content.Context
import edu.metrostate.ics342.mediatracker.R
import kotlinx.serialization.Serializable


@Serializable
data class MediaDetail(
    val id: Int,
    val mediaType: MediaType,
    val title: String,
    val author: String? = null,        // books
    val director: String? = null,      // movies
    val creator: String? = null,       // shows
    val network: String? = null,       // shows
    val coverUrl: String? = null,
    val publishedYear: Int? = null,
    val averageRating: Float = 0f,
    val ratingCount: Int = 0,
    val genres: List<String> = emptyList(),
    // ── MediaDetail-only fields ──────────────────────────────────────────────
    val description: String? = null,
    val pageCount: Int? = null,        // books
    val runtimeMinutes: Int? = null,   // movies
    val seasonCount: Int? = null,      // shows
    val episodeCount: Int? = null,     // shows
    val isbn: String? = null,          // books
    val reviewCount: Int = 0
)

fun MediaDetail.creatorCredit(context: Context): String = when (mediaType) {
    MediaType.BOOK    -> author   ?: context.getString(R.string.media_unknown_author)
    MediaType.MOVIE   -> director ?: context.getString(R.string.media_unknown_director)
    MediaType.SHOW    -> creator  ?: context.getString(R.string.media_unknown_creator)
    MediaType.UNKNOWN -> ""
}