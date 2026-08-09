package edu.metrostate.ics342.mediatracker.data.model


import kotlinx.serialization.Serializable

@Serializable
data class Favorite(
    val userId: String,
    val mediaId: Int,
    val createdAt: String,
    val media: Media? = null
)