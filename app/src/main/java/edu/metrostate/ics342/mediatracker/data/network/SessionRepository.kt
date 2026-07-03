package edu.metrostate.ics342.mediatracker.data.network



import edu.metrostate.ics342.mediatracker.data.model.UserProfile

interface SessionRepository {
    suspend fun saveSession(accessToken: String, refreshToken: String, user: UserProfile)
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun getUser(): UserProfile?
    suspend fun clearSession()
}