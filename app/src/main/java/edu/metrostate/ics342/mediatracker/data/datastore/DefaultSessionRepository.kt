package edu.metrostate.ics342.mediatracker.data.datastore


import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import edu.metrostate.ics342.mediatracker.data.network.SessionRepository
import edu.metrostate.ics342.mediatracker.data.model.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "session")

class DefaultSessionRepository(private val context: Context) : SessionRepository {

    private object Keys {
        val ACCESS_TOKEN  = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val USER_JSON     = stringPreferencesKey("user_json")
    }

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun saveSession(accessToken: String, refreshToken: String, user: UserProfile) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACCESS_TOKEN]  = accessToken
            prefs[Keys.REFRESH_TOKEN] = refreshToken
            prefs[Keys.USER_JSON]     = json.encodeToString(user)
        }
    }

    override suspend fun getAccessToken(): String? =
        context.dataStore.data.first()[Keys.ACCESS_TOKEN]

    override suspend fun getRefreshToken(): String? =
        context.dataStore.data.first()[Keys.REFRESH_TOKEN]

    override suspend fun getUser(): UserProfile? =
        context.dataStore.data.first()[Keys.USER_JSON]?.let { json.decodeFromString(it) }

    override suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}