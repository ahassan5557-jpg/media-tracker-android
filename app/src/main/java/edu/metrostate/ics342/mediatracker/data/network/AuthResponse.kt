package edu.metrostate.ics342.mediatracker.data.network


import edu.metrostate.ics342.mediatracker.data.model.UserProfile
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserProfile
)