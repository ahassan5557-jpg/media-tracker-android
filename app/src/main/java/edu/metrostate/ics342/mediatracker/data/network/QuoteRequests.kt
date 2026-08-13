package edu.metrostate.ics342.mediatracker.data.network


import kotlinx.serialization.Serializable

@Serializable
data class CreateQuoteRequest(
    val mediaId: Int,
    val quoteText: String,
    val pageNumber: Int? = null,
    val isPublic: Boolean = false
)

@Serializable
data class UpdateQuoteRequest(
    val quoteText: String,
    val pageNumber: Int? = null,
    val isPublic: Boolean = false
)
