package edu.metrostate.ics342.mediatracker.data.network


import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val grantType: String = "password",
    val email: String,
    val password: String,
    val clientId: String,
    val clientSecret: String
)