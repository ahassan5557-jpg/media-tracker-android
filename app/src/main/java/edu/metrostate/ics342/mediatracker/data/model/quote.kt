package edu.metrostate.ics342.mediatracker.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Quote(
    val id: Int = 0,
    val userId: String,
    val mediaId: Int,
    val quoteText: String,
    val pageNumber: Int? = null,
    val isPublic: Boolean = false,
    val likeCount: Int = 0,
    val createdAt: String,
    val media: Media? = null
)
