package edu.metrostate.ics342.mediatracker.data.network


import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import kotlinx.coroutines.runBlocking
object RetrofitInstance {

    internal  val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults    = true
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(ApiConstants.BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val userApiService: UserApiService = retrofit.create(UserApiService::class.java)
    fun mediaApiService(sessionRepository: SessionRepository): MediaApiService {
        val authClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor { chain ->
                val token = runBlocking { sessionRepository.getAccessToken() }
                val request = chain.request().newBuilder()
                    .apply { if (token != null) addHeader("Authorization", "Bearer $token") }
                    .build()
                chain.proceed(request)
            }
            .build()

        val authRetrofit = Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(authClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        return authRetrofit.create(MediaApiService::class.java)
    }
}
