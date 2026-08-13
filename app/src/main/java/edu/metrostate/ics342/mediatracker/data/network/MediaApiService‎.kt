package edu.metrostate.ics342.mediatracker.data.network

import edu.metrostate.ics342.mediatracker.data.model.Favorite
import edu.metrostate.ics342.mediatracker.data.model.LibraryItem
import edu.metrostate.ics342.mediatracker.data.model.Media
import edu.metrostate.ics342.mediatracker.data.model.MediaDetail
import edu.metrostate.ics342.mediatracker.data.model.Quote
import edu.metrostate.ics342.mediatracker.data.model.Review
import retrofit2.Response
import retrofit2.http.*

interface MediaApiService {
    @GET("media")
    suspend fun searchMedia(
        @Query("query") query: String? = null,
        @Query("type") type: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("after") after: String? = null
    ): Response<List<Media>>

    @GET("media/{id}")
    suspend fun getMediaDetail(@Path("id") id: Int): Response<MediaDetail>

    @GET("library")
    suspend fun getLibrary(
        @Query("status") status: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("after") after: String? = null
    ): Response<List<LibraryItem>>

    @GET("library/{mediaId}")
    suspend fun getLibraryItem(@Path("mediaId") mediaId: Int): Response<LibraryItem>

    @POST("library")
    suspend fun addToLibrary(@Body body: AddToLibraryRequest): Response<LibraryItem>

    @PUT("library/{mediaId}")
    suspend fun updateLibraryStatus(
        @Path("mediaId") mediaId: Int,
        @Body body: UpdateLibraryStatusRequest
    ): Response<LibraryItem>

    @DELETE("library/{mediaId}")
    suspend fun removeFromLibrary(@Path("mediaId") mediaId: Int): Response<Unit>

    @GET("favorites/{mediaId}")
    suspend fun getFavorite(@Path("mediaId") mediaId: Int): Response<Favorite>

    @POST("favorites")
    suspend fun addFavorite(@Body body: AddToFavoritesRequest): Response<Favorite>

    @DELETE("favorites/{mediaId}")
    suspend fun removeFavorite(@Path("mediaId") mediaId: Int): Response<Unit>

    @GET("reviews")
    suspend fun getReviews(@Query("mediaId") mediaId: Int): Response<List<Review>>

    // ── Quotes ──────────────────────────────────────────────────────────
    // Week 1: only getQuotes + createQuote are actually called.
    // PUT/DELETE/likes are declared now so Week 2 doesn't require touching this file again.

    @GET("quotes")
    suspend fun getQuotes(
        @Query("public") public: Boolean? = null
    ): Response<List<Quote>>

    @POST("quotes")
    suspend fun createQuote(@Body body: CreateQuoteRequest): Response<Quote>

    @PUT("quotes/{id}")
    suspend fun updateQuote(
        @Path("id") id: Int,
        @Body body: UpdateQuoteRequest
    ): Response<Quote>

    @DELETE("quotes/{id}")
    suspend fun deleteQuote(@Path("id") id: Int): Response<Unit>

    @POST("quotes/{id}/likes")
    suspend fun likeQuote(@Path("id") id: Int): Response<Unit>

    @DELETE("quotes/{id}/likes")
    suspend fun unlikeQuote(@Path("id") id: Int): Response<Unit>
}