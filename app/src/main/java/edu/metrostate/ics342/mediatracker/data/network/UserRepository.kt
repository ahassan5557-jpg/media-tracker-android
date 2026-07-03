package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.model.UserProfile
interface UserRepository {
    suspend fun register(
        email: String,
        password: String,
        username: String,
        displayName: String
    ): RegisterResult
    suspend fun login(email: String, password: String): LoginResult
}

sealed interface RegisterResult {
    data object Success : RegisterResult
    data object Conflict : RegisterResult
    data object NetworkError : RegisterResult
    data object UnknownError : RegisterResult
}
sealed interface LoginResult {
    data class Success(
        val accessToken: String,
        val refreshToken: String,
        val user: UserProfile
    ) : LoginResult
    data object InvalidCredentials : LoginResult
    data object NetworkError : LoginResult
    data object UnknownError : LoginResult
}